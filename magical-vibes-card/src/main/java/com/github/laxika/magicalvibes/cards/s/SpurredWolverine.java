package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "237")
public class SpurredWolverine extends Card {

    public SpurredWolverine() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.BEAST)),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "Tap two untapped Beasts you control: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));
    }
}
