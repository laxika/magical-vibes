package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * "This land enters tapped unless you control a [subtype1] or a [subtype2]."
 * Used by the M10/M11 "check land" cycle (e.g. Dragonskull Summit, Glacial Fortress).
 */
public record EntersTappedUnlessControlLandSubtypeEffect(List<CardSubtype> requiredSubtypes) implements CardEffect {
}
