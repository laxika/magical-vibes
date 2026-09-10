package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Capability for a static effect that gives its source card named subtypes in every zone.
 * Characteristic queries use this capability when evaluating a card outside the battlefield.
 */
public interface SelfAllZoneSubtypeGrantingEffect extends CardEffect {

    List<CardSubtype> grantedSubtypes();
}
