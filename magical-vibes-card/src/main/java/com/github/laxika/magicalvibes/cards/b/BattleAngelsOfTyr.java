package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BattleAngelsOfTyrEffect;

@CardRegistration(set = "SLD", collectorNumber = "875")
public class BattleAngelsOfTyr extends Card {

    public BattleAngelsOfTyr() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new BattleAngelsOfTyrEffect());
    }
}
