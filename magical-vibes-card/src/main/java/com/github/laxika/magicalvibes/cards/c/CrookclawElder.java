package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "34")
public class CrookclawElder extends Card {

    public CrookclawElder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.BIRD)),
                        new DrawCardEffect()
                ),
                "Tap two untapped Birds you control: Draw a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                ),
                "Tap two untapped Wizards you control: Target creature gains flying until end of turn.",
                TargetFilters.creature()
        ));
    }
}
