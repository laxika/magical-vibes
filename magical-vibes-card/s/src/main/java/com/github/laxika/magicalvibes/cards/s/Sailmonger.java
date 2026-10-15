package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "95")
public class Sailmonger extends Card {

    public Sailmonger() {
        // {2}: Target creature gains flying until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{2}: Target creature gains flying until end of turn. Any player may activate this ability.",
                TargetFilters.creature()
        ).withActivatableByAnyPlayer());
    }
}
