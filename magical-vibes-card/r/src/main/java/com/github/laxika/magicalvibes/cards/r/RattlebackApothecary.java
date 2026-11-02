package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "100")
public class RattlebackApothecary extends Card {

    public RattlebackApothecary() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youCommitCrime(),
                        new GrantChosenKeywordEffect(List.of(Keyword.MENACE, Keyword.LIFELINK), GrantScope.TARGET));
    }
}
