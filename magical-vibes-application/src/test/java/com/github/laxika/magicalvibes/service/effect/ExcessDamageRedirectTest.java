package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * Damage to a creature whose excess is dealt to that creature's controller instead (CR 120.4a): the creature and the
 * controller are dealt their damage in the effect's one damage event, recorded as the resolution's damage step.
 */
@CardUsed({GrizzlyBears.class})
class ExcessDamageRedirectTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private void resolve(UUID targetId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Excess to its controller",
                List.of(effects), targetId, (UUID) null);
        gd.stack.add(entry);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The damage to the creature and the excess to its controller are the resolution's damage step")
    void creatureAndControllerDamageIsTheDamageStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolve(bears.getId(), DealDamageEffect.toTargetCreature(4).excessToControllerInstead(),
                new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Its damage step begins anew: damage an earlier step of the resolution dealt isn't counted")
    void theDamageStepBeginsAnew() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolve(bears.getId(), DealDamageEffect.to(Subjects.eachOpponent(), 1),
                DealDamageEffect.toTargetCreature(4).excessToControllerInstead(),
                new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("The excess, worked out before a prevention, is the event value a later step reads")
    void theExcessIsTheEventValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addPlayerDamagePreventionShield(player2.getId(), 5);

        resolve(bears.getId(), DealDamageEffect.toTargetCreature(5).excessToControllerInstead(),
                new ConditionalEffect(new EventValueAtLeast(3), new GainLifeEffect(7)));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 27);
    }
}
