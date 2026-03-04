package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "132")
public class SilverskinArmor extends Card {

    public SilverskinArmor() {
        // Equipped creature gets +1/+1
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(1, 1));
        // Equipped creature is an artifact in addition to its other types
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new EquipEffect()),
                "Equip {2}",
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
