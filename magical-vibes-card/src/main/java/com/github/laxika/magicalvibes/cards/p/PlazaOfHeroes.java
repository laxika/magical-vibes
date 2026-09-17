package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "252")
public class PlazaOfHeroes extends Card {

    public PlazaOfHeroes() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS,
                        new ManaRestriction.LegendarySpells()
                )),
                "{T}: Add one mana of any color. Spend this mana only to cast a legendary spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsAmongControlledEffect(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                )),
                "{T}: Add one mana of any color among legendary permanents you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new ExileSelfCost(),
                        new GrantKeywordEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                                GrantScope.TARGET
                        )
                ),
                "{3}, {T}, Exile this land: Target legendary creature gains hexproof and indestructible until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                        )),
                        "Target must be a legendary creature"
                )
        ));
    }
}
