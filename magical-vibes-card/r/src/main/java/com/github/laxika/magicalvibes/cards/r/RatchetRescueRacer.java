package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Ratchet, Field Medic. */
public class RatchetRescueRacer extends Card {

    public RatchetRescueRacer() {
        // Living metal: during your turn, this Vehicle is also a creature.
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));

        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new OncePerTurnTriggerEffect(
                        new TriggeringArtifactControllerConditionalEffect(
                                new TransformSelfEffect(), false, true)));
    }
}
