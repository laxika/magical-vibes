package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Capability for a static effect that makes its source a creature outside the battlefield. */
public interface SelfOutsideBattlefieldCreatureEffect extends CardEffect {

    int power();

    int toughness();

    List<CardSubtype> grantedSubtypes();
}
