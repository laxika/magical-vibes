package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MH2", collectorNumber = "143")
public class StrikeItRich extends Card {

    public StrikeItRich() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
