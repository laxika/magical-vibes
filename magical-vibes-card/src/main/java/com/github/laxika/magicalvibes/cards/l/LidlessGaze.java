package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryMayPlayUntilNextTurnEffect;

@CardRegistration(set = "LTC", collectorNumber = "59")
@CardRegistration(set = "LTC", collectorNumber = "141")
public class LidlessGaze extends Card {

    public LidlessGaze() {
        addEffect(EffectSlot.SPELL, new ExileTopCardOfEachPlayersLibraryMayPlayUntilNextTurnEffect());
        addCastingOption(new FlashbackCast("{2}{B}{R}"));
    }
}
