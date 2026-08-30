package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Carries Allure of the Unknown's controller and opponent through the revealed-card choice. */
public record PendingAllureOfTheUnknownChoice(UUID controllerId, UUID opponentId)
        implements PendingInteraction {
}
