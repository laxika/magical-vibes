package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "281")
@CardRegistration(set = "TPR", collectorNumber = "220")
@CardRegistration(set = "VMA", collectorNumber = "267")
public class CursedScroll extends Card {

    public CursedScroll() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new ChooseCardNameAtResolutionEffect(),
                        RevealCardsFromHandEffect.atRandom(Subjects.you(), 1),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, named),
                                new DealDamageToAnyTargetEffect(2))),
                "{3}, {T}: Choose a card name, then reveal a card at random from your hand. "
                        + "If that card has the chosen name, Cursed Scroll deals 2 damage to any target."));
    }
}
