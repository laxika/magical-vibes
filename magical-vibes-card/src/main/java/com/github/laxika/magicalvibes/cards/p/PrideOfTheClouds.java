package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "125")
public class PrideOfTheClouds extends Card {

    public PrideOfTheClouds() {
        PermanentCount otherFlyingCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                CountScope.ANY_PLAYER,
                true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(otherFlyingCreatures, otherFlyingCreatures));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}",
                List.of(new CreateTokenEffect(1, "Bird", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.BIRD),
                        Set.of(Keyword.FLYING), Set.of())),
                "Forecast — {2}{W}{U}, Reveal this card from your hand: Create a 1/1 white and blue Bird creature token with flying. "
                        + "Activate only during your upkeep and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
