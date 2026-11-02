package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "255")
@CardRegistration(set = "INR", collectorNumber = "439")
@CardRegistration(set = "VOW", collectorNumber = "253")
@CardRegistration(set = "DBL", collectorNumber = "520")
public class BoardedWindow extends Card {

    public BoardedWindow() {
        // Creatures attacking you get -1/-0.
        addStaticEffect(new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                new PermanentIsAttackingSourceControllerPredicate()));

        // At the beginning of each end step, if you were dealt 4 or more damage this turn,
        // exile this artifact.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new ControllerDealtDamageThisTurn(4),
                new ExileSelfEffect()));
    }
}
