package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "176")
public class EndlessSands extends Card {

    public EndlessSands() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {2}, {T}: Exile target creature you control.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{2}, {T}: Exile target creature you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a creature you control")));

        // {4}, {T}, Sacrifice this land: Return each creature card exiled with this land to the
        // battlefield under its owner's control.
        addActivatedAbility(new ActivatedAbility(
                true, "{4}",
                List.of(new SacrificeSelfCost(), new ReturnAllCardsExiledWithSourceEffect()),
                "{4}, {T}, Sacrifice this land: Return each creature card exiled with this land to the battlefield under its owner's control."
        ));
    }
}
