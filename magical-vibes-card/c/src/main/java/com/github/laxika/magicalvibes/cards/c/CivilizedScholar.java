package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HomicidalBrute;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "47")
public class CivilizedScholar extends Card {

    public CivilizedScholar() {
        setBackFaceCard(new HomicidalBrute());

        // {T}: Draw a card, then discard a card. If a creature card is discarded this way,
        // untap Civilized Scholar, then transform it.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DrawCardEffect(1),
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.DISCARD, new CardTypePredicate(CardType.CREATURE)),
                                SequenceEffect.of(new UntapPermanentsEffect(Subjects.source()),
                                        new TransformSelfEffect()))),
                "{T}: Draw a card, then discard a card. If a creature card is discarded this way, untap Civilized Scholar, then transform it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HomicidalBrute";
    }
}
