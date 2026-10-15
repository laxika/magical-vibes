package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "168")
public class ButcherOfTheHorde extends Card {

    public ButcherOfTheHorde() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Vigilance",
                                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Lifelink",
                                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Haste",
                                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF))
                        ))
                ),
                "Sacrifice another creature: This creature gains your choice of vigilance, lifelink, or haste until end of turn."
        ));
    }
}
