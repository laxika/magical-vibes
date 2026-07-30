package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VigilanteJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("A Human you control entering deals 1 damage to target player")
    void humanEntersDamagesPlayer() {
        harness.addToBattlefield(player1, new VigilanteJustice());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Elite Vanguard (Human Soldier)

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the damage trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A Human you control entering can deal its 1 damage to a creature")
    void humanEntersDamagesCreature() {
        harness.addToBattlefield(player1, new VigilanteJustice());
        harness.addToBattlefield(player2, new SavannahLions());
        UUID lionsId = harness.getPermanentId(player2, "Savannah Lions");

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Elite Vanguard

        harness.handlePermanentChosen(player1, lionsId);
        harness.passBothPriorities(); // resolve the damage trigger

        // Savannah Lions is a 2/1 — 1 damage kills it
        harness.assertNotOnBattlefield(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("A non-Human creature entering does not trigger")
    void nonHumanDoesNotTrigger() {
        harness.addToBattlefield(player1, new VigilanteJustice());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Human entering does not trigger")
    void opponentHumanDoesNotTrigger() {
        harness.addToBattlefield(player1, new VigilanteJustice());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Elite Vanguard

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
