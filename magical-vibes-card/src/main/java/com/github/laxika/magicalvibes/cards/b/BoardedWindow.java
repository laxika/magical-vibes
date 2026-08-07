package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

@CardRegistration(set = "INR", collectorNumber = "255")
@CardRegistration(set = "INR", collectorNumber = "439")
public class BoardedWindow extends Card {

    public BoardedWindow() {
        // Creatures attacking you get -1/-0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                new PermanentIsAttackingSourceControllerPredicate()));

        // At the beginning of each end step, if you were dealt 4 or more damage this turn,
        // exile this artifact.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerDealtDamageThisTurn(4),
                new ExileSelfEffect()));
    }
}
