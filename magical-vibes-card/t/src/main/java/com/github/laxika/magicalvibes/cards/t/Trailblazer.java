package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ICE", collectorNumber = "272")
public class Trailblazer extends Card {

    public Trailblazer() {
        target(TargetFilters.creature()).addSpellEffect(new MakeCreatureUnblockableEffect());
    }
}
