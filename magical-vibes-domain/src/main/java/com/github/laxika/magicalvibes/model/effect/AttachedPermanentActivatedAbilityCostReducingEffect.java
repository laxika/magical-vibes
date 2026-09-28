package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/** Cost reduction whose qualifying target is the permanent carrying this effect's attachment. */
public interface AttachedPermanentActivatedAbilityCostReducingEffect
        extends ActivatedAbilityCostReducingEffect {

    boolean appliesTo(ActivatedAbility ability, Permanent reducingPermanent,
                      UUID targetId, List<UUID> targetIds);
}
