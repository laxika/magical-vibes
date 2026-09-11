package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "80")
public class SilundiVision extends Card {

    public SilundiVision() {
        SilundiIsle backFace = new SilundiIsle();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "SilundiIsle";
    }
}
