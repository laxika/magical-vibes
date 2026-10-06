package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rebuke.class, DarkthicketWolf.class})
class RebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rebuke targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new DarkthicketWolf());
        UUID targetId = harness.getPermanentId(player1, "Darkthicket Wolf");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cannot target a blocking creature")
    void cannotTargetBlockingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Resolving destroys the attacking creature")
    void resolvingDestroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("Rebuke goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Rebuke");
    }

    @Test
    @DisplayName("Rebuke does not destroy a target that stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertNotInGraveyard(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player2, "Rebuke");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rebuke can destroy its controller's own attacking creature")
    void destroysOwnAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new Rebuke()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Rebuke");
    }

    @Test
    @DisplayName("Rebuke fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Rebuke()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Rebuke still goes to graveyard
        harness.assertInGraveyard(player2, "Rebuke");
    }
}
