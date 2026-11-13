package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "77")
public class ZenithSeeker extends Card {

    public ZenithSeeker() {
        // Flying is auto-loaded from Scryfall.
        // Whenever you cycle or discard a card, target creature gains flying until end of turn.
        // Cycling discards the card (CR 702.29e), so the single "controller discards" trigger covers
        // both wordings. The GrantScope.TARGET filter (creatures only) drives the trigger's target,
        // chosen as the stack door puts the ability on the stack; no cast-time target() is declared,
        // so casting Zenith Seeker itself never prompts for a target.
        addTrigger(Triggers.youDiscardCard(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, new PermanentIsCreaturePredicate()));
    }
}
