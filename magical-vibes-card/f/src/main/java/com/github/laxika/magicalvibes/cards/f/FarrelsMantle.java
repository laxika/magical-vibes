package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FEM", collectorNumber = "2")
@CardRegistration(set = "FEM", collectorNumber = "138")
@CardRegistration(set = "ME2", collectorNumber = "13")
public class FarrelsMantle extends Card {

    public FarrelsMantle() {
        // Enchant creature
        target(TargetFilters.creature());

        // Whenever enchanted creature attacks and isn't blocked, its controller may have it deal
        // damage equal to its power plus 2 to another target creature. If that player does, the
        // attacking creature assigns no combat damage this turn.
        addTrigger(Triggers.enchantedCreatureAttacksUnblocked(),
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(
                                new Sum(new SourcePower(true), new Fixed(2)),
                                false,
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                        new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power plus 2 to another target creature?",
                        null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
