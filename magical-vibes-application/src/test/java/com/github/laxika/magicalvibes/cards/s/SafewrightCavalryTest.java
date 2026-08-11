package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafewrightCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Safewright Cavalry can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Safewright Cavalry cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addAttacker();
        addBlocker();
        addBlocker();

        assertThatThrownBy(() -> declareBlockers(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Ability gives a target Elf you control +2/+2 until end of turn")
    void boostsTargetElfYouControl() {
        addSafewrightReady();
        harness.addToBattlefield(player1, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(elf.getEffectivePower()).isEqualTo(3);
        assertThat(elf.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability cannot target an Elf an opponent controls")
    void rejectsOpponentElfTarget() {
        addSafewrightReady();
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a non-Elf creature")
    void rejectsNonElfTarget() {
        addSafewrightReady();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addSafewrightReady();
        harness.addToBattlefield(player1, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(elf.getEffectivePower()).isEqualTo(1);
        assertThat(elf.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new SafewrightCavalry());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
    }

    private void addSafewrightReady() {
        harness.addToBattlefield(player1, new SafewrightCavalry());
        findPermanent(player1, "Safewright Cavalry").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 5);
    }
}
