package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AuntiesSnitch;
import com.github.laxika.magicalvibes.cards.e.EarwigSquad;
import com.github.laxika.magicalvibes.cards.f.FrogtosserBanneret;
import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NogginWhack.class, AuntiesSnitch.class, EarwigSquad.class,
        FrogtosserBanneret.class, MudbuttonClanger.class})
class NogginWhackTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    @Test
    @DisplayName("Controller chooses two of the three revealed cards to discard")
    void controllerDiscardsTwoRevealed() {
        Card snitch = new AuntiesSnitch();
        Card squad = new EarwigSquad();
        Card banneret = new FrogtosserBanneret();
        Card clanger = new MudbuttonClanger();
        harness.setHand(player2, new ArrayList<>(List.of(snitch, squad, banneret, clanger)));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Target reveals Auntie's Snitch (0), Earwig Squad (1), Frogtosser Banneret (2) — Mudbutton Clanger stays hidden.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);

        // Controller must pick two of the three revealed cards.
        PendingInteraction.RevealCardsDiscardChoice discardChoice = activeChoice();
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.revealStage()).isFalse();
        assertThat(discardChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);
        assertThat(discardChoice.validIndices()).containsExactly(0, 1, 2);

        // Discard the first revealed card (Auntie's Snitch); still one more to choose.
        harness.handleCardChosen(player1, 0);
        PendingInteraction.RevealCardsDiscardChoice second = activeChoice();
        assertThat(second).isNotNull();
        assertThat(second.remainingCount()).isEqualTo(1);
        assertThat(second.revealedCardIds()).hasSize(2);

        // Discard Frogtosser Banneret (index 1 of the two remaining revealed cards).
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Auntie's Snitch", "Frogtosser Banneret");
        // Earwig Squad (revealed, not chosen) and the hidden Mudbutton Clanger remain in hand.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Earwig Squad", "Mudbutton Clanger");
    }

    @Test
    @DisplayName("With exactly two cards the whole hand is revealed and both are discarded")
    void wholeHandOfTwoBothDiscarded() {
        harness.setHand(player2, new ArrayList<>(List.of(new AuntiesSnitch(), new EarwigSquad())));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Straight to the controller's discard choice over the whole (two-card) hand.
        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Auntie's Snitch", "Earwig Squad");
    }

    @Test
    @DisplayName("With a single card only that card is discarded (fewer than the discard count)")
    void singleCardDiscardsOnlyOne() {
        harness.setHand(player2, new ArrayList<>(List.of(new AuntiesSnitch())));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Auntie's Snitch");
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Prowl can cast Noggin Whack for {1}{B} after Rogue combat damage")
    void prowlCastAfterRogueCombatDamage() {
        var attacker = addCreatureReady(player1, new AuntiesSnitch());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithProwl(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Noggin Whack");
    }

    @Test
    @DisplayName("Prowl is unavailable after combat damage from a non-Rogue")
    void prowlUnavailableAfterNonRogueCombatDamage() {
        var attacker = addCreatureReady(player1, new MudbuttonClanger());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prowl");
    }

    @Test
    @DisplayName("Both discard choices are made before either card leaves the hand")
    void choosesBothCardsBeforeDiscarding() {
        Card snitch = new AuntiesSnitch();
        Card squad = new EarwigSquad();
        Card banneret = new FrogtosserBanneret();
        harness.setHand(player2, List.of(snitch, squad, banneret));
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(snitch, squad, banneret);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(banneret);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(snitch, squad);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster may target themselves and chooses two of their revealed cards")
    void canTargetSelf() {
        Card snitch = new AuntiesSnitch();
        Card squad = new EarwigSquad();
        Card banneret = new FrogtosserBanneret();
        harness.setHand(player1, List.of(new NogginWhack(), snitch, squad, banneret));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(banneret);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch, squad);
        harness.assertInGraveyard(player1, "Noggin Whack");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Prowl cannot be used before any qualifying combat damage")
    void prowlUnavailableWithoutCombatDamage() {
        harness.setHand(player1, List.of(new NogginWhack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prowl");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
