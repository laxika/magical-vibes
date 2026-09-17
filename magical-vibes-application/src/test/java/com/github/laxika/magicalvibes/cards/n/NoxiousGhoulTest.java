package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.k.KeeneyeAven;
import com.github.laxika.magicalvibes.cards.w.WitheredWretch;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousGhoul.class, WitheredWretch.class, AvenEnvoy.class, KeeneyeAven.class})
class NoxiousGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives non-Zombie creatures -1/-1")
    void ownEntryWeakensNonZombieCreatures() {
        Permanent ownBird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opposingBird = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new WitheredWretch());
        int zombiePower = gqs.getEffectivePower(gd, ownZombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, ownZombie);

        castNoxiousGhoul();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(zombiePower);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(zombieToughness);
    }

    @Test
    @DisplayName("Another Zombie entering under an opponent's control triggers it")
    void opponentZombieEntryTriggersIt() {
        harness.addToBattlefield(player1, new NoxiousGhoul());
        Permanent ownBird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opposingBird = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WitheredWretch(), "{B}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Zombie creature entering does not trigger it")
    void nonZombieEntryDoesNotTriggerIt() {
        harness.addToBattlefield(player1, new NoxiousGhoul());
        Permanent ownBird = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opposingBird = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AvenEnvoy(), "{U}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Noxious Ghoul triggers for the same Zombie entry")
    void eachNoxiousGhoulTriggersForAnotherZombieEntry() {
        harness.addToBattlefield(player1, new NoxiousGhoul());
        harness.addToBattlefield(player2, new NoxiousGhoul());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new KeeneyeAven());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WitheredWretch(), "{B}{B}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at the end of the turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent opposingBird = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        castNoxiousGhoul();
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBird)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opposingBird)).isEqualTo(2);
    }

    private void castNoxiousGhoul() {
        harness.castFromHand(player1, new NoxiousGhoul(), "{3}{B}{B}");
    }
}
