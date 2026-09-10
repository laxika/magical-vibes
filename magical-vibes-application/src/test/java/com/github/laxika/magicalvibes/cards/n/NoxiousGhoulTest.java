package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousGhoul.class, Gravecrawler.class, GrizzlyBears.class})
class NoxiousGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives non-Zombie creatures -1/-1")
    void ownEntryWeakensNonZombieCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        int zombiePower = gqs.getEffectivePower(gd, ownZombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, ownZombie);

        castNoxiousGhoul();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(zombiePower);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(zombieToughness);
    }

    @Test
    @DisplayName("Another Zombie entering under an opponent's control triggers it")
    void opponentZombieEntryTriggersIt() {
        harness.addToBattlefield(player1, new NoxiousGhoul());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Gravecrawler()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Zombie creature entering does not trigger it")
    void nonZombieEntryDoesNotTriggerIt() {
        harness.addToBattlefield(player1, new NoxiousGhoul());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at the end of the turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNoxiousGhoul();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    private void castNoxiousGhoul() {
        harness.setHand(player1, List.of(new NoxiousGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
