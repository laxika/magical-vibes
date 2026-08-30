package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Carries the top card while Psychotic Episode's combined hand/library choice is pending. */
public record PendingPsychoticEpisodeChoice(UUID targetPlayerId, UUID topCardId)
        implements PendingInteraction {
}
