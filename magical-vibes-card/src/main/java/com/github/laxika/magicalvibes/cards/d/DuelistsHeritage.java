package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "153")
public class DuelistsHeritage extends Card {

    public DuelistsHeritage() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new MayEffect(
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET),
                        "Have target attacking creature gain double strike?"));
    }
}
