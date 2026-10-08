package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlpoolWhelm.class, GoldmeadowDodger.class, Forest.class})
class WhirlpoolWhelmTest extends BaseCardTest {

    private UUID prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player2, new GoldmeadowDodger()); // the bounce target
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}

        return harness.getPermanentId(player2, "Goldmeadow Dodger");
    }

    // Caster (player1) wins the clash: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GoldmeadowDodger(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GoldmeadowDodger(), new Forest(), new Forest()));
    }

    // Neither player wins when the revealed cards have the same mana value.
    private void stackClashTie() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    private void keepRevealedCardsOnTop() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Winning the clash and accepting puts the creature on top of its owner's library")
    void wonClashAcceptPutsOnTopOfLibrary() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCardsOnTop();

        // Won clash → controller is offered the "put on top instead" choice.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertNotInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Winning the clash and declining returns the creature to its owner's hand")
    void wonClashDeclineReturnsToHand() {
        UUID targetId = prepare();
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isNotEqualTo("Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Losing the clash returns the creature to its owner's hand with no choice offered")
    void lostClashReturnsToHand() {
        UUID targetId = prepare();
        stackClashLossForCaster();

        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCardsOnTop();

        // No "put on top" choice on a loss — the creature simply goes to hand.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Tied clash returns the creature to its owner's hand with no choice offered")
    void tiedClashReturnsToHand() {
        UUID targetId = prepare();
        stackClashTie();

        harness.castAndResolveInstant(player1, 0, targetId);
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GoldmeadowDodger()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WhirlpoolWhelm()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID landId = harness.getPermanentId(player2, "Forest");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Both clash placements finish before the caster chooses the creature's destination")
    void bottomingRevealedCardsDoesNotChangeClashWinner() {
        UUID targetId = prepare();
        GoldmeadowDodger casterReveal = new GoldmeadowDodger();
        Forest opponentReveal = new Forest();
        WhirlpoolWhelm opponentNext = new WhirlpoolWhelm();
        harness.setLibrary(player1, List.of(casterReveal, new Forest()));
        harness.setLibrary(player2, List.of(opponentReveal, opponentNext));

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertOnBattlefield(player2, "Goldmeadow Dodger");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(casterReveal);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.assertOnBattlefield(player2, "Goldmeadow Dodger");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(opponentReveal);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(casterReveal);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentReveal);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertNotInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Goldmeadow Dodger", "Whirlpool Whelm", "Forest");
        harness.assertInGraveyard(player1, "Whirlpool Whelm");
    }

    @Test
    @DisplayName("The caster can return their own creature")
    void canReturnOwnCreature() {
        prepare();
        harness.addToBattlefield(player1, new GoldmeadowDodger());
        UUID ownTarget = harness.getPermanentId(player1, "Goldmeadow Dodger");
        stackClashLossForCaster();

        harness.castAndResolveInstant(player1, 0, ownTarget);
        keepRevealedCardsOnTop();

        harness.assertNotOnBattlefield(player1, "Goldmeadow Dodger");
        harness.assertInHand(player1, "Goldmeadow Dodger");
        harness.assertOnBattlefield(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("An illegal target prevents the clash from happening")
    void missingTargetPreventsClash() {
        UUID targetId = prepare();
        stackClashWinForCaster();
        List<?> casterLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<?> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEqualTo(casterLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).isEqualTo(opponentLibrary);
        harness.assertInGraveyard(player1, "Whirlpool Whelm");
        harness.assertNotInHand(player2, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("A caster with an empty library does not win the clash")
    void emptyCasterLibraryReturnsCreatureToHand() {
        UUID targetId = prepare();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castAndResolveInstant(player1, 0, targetId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Goldmeadow Dodger");
        harness.assertInHand(player2, "Goldmeadow Dodger");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

}
