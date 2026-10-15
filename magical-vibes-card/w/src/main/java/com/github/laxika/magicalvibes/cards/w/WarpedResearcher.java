package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "56")
public class WarpedResearcher extends Card {

    public WarpedResearcher() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CYCLES,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.SHROUD), GrantScope.SELF));
    }
}
