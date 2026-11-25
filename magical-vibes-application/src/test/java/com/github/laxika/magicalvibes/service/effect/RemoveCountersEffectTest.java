package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RemoveCountersEffect} with a subject other than its source, resolved through the engine: "remove a
 * counter from enchanted creature" on an Aura whose activation cost takes it off the battlefield.
 */
@CardUsed({GrizzlyBears.class, Pacifism.class})
class RemoveCountersEffectTest extends BaseCardTest {

    /** An Aura with "Sacrifice this Aura: Remove a +1/+1 counter from enchanted creature." */
    private static Card auraWithSacrificeRemoval() {
        Card aura = new Pacifism();
        aura.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificeSelfCost(false, false),
                        new RemoveCountersEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE)),
                "Sacrifice this Aura: Remove a +1/+1 counter from enchanted creature."));
        return aura;
    }

    @Test
    @DisplayName("Removes the counter from the creature the Aura enchanted before its sacrifice cost was paid")
    void removesFromHostAfterSacrificeCost() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = addCreatureReady(player1, auraWithSacrificeRemoval());
        aura.setAttachedTo(bears.getId());

        harness.activateAbilityOfPermanent(player1, aura, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
