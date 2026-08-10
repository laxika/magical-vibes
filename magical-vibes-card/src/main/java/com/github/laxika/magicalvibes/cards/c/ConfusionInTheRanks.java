package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "87")
public class ConfusionInTheRanks extends Card {

    private static final PermanentPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
            new PermanentSharesCardTypeWithSourcePermanentPredicate()
    ));

    public ConfusionInTheRanks() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.ENCHANTMENT))),
                        ExchangeControlOfTargetPermanentsEffect.forTriggeringPermanent(TARGET)));
    }
}
