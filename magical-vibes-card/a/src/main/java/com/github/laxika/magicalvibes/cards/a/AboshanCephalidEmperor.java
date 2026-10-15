package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "58")
public class AboshanCephalidEmperor extends Card {

    public AboshanCephalidEmperor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.OCTOPUS)),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "Tap an untapped Octopus you control: Tap target permanent.",
                TargetFilters.permanent()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}{U}",
                List.of(new TapPermanentsEffect(
                        TapUntapScope.ALL_CREATURES,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                "{U}{U}{U}: Tap all creatures without flying."));
    }
}
