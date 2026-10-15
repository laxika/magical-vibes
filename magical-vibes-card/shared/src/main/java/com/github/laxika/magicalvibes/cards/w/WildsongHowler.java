package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class WildsongHowler extends Card {

    public WildsongHowler() {
        LookAtTopCardsEffect lookAtTopCards = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardTypePredicate(CardType.CREATURE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, lookAtTopCards);
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, lookAtTopCards);
    }
}
