package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerAttackedBySubtypeControllerControlledThisTurn;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1560")
@CardRegistration(set = "DMC", collectorNumber = "39")
@CardRegistration(set = "DMC", collectorNumber = "61")
public class RamsesAssassinLord extends Card {

    public RamsesAssassinLord() {
        addStaticEffect(new StaticBoostEffect(
                1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)));
        addTrigger(Triggers.playerLosesGame(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype.ASSASSIN)),
                new WinGameEffect());
    }
}
