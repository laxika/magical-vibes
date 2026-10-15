package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerToughnessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "5")
@CardRegistration(set = "MKC", collectorNumber = "312")
public class DuskanaTheRageMother extends Card {

    public DuskanaTheRageMother() {
        PermanentAllOfPredicate twoTwoCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentBasePowerToughnessPredicate(2, 2)));

        // When Duskana enters, draw a card for each creature you control with base power and
        // toughness 2/2.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new PermanentCount(twoTwoCreature, CountScope.CONTROLLER)));

        // Whenever a creature you control with base power and toughness 2/2 attacks, it gets
        // +3/+3 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(twoTwoCreature,
                        new BoostTargetCreatureEffect(3, 3)));
    }
}
