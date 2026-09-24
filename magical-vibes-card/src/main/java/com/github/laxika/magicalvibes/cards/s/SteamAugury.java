package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "THS", collectorNumber = "205")
@CardRegistration(set = "DDN", collectorNumber = "68")
@CardRegistration(set = "PIO", collectorNumber = "244")
@CardRegistration(set = "C15", collectorNumber = "233")
public class SteamAugury extends Card {

    public SteamAugury() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(5, true));
    }
}
