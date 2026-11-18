package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitualOfHope.class, CrawWurm.class, GrizzlyBears.class, LlanowarElves.class})
class RitualOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Without coven, creatures you control get +1/+1")
    void givesBaseBoostWithoutCoven() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castForMana();

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherOwnCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("With coven, creatures you control get +2/+1 instead")
    void givesCovenBoostInsteadOfBaseBoost() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        castForMana();

        assertThat(PermanentGrants.powerBoostOf(gd, elf)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, elf)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, wurm)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wurm)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castForMana();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isZero();
    }

    @Test
    @DisplayName("Three creatures with only two distinct powers do not enable coven")
    void duplicatePowersDoNotEnableCoven() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        harness.addToBattlefield(player2, new LlanowarElves());

        castForMana();

        for (Permanent creature : List.of(firstBears, secondBears, wurm)) {
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(1);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Additional creatures with duplicate powers do not prevent coven")
    void duplicatesDoNotPreventCoven() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        castForMana();

        for (Permanent creature : List.of(elf, firstBears, secondBears, wurm)) {
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Coven uses power including counters")
    void covenUsesEffectivePower() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        castForMana();

        for (Permanent creature : List.of(firstBears, secondBears, wurm)) {
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Coven gained before resolution upgrades the boost")
    void covenIsCheckedAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        harness.setHand(player1, List.of(new RitualOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.passBothPriorities();

        for (Permanent creature : List.of(elf, bears, wurm)) {
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesAreNotBoosted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castForMana();
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, elf)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, elf)).isZero();
    }

    private void castForMana() {
        harness.setHand(player1, List.of(new RitualOfHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
