package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "22")
public class WingbaneVantasaur extends Card {

    public WingbaneVantasaur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Savage Stomp into your hand",
                        new ConjureCardNamedIntoHandEffect("Savage Stomp", false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Naturalize into your hand",
                        new ConjureCardNamedIntoHandEffect("Naturalize", false))
        )));
    }
}
