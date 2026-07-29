package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuspiciousAncestorTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Auspicious Ancestor dies, its controller gains 3 life")
    void diesGainsThreeLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Auspicious Ancestor"));
        harness.passBothPriorities(); // Terror resolves — Ancestor dies, death trigger on the stack
        harness.passBothPriorities(); // death trigger resolves

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("Opponent's white spell: paying {1} gains 1 life")
    void payGainsOneLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Declining to pay {1} gains no life")
    void declineGainsNoLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("A nonwhite spell does not trigger the ability")
    void nonwhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own white spell triggers the ability too")
    void ownWhiteSpellTriggers() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }
}
