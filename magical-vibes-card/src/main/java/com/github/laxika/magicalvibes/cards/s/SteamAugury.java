package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "THS", collectorNumber = "205")
public class SteamAugury extends Card {

    public SteamAugury() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(5, true));
    }
}
