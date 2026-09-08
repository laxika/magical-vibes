package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FEM", collectorNumber = "2")
@CardRegistration(set = "FEM", collectorNumber = "138")
public class FarrelsMantle extends Card {

    public FarrelsMantle() {
        // Enchant creature
        target(TargetFilters.creature());

        // Whenever enchanted creature attacks and isn't blocked, its controller may have it deal
        // damage equal to its power plus 2 to another target creature. If that player does, the
        // attacking creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(
                                new Sum(new SourcePower(), new Fixed(2)),
                                false,
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                        new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power plus 2 to another target creature?"));
    }
}
