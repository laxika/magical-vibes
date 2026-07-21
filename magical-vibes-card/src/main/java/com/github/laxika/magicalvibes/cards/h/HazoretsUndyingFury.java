package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HazoretsUndyingFuryEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOU", collectorNumber = "96")
public class HazoretsUndyingFury extends Card {

    public HazoretsUndyingFury() {
        // "Lands you control don't untap during your next untap step." Resolved first: the set of
        // lands you control is unchanged by the exile/free-cast below (no permanent enters this
        // resolution), so this ordering is game-state-identical to the printed order while keeping
        // the non-pausing effect ahead of the exile-and-cast effect that begins a player interaction.
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));

        // "Shuffle your library, then exile the top four cards. You may cast any number of spells with
        // mana value 5 or less from among them without paying their mana costs."
        addEffect(EffectSlot.SPELL, new HazoretsUndyingFuryEffect(4, 5));
    }
}
