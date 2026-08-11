package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "50")
public class Glamermite extends Card {

    public Glamermite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target creature",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature"
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target creature",
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature"
                        )
                )
        )));
    }
}
