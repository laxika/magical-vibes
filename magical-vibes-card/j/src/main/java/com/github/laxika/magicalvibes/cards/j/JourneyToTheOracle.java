package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

public class JourneyToTheOracle extends Card {

    public JourneyToTheOracle() {
        addEffect(EffectSlot.SPELL,
                PutCardToBattlefieldEffect.anyNumber(new CardTypePredicate(CardType.LAND), "land"));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new MayEffect(
                        new DiscardCardThenEffect(
                                new CardTruePredicate(),
                                ReturnToHandEffect.selfSpell(),
                                "a card"),
                        "Discard a card to return Journey to the Oracle to its owner's hand?")));
    }
}
