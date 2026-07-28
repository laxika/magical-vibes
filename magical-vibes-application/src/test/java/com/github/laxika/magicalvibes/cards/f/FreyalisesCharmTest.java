package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreyalisesCharmTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new FreyalisesCharm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent black spell: pay {G}{G} to draw a card")
    void opponentBlackSpellPayAndDraw() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new Facevaulter())));
        harness.addMana(player2, ManaColor.BLACK, 5);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Freyalise's Charm"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent black spell: declining to pay draws nothing")
    void opponentBlackSpellDecline() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new Facevaulter())));
        harness.addMana(player2, ManaColor.BLACK, 5);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Non-black opponent spell does not trigger")
    void nonBlackDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Freyalise's Charm"));
    }

    @Test
    @DisplayName("Controller's own black spell does not trigger")
    void ownBlackDoesNotTrigger() {
        harness.addToBattlefield(player1, new FreyalisesCharm());
        harness.setHand(player1, List.of(new Facevaulter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Freyalise's Charm"));
    }

    @Test
    @DisplayName("{G}{G}: return Freyalise's Charm to its owner's hand")
    void bounceAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new FreyalisesCharm());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Freyalise's Charm");
        harness.assertNotOnBattlefield(player1, "Freyalise's Charm");
    }
}
