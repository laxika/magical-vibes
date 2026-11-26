package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "75")
@CardRegistration(set = "M12", collectorNumber = "77")
@CardRegistration(set = "SLD", collectorNumber = "1499")
public class TimeReversal extends Card {

    public TimeReversal() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect());
        addSpellEffect(new DrawCardEffect(7, Subjects.eachPlayer()));

        // Exile Time Reversal.
        addSpellEffect(new ExileSpellEffect());
    }
}
