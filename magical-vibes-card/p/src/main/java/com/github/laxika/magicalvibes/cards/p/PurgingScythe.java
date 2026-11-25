package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastToughnessAmongOpponentCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "308")
public class PurgingScythe extends Card {

    public PurgingScythe() {
        // At the beginning of your upkeep, this artifact deals 2 damage to the creature with the least toughness.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentHasLeastToughnessAmongOpponentCreaturesPredicate(
                                                CountScope.ANY_PLAYER)))),
                        "Choose a creature with the least toughness to deal 2 damage to."),
                DealDamageEffect.to(Subjects.thisWay(StepKind.CHOOSE), 2)));
    }
}
