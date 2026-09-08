package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Carries Murmurs from Beyond's opponent card choice while its revealed cards are prompted. */
public record PendingMurmursFromBeyondChoice(UUID controllerId) implements PendingInteraction {
}
