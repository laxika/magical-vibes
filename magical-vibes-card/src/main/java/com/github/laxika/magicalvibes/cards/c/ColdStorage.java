package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "280")
public class ColdStorage extends Card {

    public ColdStorage() {
        // {3}: Exile target creature you control.
        addActivatedAbility(new ActivatedAbility(
                false, "{3}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{3}: Exile target creature you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a creature you control")));

        // Sacrifice this artifact: Return each creature card exiled with this artifact to the
        // battlefield under your control. Only creatures can be exiled with it, so every tracked
        // card returns.
        addActivatedAbility(new ActivatedAbility(
                false, "{0}",
                List.of(new SacrificeSelfCost(), new ReturnAllCardsExiledWithSourceEffect(true)),
                "Sacrifice Cold Storage: Return each creature card exiled with Cold Storage to the battlefield under your control."));
    }
}
