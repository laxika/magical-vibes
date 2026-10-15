package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireSpawn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodthirstyConqueror.class, GrizzlyBears.class, Shock.class, VampireSpawn.class})
class BloodthirstyConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to spell damage dealt to an opponent")
    void gainsLifeOnSpellDamage() {
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to an opponent")
    void gainsLifeOnCombatDamage() {
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when its controller loses life")
    void doesNotTriggerOnControllerLifeLoss() {
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-damage life loss queues a separate life-gain trigger")
    void gainsLifeOnNonDamageLifeLoss() {
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VampireSpawn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Conqueror triggers for every separate opponent life-loss event")
    void multipleConquerorsTriggerForRepeatedLifeLoss() {
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.addToBattlefield(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Queued life gain resolves after Conqueror leaves the battlefield")
    void queuedTriggerSurvivesSourceLeaving() {
        Permanent conqueror = harness.addToBattlefieldAndReturn(player1, new BloodthirstyConqueror());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, conqueror));
        harness.assertInGraveyard(player1, "Bloodthirsty Conqueror");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }
}
