package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

@CardRegistration(set = "LRW", collectorNumber = "226")
public class LammastideWeave extends Card {

    public LammastideWeave() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(new MillEffect(1, Subjects.targetPlayer()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.toACard(StepKind.MILL, named),
                new GainLifeEffect(AmountThisWay.milledManaValue(named))));
        addSpellEffect(new DrawCardEffect(1));
    }
}
