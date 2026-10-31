package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "68")
@CardRegistration(set = "LRW", collectorNumber = "79")
@CardRegistration(set = "M12", collectorNumber = "73")
@CardRegistration(set = "DSC", collectorNumber = "73")
@CardRegistration(set = "SLD", collectorNumber = "245")
@CardRegistration(set = "SLD", collectorNumber = "1783")
@CardRegistration(set = "SLD", collectorNumber = "2292")
@CardRegistration(set = "SLC", collectorNumber = "2007")
@CardRegistration(set = "TSR", collectorNumber = "315")
@CardRegistration(set = "MAR", collectorNumber = "13")
@CardRegistration(set = "OMB", collectorNumber = "13")
@CardRegistration(set = "WHO", collectorNumber = "217")
@CardRegistration(set = "WHO", collectorNumber = "808")
@CardRegistration(set = "MB2", collectorNumber = "34")
@CardRegistration(set = "C21", collectorNumber = "125")
@CardRegistration(set = "NCC", collectorNumber = "229")
@CardRegistration(set = "TDC", collectorNumber = "159")
@CardRegistration(set = "OTC", collectorNumber = "105")
@CardRegistration(set = "C18", collectorNumber = "96")
public class Ponder extends Card {

    public Ponder() {
        addSpellEffect(new ReorderTopCardsOfLibraryEffect(3));
        addSpellEffect(new MayEffect(new ShuffleLibraryEffect(false), "You may shuffle your library."));
        addSpellEffect(new DrawCardEffect(1));
    }
}
