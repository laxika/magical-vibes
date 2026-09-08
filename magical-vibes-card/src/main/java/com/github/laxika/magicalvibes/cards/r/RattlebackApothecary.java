package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "100")
public class RattlebackApothecary extends Card {

    public RattlebackApothecary() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                        new GrantChosenKeywordEffect(List.of(Keyword.MENACE, Keyword.LIFELINK), GrantScope.TARGET));
    }
}
