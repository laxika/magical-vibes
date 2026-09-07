package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;

@CardRegistration(set = "EOE", collectorNumber = "24")
public class LightstallInquisitor extends Card {

    public LightstallInquisitor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                EachOpponentExilesFromHandEffect.withPlayPermission(1, 1, true));
    }
}
