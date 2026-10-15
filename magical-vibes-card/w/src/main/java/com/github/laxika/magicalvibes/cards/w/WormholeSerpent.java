package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "62")
public class WormholeSerpent extends Card {

    public WormholeSerpent() {
        // {3}{U}: Target creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{3}{U}: Target creature can't be blocked this turn.",
                TargetFilters.creature()));
    }
}
