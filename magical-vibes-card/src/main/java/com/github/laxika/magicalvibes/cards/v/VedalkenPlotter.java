package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "41")
public class VedalkenPlotter extends Card {

    public VedalkenPlotter() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "First target must be a land you control"));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentIsLandPredicate())),
                "Second target must be a land an opponent controls"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExchangeControlOfTargetPermanentsEffect(
                        new PermanentIsLandPredicate(), false));
    }
}
