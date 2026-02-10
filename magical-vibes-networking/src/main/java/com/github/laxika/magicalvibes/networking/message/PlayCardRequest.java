package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

public record PlayCardRequest(int cardIndex, Integer xValue, UUID targetPermanentId) {
}
