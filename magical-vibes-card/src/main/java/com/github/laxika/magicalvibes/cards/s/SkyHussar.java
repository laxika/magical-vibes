package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "131")
public class SkyHussar extends Card {

    public SkyHussar() {
        PermanentAllOfPredicate whiteOrBlueCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE, CardColor.BLUE))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, whiteOrBlueCreature),
                        new DrawCardEffect(1)),
                "Forecast \u2014 Tap two untapped white and/or blue creatures you control, Reveal this card from your hand: Draw a card. "
                        + "Activate only during your upkeep and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand().withSourceStaysInHand());
    }
}
