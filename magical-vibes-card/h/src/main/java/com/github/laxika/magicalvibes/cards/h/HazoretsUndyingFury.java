package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.HazoretsUndyingFuryEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOU", collectorNumber = "96")
public class HazoretsUndyingFury extends Card {

    public HazoretsUndyingFury() {
        // "Lands you control don't untap during your next untap step." Matched at that untap step, so
        // lands that enter or come under your control later are also affected (2017-07-14 ruling).
        // Resolved first because it can't change what the exile-and-cast effect below sees.
        addSpellEffect(new SkipNextUntapEffect(
                TapUntapScope.CONTROLLED, new PermanentIsLandPredicate(), 1, true));

        // "Shuffle your library, then exile the top four cards. You may cast any number of spells with
        // mana value 5 or less from among them without paying their mana costs."
        addSpellEffect(new HazoretsUndyingFuryEffect(4, 5));
    }
}
