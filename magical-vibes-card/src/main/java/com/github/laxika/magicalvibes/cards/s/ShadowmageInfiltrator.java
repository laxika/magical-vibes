package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ODY", collectorNumber = "294")
@CardRegistration(set = "TSB", collectorNumber = "99")
public class ShadowmageInfiltrator extends Card {

    public ShadowmageInfiltrator() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
