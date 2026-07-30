package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "218")
public class SundialOfTheInfinite extends Card {

    public SundialOfTheInfinite() {
        // {1}, {T}: End the turn. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new EndTurnEffect()),
                "{1}, {T}: End the turn. Activate only during your turn.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
