package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "2")
public class AlabasterMage extends Card {

    public AlabasterMage() {
        // {1}{W}: Target creature you control gains lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                "{1}{W}: Target creature you control gains lifelink until end of turn.",
                TargetFilters.creatureYouControl()));
    }
}
