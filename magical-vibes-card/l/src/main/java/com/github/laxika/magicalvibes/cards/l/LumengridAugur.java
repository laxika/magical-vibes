package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "39")
public class LumengridAugur extends Card {

    public LumengridAugur() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DrawCardEffect(1, Subjects.targetPlayer()),
                        new DiscardEffect(1, Subjects.targetPlayer()),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.DISCARD, new CardTypePredicate(CardType.ARTIFACT)),
                                new UntapPermanentsEffect(Subjects.source()))),
                "{1}, {T}: Target player draws a card, then discards a card. If that player discards an artifact card this way, untap this creature."
        ));
    }
}
