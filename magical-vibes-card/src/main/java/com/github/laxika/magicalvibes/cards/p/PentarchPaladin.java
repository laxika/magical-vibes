package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "32")
public class PentarchPaladin extends Card {

    public PentarchPaladin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new DestroyTargetPermanentEffect(new PermanentHasSourceChosenColorPredicate())),
                "{W}{W}, {T}: Destroy target permanent of the chosen color.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSourceChosenColorPredicate(),
                        "Target must be a permanent of the chosen color"
                )
        ));
    }
}
