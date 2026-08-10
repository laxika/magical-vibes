package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "252")
public class SynodSanctum extends Card {

    public SynodSanctum() {
        // {2}, {T}: Exile target permanent you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{2}, {T}: Exile target permanent you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you control")));

        // {2}, Sacrifice this artifact: Return all cards exiled with this artifact to the
        // battlefield under your control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new ReturnAllCardsExiledWithSourceEffect(true)),
                "{2}, Sacrifice this artifact: Return all cards exiled with this artifact to the battlefield under your control."));
    }
}
