package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferDrawAndCreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "13")
@CardRegistration(set = "BLC", collectorNumber = "49")
public class TemptWithBunnies extends Card {

    public TemptWithBunnies() {
        addEffect(EffectSlot.SPELL, new TemptingOfferDrawAndCreateTokenEffect(
                new CreateTokenEffect("Rabbit", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.RABBIT), Set.of(), Set.of())));
    }
}
