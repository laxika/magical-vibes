package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;

@CardRegistration(set = "C20", collectorNumber = "39")
public class TidalBarracuda extends Card {

    public TidalBarracuda() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(null, true));
        addEffect(EffectSlot.STATIC, new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
    }
}
