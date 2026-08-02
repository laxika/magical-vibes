package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SirensCallEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "143")
public class MaddeningImp extends Card {

    public MaddeningImp() {
        // {T}: Non-Wall creatures the active player controls attack this turn if able. At the
        // beginning of the next end step, destroy each of those creatures that didn't attack this
        // turn. Activate only during an opponent's turn and only before combat.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SirensCallEffect()),
                "{T}: Non-Wall creatures the active player controls attack this turn if able. At the beginning of the next end step, destroy each of those creatures that didn't attack this turn. Activate only during an opponent's turn and only before combat.",
                ActivationTimingRestriction.ONLY_DURING_OPPONENTS_TURN_BEFORE_COMBAT));
    }
}
