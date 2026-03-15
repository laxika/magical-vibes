package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "124")
public class PistonSledge extends Card {

    public PistonSledge() {
        // When this Equipment enters, attach it to target creature you control.
        setTargetFilter(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect());

        // Equipped creature gets +3/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));

        // Equip — Sacrifice an artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeArtifactCost(), new EquipEffect()),
                "Equip — Sacrifice an artifact.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
