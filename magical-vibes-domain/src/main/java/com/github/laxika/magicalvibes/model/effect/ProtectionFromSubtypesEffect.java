package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

public record ProtectionFromSubtypesEffect(Set<CardSubtype> subtypes) implements CardEffect {
}
