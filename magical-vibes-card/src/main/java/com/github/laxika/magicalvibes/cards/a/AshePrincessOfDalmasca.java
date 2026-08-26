package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "7")
public class AshePrincessOfDalmasca extends Card {

    public AshePrincessOfDalmasca() {
        addEffect(EffectSlot.ON_ATTACK,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5,
                        new CardTypePredicate(CardType.ARTIFACT)));
    }
}
