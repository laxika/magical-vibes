package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/** Capability for a static effect that increases damage from controlled creatures of a subtype. */
public interface SubtypeSourceDamageBonusEffect extends CardEffect {

    Set<CardSubtype> subtypes();

    int amount();
}
