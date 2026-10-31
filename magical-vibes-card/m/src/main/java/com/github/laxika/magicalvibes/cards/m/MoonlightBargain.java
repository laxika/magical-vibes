package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "RAV", collectorNumber = "95")
@CardRegistration(set = "C18", collectorNumber = "114")
public class MoonlightBargain extends Card {

    public MoonlightBargain() {
        addSpellEffect(LookAtTopCardsEffect.mayChooseAnyNumberToHandRestToGraveyardPayLife(5, 2));
    }
}
