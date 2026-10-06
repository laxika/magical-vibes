package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapdiverSerpent.class, MoriokReaver.class, CopperMyr.class, AccordersShield.class})
class ScrapdiverSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Scrapdiver Serpent can't be blocked when defending player controls an artifact")
    void cantBeBlockedWhenDefenderControlsArtifact() {
        // Defender controls an artifact (CopperMyr)
        harness.addToBattlefield(player2, new CopperMyr());

        // Defender also has a creature that could block
        harness.addToBattlefield(player2, new MoriokReaver());

        // Serpent is attacking
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Attempting to block the Serpent should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Scrapdiver Serpent can be blocked when defending player controls no artifacts")
    void canBeBlockedWhenDefenderControlsNoArtifact() {
        harness.setLife(player2, 20);

        // Defender has only a non-artifact creature
        harness.addToBattlefield(player2, new MoriokReaver());

        // Serpent is attacking
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Blocking should succeed — no artifacts on defender's side
        // MoriokReaver (3/2) blocks and dies to Serpent (5/5), but Serpent is blocked so no player damage
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Serpent was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Scrapdiver Serpent deals 5 damage")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("A tapped noncreature artifact also prevents blocking")
    void cantBeBlockedWhenDefenderControlsTappedEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        equipment.tap();
        harness.addToBattlefield(player2, new MoriokReaver());
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("An artifact controlled only by the attacking player does not prevent blocking")
    void canBeBlockedWhenOnlyAttackerControlsArtifact() {
        harness.setLife(player2, 20);
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addToBattlefield(player2, new MoriokReaver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.assertLife(player2, 20);
    }
}
