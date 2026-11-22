package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;

@CardRegistration(set = "ODY", collectorNumber = "94")
@CardRegistration(set = "SLD", collectorNumber = "2156")
@CardRegistration(set = "C18", collectorNumber = "98")
public class Predict extends Card {

    public Predict() {
        addSpellEffect(new ChooseCardNameAtResolutionEffect());
        addSpellEffect(new MillEffect(1, Subjects.targetPlayer()));
        // "Draw two cards. Otherwise, draw a card": one more when a card with the chosen name was milled this way.
        addSpellEffect(new ConditionalEffect(
                HappenedThisWay.toACard(StepKind.MILL, new CardHasNameChosenThisResolutionPredicate()),
                new DrawCardEffect(1)));
        addSpellEffect(new DrawCardEffect(1));
    }
}
