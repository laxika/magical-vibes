package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithCardsPredicate;

@CardRegistration(set = "YDSK", collectorNumber = "12")
public class AnguishedRecollection extends Card {

    public AnguishedRecollection() {
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.discarded(), new SeekCardsToHandEffect(new Fixed(2),
                new CardNotPredicate(new CardSharesCardTypeWithCardsPredicate(
                        Subjects.cardsThisWay(StepKind.DISCARD))))));
    }
}
