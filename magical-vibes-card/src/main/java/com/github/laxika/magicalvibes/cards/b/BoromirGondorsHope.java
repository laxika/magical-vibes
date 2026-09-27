package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "49")
@CardRegistration(set = "LTC", collectorNumber = "132")
public class BoromirGondorsHope extends Card {

    public BoromirGondorsHope() {
        LookAtTopCardsEffect search = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.HUMAN),
                        new CardTypePredicate(CardType.ARTIFACT))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, search);
        addEffect(EffectSlot.ON_ATTACK, search);
    }
}
