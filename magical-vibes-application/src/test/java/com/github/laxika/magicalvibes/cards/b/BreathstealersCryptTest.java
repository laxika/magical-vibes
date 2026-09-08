package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathstealersCrypt.class, Impulse.class, ManOWar.class})
class BreathstealersCryptTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Drawing a creature prompts pay 3 life or discard; paying keeps the card")
    void payLifeToKeepDrawnCreature() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gameLogContains("reveals Man-o'-War")).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Man-o'-War");
        harness.assertNotInGraveyard(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Declining the payment discards the drawn creature")
    void declineDiscardsDrawnCreature() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Man-o'-War");
        harness.assertInGraveyard(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Non-creature draws are revealed but kept with no payment prompt")
    void nonCreatureIsKeptWithoutPrompt() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new Impulse(), new ManOWar()));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Impulse")).isTrue();
        harness.assertInHand(player1, "Impulse");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can't pay 3 life auto-discards the drawn creature with no prompt")
    void cannotPayAutoDiscards() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));
        harness.setLife(player1, 2);

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 2);
        harness.assertNotInHand(player1, "Man-o'-War");
        harness.assertInGraveyard(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Opponent's draws are also revealed and subject to the creature discard")
    void affectsOpponentDraws() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player2, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotInHand(player2, "Man-o'-War");
        harness.assertInGraveyard(player2, "Man-o'-War");
    }

    @Test
    @DisplayName("Paying to keep a creature draw counts as life loss")
    void payingToKeepDrawnCreatureCountsAsLifeLoss() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.lifeLostThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Breathstealer's Crypt applies to a creature draw")
    void multipleCryptsEachApplyToCreatureDraw() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, "Man-o'-War");
        harness.assertNotInGraveyard(player1, "Man-o'-War");
    }
}
