package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidWinnower.class, GrizzlyBears.class, LlanowarElves.class, Hurricane.class})
class VoidWinnowerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot cast even-mana-value spells")
    void opponentCannotCastEvenManaValueSpell() {
        harness.addToBattlefield(player1, new VoidWinnower());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Odd-mana-value spells remain castable")
    void oddManaValueSpellRemainsCastable() {
        harness.addToBattlefield(player1, new VoidWinnower());
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        prepareMainPhase(player2);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The controller can cast even-mana-value spells")
    void controllerCanCastEvenManaValueSpell() {
        harness.addToBattlefield(player1, new VoidWinnower());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The chosen X value determines whether an X spell has even mana value")
    void chosenXDeterminesManaValueParity() {
        harness.addToBattlefield(player1, new VoidWinnower());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        prepareMainPhase(player2);

        harness.castSorcery(player2, 0, 2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponents cannot block with even-mana-value creatures")
    void opponentEvenManaValueCreatureCannotBlock() {
        harness.addToBattlefield(player1, new VoidWinnower());
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("even mana values");
    }

    @Test
    @DisplayName("Odd-mana-value creatures can block")
    void oddManaValueCreatureCanBlock() {
        harness.addToBattlefield(player1, new VoidWinnower());
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LlanowarElves());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("declares 1 blocker"));
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    protected void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
