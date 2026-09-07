package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MAT", collectorNumber = "17")
public class KolaghanWarmonger extends Card {

    public KolaghanWarmonger() {
        addEffect(EffectSlot.ON_ATTACK, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardSubtypePredicate(CardSubtype.DRAGON)));
    }
}
