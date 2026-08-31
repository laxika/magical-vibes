package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Carries Animal Magnetism's controller and revealed cards while an opponent chooses a creature. */
public record PendingAnimalMagnetismChoice(UUID controllerId) implements PendingInteraction {
}
