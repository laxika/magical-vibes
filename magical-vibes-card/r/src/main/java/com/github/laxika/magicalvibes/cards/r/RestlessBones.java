package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "58")
public class RestlessBones extends Card {

    public RestlessBones() {
        // {3}{B}, {T}: Target creature gains swampwalk until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{B}",
                List.of(new GrantKeywordEffect(Keyword.SWAMPWALK, GrantScope.TARGET)),
                "{3}{B}, {T}: Target creature gains swampwalk until end of turn.",
                TargetFilters.creature()));

        // {1}{B}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Restless Bones."));
    }
}
