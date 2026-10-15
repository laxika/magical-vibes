package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathstealersCrypt.class, EverybodyLives.class, Impulse.class, Inspiration.class, ManOWar.class})
class BreathstealersCryptTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passUntil(activePlayer, TurnStep.DRAW);
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

    @Test
    @DisplayName("Finish the Crypt choice before drawing the next card of Inspiration")
    void finishesReplacementBeforeNextDraw() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setHand(player1, List.of(new Inspiration()));
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse(), new Impulse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "Man-o'-War");
        harness.assertNotInHand(player1, "Impulse");
        assertThat(gameLogContains("reveals Impulse")).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Man-o'-War");
        harness.assertNotInHand(player1, "Man-o'-War");
        harness.assertInHand(player1, "Impulse");
        assertThat(gameLogContains("reveals Impulse")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Paying for one Crypt does not prevent another Crypt from discarding the creature")
    void payFirstCryptAndDeclineSecond() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse()));

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Man-o'-War");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 17);
        harness.assertNotInHand(player1, "Man-o'-War");
        harness.assertInGraveyard(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Everybody Lives prevents paying life, so drawn creatures must be discarded")
    void cannotKeepCreatureWhenLifeLossIsForbidden() {
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setHand(player1, List.of(new EverybodyLives(), new Inspiration()));
        harness.setLibrary(player1, List.of(new ManOWar(), new Impulse(), new Impulse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Man-o'-War");
        harness.assertInGraveyard(player1, "Man-o'-War");
        harness.assertInHand(player1, "Impulse");
    }
}
