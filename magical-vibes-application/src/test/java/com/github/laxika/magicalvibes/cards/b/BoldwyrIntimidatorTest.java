package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoldwyrIntimidatorTest extends BaseCardTest {

    // ===== Static: Cowards can't block Warriors =====

    @Test
    @DisplayName("A Coward can't block a Warrior while Boldwyr Intimidator is on the battlefield")
    void cowardCannotBlockWarrior() {
        Permanent boldwyr = addReadyBoldwyr(player1);
        boldwyr.setAttacking(true); // Boldwyr is a Warrior

        Permanent blocker = addReadyCreature(player2);
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("A non-Coward creature can still block the Warrior Boldwyr")
    void nonCowardCanBlock() {
        Permanent boldwyr = addReadyBoldwyr(player1);
        boldwyr.setAttacking(true);

        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== {R}: Target creature becomes a Coward until end of turn =====

    @Test
    @DisplayName("{R} makes a target creature a Coward, which then can't block the Warrior Boldwyr")
    void cowardAbilityStopsBlock() {
        Permanent boldwyr = addReadyBoldwyr(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        boldwyr.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("The Coward type wears off at end of turn, restoring the ability to block")
    void cowardWearsOff() {
        Permanent boldwyr = addReadyBoldwyr(player1);
        boldwyr.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        blocker.resetModifiers(); // end-of-turn cleanup

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== {2}{R}: Target creature becomes a Warrior until end of turn =====

    @Test
    @DisplayName("{2}{R} makes an attacker a Warrior, which a Coward then can't block")
    void warriorAbilityBlocksCoward() {
        // Boldwyr (index 0) supplies the static; the attacker (index 1) is a made-Warrior bear.
        addReadyBoldwyr(player1);
        Permanent attacker = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    // ===== Targeting =====

    @Test
    @DisplayName("The type-changing abilities can only target creatures")
    void cannotTargetNonCreature() {
        addReadyBoldwyr(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, landId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyBoldwyr(Player player) {
        Permanent perm = new Permanent(new BoldwyrIntimidator());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
