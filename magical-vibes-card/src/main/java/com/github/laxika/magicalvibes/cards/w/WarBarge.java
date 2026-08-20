package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyTargetWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "115")
public class WarBarge extends Card {

    public WarBarge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.TARGET),
                        new RegisterDelayedDestroyTargetWhenSourceLeavesEffect()),
                "{3}: Target creature gains islandwalk until end of turn. When this artifact leaves the "
                        + "battlefield this turn, destroy that creature. A creature destroyed this way "
                        + "can't be regenerated.",
                TargetFilters.creature()));
    }
}
