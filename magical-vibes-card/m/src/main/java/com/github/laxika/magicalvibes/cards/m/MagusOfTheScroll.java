package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "169")
public class MagusOfTheScroll extends Card {

    public MagusOfTheScroll() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new ChooseCardNameAtResolutionEffect(),
                        RevealCardsFromHandEffect.atRandom(Subjects.you(), 1),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, named),
                                DealDamageEffect.toAnyTarget(2))),
                "{3}, {T}: Choose a card name, then reveal a card at random from your hand. "
                        + "If that card has the chosen name, Magus of the Scroll deals 2 damage to any target."));
    }
}
