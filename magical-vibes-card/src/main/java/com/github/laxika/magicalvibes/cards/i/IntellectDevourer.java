package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;

@CardRegistration(set = "HBG", collectorNumber = "162")
public class IntellectDevourer extends Card {

    public IntellectDevourer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                EachOpponentExilesFromHandEffect.withReturnOnSourceLeave(1));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(true));
    }
}
