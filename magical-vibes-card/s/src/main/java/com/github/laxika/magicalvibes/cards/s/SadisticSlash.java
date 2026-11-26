package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "666")
public class SadisticSlash extends Card {

    public SadisticSlash() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-5, -5));
        addCastingOption(new GraveyardCast(null, "{1}{B}", List.of(), new CardDiscardedThisTurn()));
    }
}
