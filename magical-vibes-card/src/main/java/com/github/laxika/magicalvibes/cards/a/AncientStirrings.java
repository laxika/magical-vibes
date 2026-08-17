package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

@CardRegistration(set = "ROE", collectorNumber = "174")
public class AncientStirrings extends Card {

    public AncientStirrings() {
        // Look at the top five cards of your library. You may reveal a colorless card from among
        // them and put it into your hand. Then put the rest on the bottom of your library in any order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(5,
                new CardIsColorlessPredicate()));
    }
}
