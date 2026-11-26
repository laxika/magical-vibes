package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SLD", collectorNumber = "821")
@CardRegistration(set = "MH1", collectorNumber = "46")
@CardRegistration(set = "MB2", collectorNumber = "160")
public class EchoOfEons extends Card {

    public EchoOfEons() {
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect());
        addSpellEffect(new DrawCardEffect(7, Subjects.eachPlayer()));
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
