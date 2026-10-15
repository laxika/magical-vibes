package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "226")
public class HellionCrucible extends Card {

    public HellionCrucible() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}{R}, {T}: Put a pressure counter on Hellion Crucible.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PRESSURE)),
                "{1}{R}, {T}: Put a pressure counter on Hellion Crucible."
        ));

        // {1}{R}, {T}, Remove two pressure counters from Hellion Crucible and sacrifice it:
        // Create a 4/4 red Hellion creature token with haste.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.PRESSURE),
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Hellion", 4, 4, CardColor.RED,
                                List.of(CardSubtype.HELLION), Set.of(Keyword.HASTE), Set.of())
                ),
                "{1}{R}, {T}, Remove two pressure counters from Hellion Crucible and sacrifice it: Create a 4/4 red Hellion creature token with haste."
        ));
    }
}
