package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "15")
public class LaunchTheFleet extends Card {

    public LaunchTheFleet() {
        // Strive — This spell costs {1} more to cast for each target beyond the first.
        setAdditionalManaCostPerExtraTarget("{1}");

        // Any number of target creatures each gain "Whenever this creature attacks, create a 1/1
        // white Soldier creature token that's tapped and attacking" until end of turn.
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_ATTACK,
                        new CreateTokenEffect(1, "Soldier", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.SOLDIER), true)));
    }
}
