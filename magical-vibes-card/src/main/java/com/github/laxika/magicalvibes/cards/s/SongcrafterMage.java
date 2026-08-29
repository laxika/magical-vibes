package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantHarmonizeToTargetGraveyardCardEffect;

@CardRegistration(set = "TDM", collectorNumber = "225")
public class SongcrafterMage extends Card {

    public SongcrafterMage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantHarmonizeToTargetGraveyardCardEffect());
    }
}
