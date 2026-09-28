package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/** Cost reduction supplied by a battlefield permanent for spells targeting its attachment. */
public interface BattlefieldTargetCastCostReducingEffect extends CardEffect {

    int amount();

    boolean appliesTo(UUID castingPlayerId, UUID sourceControllerId, Permanent sourcePermanent,
                      Card spell, List<UUID> targetIds);
}
