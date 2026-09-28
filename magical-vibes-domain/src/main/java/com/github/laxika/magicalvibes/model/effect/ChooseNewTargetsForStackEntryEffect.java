package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Pending retarget choice for one target occurrence of a stack entry. */
public record ChooseNewTargetsForStackEntryEffect(UUID stackEntryId, int targetIndex) implements CardEffect {}
