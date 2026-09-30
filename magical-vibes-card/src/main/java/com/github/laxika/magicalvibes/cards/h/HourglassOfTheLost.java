package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "40")
@CardRegistration(set = "M3C", collectorNumber = "92")
public class HourglassOfTheLost extends Card {

    public HourglassOfTheLost() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new PutCountersOnSelfEffect(CounterType.TIME)
                ),
                "{T}: Add {W}. Put a time counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.TIME),
                        new ExileSelfCost(),
                        new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                                new XValue(),
                                true, null, true, false)
                ),
                "{T}, Remove X time counters from this artifact and exile it: Return each nonland permanent card "
                        + "with mana value X from your graveyard to the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
