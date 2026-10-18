package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

/** Special action: reveal the chosen mission of the Secret Mission card {@code cardId} in the command zone. */
public record RevealSecretMissionRequest(UUID cardId) {
}
