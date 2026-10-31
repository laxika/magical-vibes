package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;

@CardRegistration(set = "BOK", collectorNumber = "54")
public class SwayOfTheStars extends Card {

    private static final int NEW_LIFE_TOTAL = 7;
    private static final int CARDS_DRAWN = 7;

    public SwayOfTheStars() {
        // Each player shuffles their hand, graveyard, and all permanents they own into their
        // library, then draws seven cards. Sway of the Stars is still on the stack while this
        // resolves, so it is not shuffled in — it hits the graveyard afterwards as usual.
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect(true));
        addSpellEffect(new EachPlayerDrawsCardEffect(CARDS_DRAWN));

        // Each player's life total becomes 7.
        addSpellEffect(new SetLifeTotalEffect(NEW_LIFE_TOTAL, SetLifeTotalRecipient.EACH_PLAYER));
    }
}
