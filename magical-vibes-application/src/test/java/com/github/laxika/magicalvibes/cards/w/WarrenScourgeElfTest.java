package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdderStaffBoggart;
import com.github.laxika.magicalvibes.cards.b.BlackPoplarShaman;
import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.m.MudbuttonTorchrunner;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarrenScourgeElf.class, AdderStaffBoggart.class, BlackPoplarShaman.class,
        LowlandOaf.class, WoodlandChangeling.class, MudbuttonTorchrunner.class, Tarfire.class})
class WarrenScourgeElfTest extends BaseCardTest {

    @Test
    @DisplayName("Warren-Scourge Elf takes no combat damage from Goblin creature when blocking")
    void takesNoDamageFromGoblin() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AdderStaffBoggart());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WarrenScourgeElf());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Adder-Staff Boggart's 2 damage is prevented by protection from Goblins — Elf survives
        harness.assertOnBattlefield(player2, "Warren-Scourge Elf");
    }

    @Test
    @DisplayName("Warren-Scourge Elf takes normal combat damage from non-Goblin creature when blocking")
    void takesNormalDamageFromNonGoblin() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WarrenScourgeElf());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Lowland Oaf's 3 damage kills the 1/1 Elf
        harness.assertNotOnBattlefield(player2, "Warren-Scourge Elf");
    }

    @Test
    @DisplayName("Goblin creature cannot block Warren-Scourge Elf")
    void goblinCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WarrenScourgeElf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AdderStaffBoggart());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Goblin creature can block Warren-Scourge Elf")
    void nonGoblinCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WarrenScourgeElf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BlackPoplarShaman());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void goblinSpellCannotTargetElfControlledByEitherPlayer() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new WarrenScourgeElf());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new WarrenScourgeElf());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownElf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opposingElf.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Warren-Scourge Elf");
        harness.assertOnBattlefield(player2, "Warren-Scourge Elf");
    }

    @Test
    void changelingCannotBlockElf() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new WarrenScourgeElf());
        elf.setSummoningSick(false);
        elf.setAttacking(true);
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void preventsCombatDamageFromChangeling() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new WarrenScourgeElf());
        elf.setBlocking(true);
        elf.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Warren-Scourge Elf");
        assertThat(elf.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void deadGoblinCannotTargetElfWithItsTriggeredAbility() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new WarrenScourgeElf());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MudbuttonTorchrunner());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, goblin.getId());

        harness.assertInGraveyard(player1, "Mudbutton Torchrunner");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(elf.getId())
                .contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Warren-Scourge Elf");
    }
}
