package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "68")
@CardRegistration(set = "LRW", collectorNumber = "79")
@CardRegistration(set = "M12", collectorNumber = "73")
@CardRegistration(set = "SLD", collectorNumber = "245")
@CardRegistration(set = "SLD", collectorNumber = "1783")
@CardRegistration(set = "SLD", collectorNumber = "2292")
@CardRegistration(set = "SLC", collectorNumber = "69")
@CardRegistration(set = "TSR", collectorNumber = "315")
@CardRegistration(set = "MAR", collectorNumber = "13")
@CardRegistration(set = "OMB", collectorNumber = "13")
public class Ponder extends Card {

    public Ponder() {
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(3));
        addEffect(EffectSlot.SPELL, new MayEffect(new ShuffleLibraryEffect(false), "You may shuffle your library."));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
