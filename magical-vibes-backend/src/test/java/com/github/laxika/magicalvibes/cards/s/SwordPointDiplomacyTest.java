package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOpponentPaysLifeOrToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwordPointDiplomacyTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effect")
    void hasCorrectEffect() {
        SwordPointDiplomacy card = new SwordPointDiplomacy();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(RevealTopCardsOpponentPaysLifeOrToHandEffect.class);
        RevealTopCardsOpponentPaysLifeOrToHandEffect effect =
                (RevealTopCardsOpponentPaysLifeOrToHandEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(3);
        assertThat(effect.lifeCost()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting and resolving reveals top 3 and presents opponent with choice")
    void castingPresentsOpponentWithChoice() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
    }

    @Test
    @DisplayName("Opponent denies no cards — all 3 go to controller's hand")
    void opponentDeniesNone() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent selects no cards to deny
        harness.handleMultipleGraveyardCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        // Opponent life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent denies one card — pays 3 life, denied card exiled, rest to hand")
    void opponentDeniesOne() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent denies Grizzly Bears
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Opponent denies all three cards — pays 9 life, all exiled")
    void opponentDeniesAll() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent denies all three cards
        harness.handleMultipleGraveyardCardsChosen(player2, List.of(card1.getId(), card2.getId(), card3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Opponent with less than 3 life cannot deny any card — all go to hand automatically")
    void opponentCannotAffordToPay() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));
        harness.setLife(player2, 2);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No choice presented — all go to hand automatically
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent with exactly 3 life can still pay for one card")
    void opponentWithExactly3LifeCanPayForOne() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));
        harness.setLife(player2, 3);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Choice is presented (opponent has exactly 3 life, can pay for one)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        harness.handleMultipleGraveyardCardsChosen(player2, List.of(card1.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(0);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("Opponent cannot pay for more cards than their life allows")
    void opponentCannotOverpay() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        Card card3 = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2, card3));
        harness.setLife(player2, 5);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent tries to deny two cards (would cost 6 life but only has 5)
        assertThatThrownBy(() ->
                harness.handleMultipleGraveyardCardsChosen(player2, List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fewer than 3 cards in library reveals only what's available")
    void fewerThanThreeCards() {
        Card card1 = new GrizzlyBears();
        Card card2 = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(card1, card2));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Choice presented with 2 cards
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Opponent denies none
        harness.handleMultipleGraveyardCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyard() {
        Card card1 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(card1);

        SwordPointDiplomacy spd = new SwordPointDiplomacy();
        harness.setHand(player1, List.of(spd));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent makes choice
        harness.handleMultipleGraveyardCardsChosen(player2, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sword-Point Diplomacy"));
    }
}
