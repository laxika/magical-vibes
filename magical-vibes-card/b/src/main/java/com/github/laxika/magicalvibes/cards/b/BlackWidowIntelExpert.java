package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForYouAndDamagedPlayerEffect;

@CardRegistration(set = "MSC", collectorNumber = "504")
public class BlackWidowIntelExpert extends Card {

    public BlackWidowIntelExpert() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardForYouAndDamagedPlayerEffect(2));
    }
}
