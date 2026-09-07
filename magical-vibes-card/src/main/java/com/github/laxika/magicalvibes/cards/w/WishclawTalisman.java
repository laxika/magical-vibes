package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "110")
public class WishclawTalisman extends Card {

    public WishclawTalisman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.WISH, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.WISH),
                        new SearchLibraryEffect(),
                        new ChooseOpponentGainsControlOfSourceEffect()
                ),
                "{1}, {T}, Remove a wish counter from this artifact: Search your library for a card, "
                        + "put it into your hand, then shuffle. An opponent gains control of this artifact. "
                        + "Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
