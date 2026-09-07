package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithoutLegendaryCreatureOrPlaneswalkerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesPermanentsOrCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLoyaltyAbilitiesOfOtherPlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "207")
public class NicolBolasDragonGod extends Card {

    public NicolBolasDragonGod() {
        addEffect(EffectSlot.STATIC,
                new GainLoyaltyAbilitiesOfOtherPlaneswalkersEffect());

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1),
                        EachPlayerExilesPermanentsOrCardsFromHandEffect.opponents(
                                new Fixed(1))),
                "+1: You draw a card. Each opponent exiles a card from their hand or a permanent "
                        + "they control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "−3: Destroy target creature or planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate()
                        )),
                        "Target must be a creature or planeswalker"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new EachOpponentWithoutLegendaryCreatureOrPlaneswalkerLosesGameEffect()),
                "−8: Each opponent who doesn't control a legendary creature or planeswalker loses the game."
        ));
    }
}
