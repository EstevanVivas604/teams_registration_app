const totalPlayers = 5;
const players = [];

document.addEventListener('DOMContentLoaded', () => {
    const startBtn = document.getElementById('start-btn');
    const teamSection = document.getElementById('team-section');
    const playerForm = document.getElementById('player-form');
    const playerFormDiv = document.getElementById('player-form-div');
    const addPlayerBtn = document.getElementById('add-player-btn');
    const playerNumberLabel = document.getElementById('player-number');
    const progressBar = document.getElementById('progress-bar');


    progressBar.style.width = `${(1 / totalPlayers) * 100}%`;

    startBtn.addEventListener('click', async () => {
        const teamNameInput = document.getElementById('team-name');
        const teamName = teamNameInput.value.trim();
        if (!teamName) {
            showErrorNotification('Ingrese un nombre de equipo.');
            return;
        }

        if (!await validateTeamName(teamName)) {
            return;
        }

        teamSection.style.display = 'none';
        playerFormDiv.style.display = 'block';
    });

    addPlayerBtn.addEventListener('click', async () => {
        if (!await addPlayer()) {
            return;
        }

        playerForm.reset();

        if (players.length === totalPlayers) {
            playerFormDiv.style.display = 'none';
            showCaptainSelection();
            return;
        }

        playerNumberLabel.textContent = (players.length + 1).toString();
        progressBar.style.width = `${((players.length + 1) / totalPlayers) * 100}%`;
    });

    function showCaptainSelection() {
        const captainSelectionDiv = document.getElementById('captain-selection');
        const playerListDiv = document.getElementById('player-list');

        playerListDiv.innerHTML = '';
        players.forEach((player, index) => {
            const playerOptionHTML = `
                <div class="player-option">
                    <input type="radio" name="captain" value="${index}" id="captain-${index}" required>
                    <label for="captain-${index}">${player.names} ${player.surnames} (${player.alias})</label>
                </div>
            `;
            playerListDiv.innerHTML += playerOptionHTML;
        });

        captainSelectionDiv.style.display = 'block';
    }

    document.getElementById('finalize-btn').addEventListener('click', registerTeam);
});

function showNotification(type, message) {
    const notification = document.createElement('div');
    notification.classList.add('notification', type, 'show');

    notification.innerHTML = `
        <div class="notification-content">
            <p class="notification-message">${message}</p>
        </div>
        <button class="close-btn">&times;</button>
    `;
    document.body.appendChild(notification);

    notification.querySelector('.close-btn').addEventListener('click', () => {
        closeNotification(notification);
    });

    setTimeout(() => closeNotification(notification), 3000);
}

function showErrorNotification(message) {
    showNotification('error', message);
}

function showSuccessNotification(message) {
    showNotification('success', message);
}

function closeNotification(notification) {
    notification.classList.remove('show');
    setTimeout(() => notification.remove(), 500);
}

async function addPlayer() {
    const names = document.getElementById('names').value.trim();
    const surnames = document.getElementById('surnames').value.trim();
    const birthdate = document.getElementById('birthdate').value.trim();
    const email = document.getElementById('email').value.trim();
    const alias = document.getElementById('alias').value.trim();

    if (!names || !surnames || !birthdate || !email || !alias) {
        showErrorNotification('Por favor complete todos los campos.');
        return false;
    }

    if (!validateName(names, surnames) || !validateEmailFormat(email) || !validateBirthdate(birthdate)) {
        return false;
    }

    if (players.some((p) => p.email === email)) {
        showErrorNotification("El correo electrónico ya está en uso por uno de los jugadores agregados anteriormente.");
        return false;
    }


    if (players.some((p) => p.alias === alias)) {
        showErrorNotification("El alias ya está en uso por uno de los jugadores agregados anteriormente.");
        return false;
    }

    const [isAliasValid, isEmailValid] = await Promise.all([
        validateAlias(alias),
        validateEmail(email)
    ]);

    if (!isAliasValid || !isEmailValid) {
        return false;
    }

    const player = {
        names: names,
        surnames: surnames,
        birthdate: birthdate,
        email: email,
        alias: alias
    };

    players.push(player);
    return true;
}

function validateName(names, surnames) {
    const regex = /^[A-Za-z\s]+$/;
    const resultTest = regex.test(names) && regex.test(surnames);
    if (!resultTest) {
        showErrorNotification('Los nombres y apellidos no pueden contener números ni caracteres especiales.')
    }

    return resultTest;
}

function validateEmailFormat(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const isValid = emailRegex.test(email);

    if (!isValid) {
        showErrorNotification('Ingrese un correo electrónico válido.');
    }

    return isValid;
}


function validateBirthdate(birthdate) {
    const minAge = 18;
    const age = calculateAge(birthdate);
    if (age < minAge) {
        showErrorNotification(`Los jugadores deben tener al menos ${minAge} años para registrarse.`);
    }

    return age >= minAge;
}

function calculateAge(birthDate) {
    const today = new Date();
    const birth = new Date(birthDate);

    let age = today.getFullYear() - birth.getFullYear();
    const monthDifference = today.getMonth() - birth.getMonth();

    if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birth.getDate())) {
        age--;
    }

    return age;
}

async function getRequestResponse(url) {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        return {
            statusCode: response.status,
            data: response.ok ? await response.json() : null,
        };
    } catch (error) {
        return {
            statusCode: 503, //connection error
            data: null
        };
    }
}

function handleRequestError() {
    showErrorNotification('Hubo un problema al conectarse con el servidor. Intente de nuevo.');
}

async function validateEmail(email) {
    const result = await getRequestResponse(`/players?email=${email}`);

    if (result.statusCode === 200) {
        showErrorNotification(`El correo electrónico '${email}' ya está registrado.`);
        return false;
    }

    if (result.statusCode === 404) {
        return true;
    }

    handleRequestError();
    return false;
}

async function validateAlias(alias) {
    const result = await getRequestResponse(`/players?alias=${alias}`);

    if (result.statusCode === 200) {
        showErrorNotification(`El alias '${alias}' ya está en uso.`);
        return false;
    }

    if (result.statusCode === 404) {
        return true;
    }

    handleRequestError();
    return false;
}

async function validateTeamName(teamName) {
    const result = await getRequestResponse(`/teams?team=${teamName}`);

    if (result.statusCode === 200) {
        showErrorNotification(`El nombre de equipo '${teamName}' ya está en uso.`);
        return false;
    }

    if (result.statusCode === 404) {
        return true;
    }

    handleRequestError();
    return false;
}

async function registerTeam() {
    const teamName = document.getElementById('team-name').value.trim();
    const selectedCaptain = document.querySelector('input[name="captain"]:checked');

    if (!selectedCaptain) {
        showErrorNotification('Seleccione un jugador como capitán del equipo.');
        return;
    }

    try {
        const response = await fetch('/teams/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: teamName,
                players: players,
                captainIdx: selectedCaptain.value
            })
        });

        switch (response.status) {
            case 200:
                showSuccessNotification('Equipo registrado exitosamente.');
                setTimeout(() => {
                    window.location.reload();
                }, 3000);
                break;

            case 400:
                showErrorNotification('El registro no pudo completarse. Algunos datos proporcionados son inválidos o incompletos. Revise la información e intente nuevamente.');
                setTimeout(() => {
                    window.location.reload();
                }, 3000);
                break;

            default:
                handleRequestError();
                break;
        }

    } catch (e) {
        showErrorNotification('No se ha podido completar el registro. Intente de nuevo.');
    }
}
