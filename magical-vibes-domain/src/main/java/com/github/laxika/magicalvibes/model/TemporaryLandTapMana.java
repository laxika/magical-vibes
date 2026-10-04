package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Extra mana from a resolved spell, retaining that spell's controller for replacements. */
public record TemporaryLandTapMana(ManaColor color, UUID controllerId) {}
