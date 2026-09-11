package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "133")
@CardRegistration(set = "DD1", collectorNumber = "17")
@CardRegistration(set = "EVG", collectorNumber = "17")
public class WirewoodSymbiote extends Card {

    public WirewoodSymbiote() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ReturnMultiplePermanentsToHandCost(
                                1, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                        new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "Return an Elf you control to its owner's hand: Untap target creature. "
                        + "Activate only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                null));
    }
}
