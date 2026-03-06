package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "33")
public class DeceiverExarch extends Card {

    public DeceiverExarch() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Untap target permanent you control",
                        new UntapTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentControlledBySourceControllerPredicate(),
                                "Target must be a permanent you control"
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target permanent an opponent controls",
                        new TapTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                "Target must be a permanent an opponent controls"
                        )
                )
        )));
    }
}
