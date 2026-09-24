package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SLD", collectorNumber = "145")
@CardRegistration(set = "SLD", collectorNumber = "962")
public class GregorShrewdMagistrate extends Card {

    public GregorShrewdMagistrate() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(new SourcePower()));
    }
}
