package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ZEN", collectorNumber = "124")
public class GeyserGlider extends Card {

    public GeyserGlider() {
        // Landfall — Whenever a land you control enters, this creature gains flying until end of turn.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
