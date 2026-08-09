package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "138")
public class PatronOfTheOrochi extends Card {

    public PatronOfTheOrochi() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{6}{G}{G}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.SNAKE))
        )));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.ALL_PERMANENTS,
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN)))
                ),
                "{T}: Untap all Forests and all green creatures. Activate only once each turn.", 1));
    }
}
