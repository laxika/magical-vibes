package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "89")
@CardRegistration(set = "ME1", collectorNumber = "44")
public class PolarKraken extends Card {

    public PolarKraken() {
        // Trample is auto-loaded from Scryfall.
        addStaticEffect(new EntersTappedEffect());
        // Cumulative upkeep—Sacrifice a land.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CumulativeUpkeepEffect.sacrifice(new PermanentIsLandPredicate()));
    }
}
