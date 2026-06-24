package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "151")
public class Helvault extends Card {

    public Helvault() {
        // {1}, {T}: Exile target creature you control.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{1}, {T}: Exile target creature you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())),
                        "Target must be a creature you control")));

        // {7}, {T}: Exile target creature you don't control.
        addActivatedAbility(new ActivatedAbility(
                true, "{7}",
                List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                "{7}, {T}: Exile target creature you don't control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature you don't control")));

        // When Helvault is put into a graveyard from the battlefield, return all cards
        // exiled with it to the battlefield under their owners' control.
        addEffect(EffectSlot.ON_DEATH, new ReturnAllCardsExiledWithSourceEffect());
    }
}
