package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardWithKeywordFromGraveyardToHandEffect;

@CardRegistration(set = "SOM", collectorNumber = "147")
public class CorpseCur extends Card {

    public CorpseCur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new ReturnCardWithKeywordFromGraveyardToHandEffect(CardType.CREATURE, Keyword.INFECT),
                        "Return a creature card with infect from your graveyard to your hand?"
                )
        );
    }
}
