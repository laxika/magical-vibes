package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

public class JourneyToTheOracle extends Card {

    public JourneyToTheOracle() {
        addSpellEffect(PutCardToBattlefieldEffect.anyNumber(new CardTypePredicate(CardType.LAND), "land"));
        addSpellEffect(new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new MayEffect(
                        SequenceEffect.of(
                                DiscardEffect.matching(1, Subjects.you(), new CardTruePredicate()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        ReturnToHandEffect.selfSpell())),
                        "Discard a card to return Journey to the Oracle to its owner's hand?")));
    }
}
