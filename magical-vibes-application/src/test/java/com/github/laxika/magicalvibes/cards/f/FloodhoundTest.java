package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Floodhound.class})
class FloodhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three mana and taps to investigate")
    void paysThreeManaAndTapsToInvestigate() {
        Permanent floodhound = addCreatureReady(player1, new Floodhound());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(floodhound.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent floodhound = addCreatureReady(player1, new Floodhound());
        floodhound.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot investigate while summoning sick")
    void cannotInvestigateWhileSummoningSick() {
        Permanent floodhound = harness.addToBattlefieldAndReturn(player1, new Floodhound());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(floodhound.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot investigate with only two mana")
    void cannotInvestigateWithOnlyTwoMana() {
        Permanent floodhound = addCreatureReady(player1, new Floodhound());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(floodhound.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can investigate during the opponent's turn")
    void canInvestigateDuringOpponentsTurn() {
        Permanent floodhound = addCreatureReady(player1, new Floodhound());
        prepareMainPhase(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(floodhound.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Generated Clue can be sacrificed immediately for two mana to draw a card")
    void generatedClueCanBeSacrificedToDraw() {
        addCreatureReady(player1, new Floodhound());
        prepareMainPhase(player1);
        Floodhound drawnCard = new Floodhound();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
