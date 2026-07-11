package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "21")
public class HoofprintsOfTheStag extends Card {

    public HoofprintsOfTheStag() {
        // Whenever you draw a card, you may put a hoofprint counter on this enchantment.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.HOOFPRINT),
                "Put a hoofprint counter on Hoofprints of the Stag?"
        ));

        // {2}{W}, Remove four hoofprint counters from this enchantment:
        // Create a 4/4 white Elemental creature token with flying. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}",
                List.of(
                        new RemoveCounterFromSourceCost(4, CounterType.HOOFPRINT),
                        new CreateTokenEffect("Elemental", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of())
                ),
                "{2}{W}, Remove four hoofprint counters from this enchantment: Create a 4/4 white Elemental creature token with flying. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
