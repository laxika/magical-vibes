package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "113")
@CardRegistration(set = "DRK", collectorNumber = "118")
@CardRegistration(set = "TSB", collectorNumber = "121")
public class SafeHaven extends Card {

    public SafeHaven() {
        // {2}, {T}: Exile target creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{2}, {T}: Exile target creature you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a creature you control")));

        // At the beginning of your upkeep, you may sacrifice this land. If you do, return each
        // card exiled with this land to the battlefield under its owner's control.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new SacrificeSelfThenEffect(new ReturnAllCardsExiledWithSourceEffect()),
                "Sacrifice Safe Haven to return each card exiled with it?"));
    }
}
