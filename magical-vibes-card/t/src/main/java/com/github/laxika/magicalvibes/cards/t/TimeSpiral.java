package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "USG", collectorNumber = "103")
public class TimeSpiral extends Card {

    public TimeSpiral() {
        // Exile Time Spiral.
        addSpellEffect(ExileSpellEffect.immediately());

        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect());
        addSpellEffect(new EachPlayerDrawsCardEffect(7));

        // Untap up to six lands.
        addSpellEffect(new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS, new PermanentIsLandPredicate(), 6));
    }
}
