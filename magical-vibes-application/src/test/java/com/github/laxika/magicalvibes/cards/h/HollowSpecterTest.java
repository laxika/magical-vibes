package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowSpecter.class, GrizzlyBears.class, HillGiant.class})
class HollowSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller pay X and discard one of X revealed cards")
    void combatDamagePaysXAndDiscardsFromRevealedCards() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice xChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(xChoice).isNotNull();
        assertThat(xChoice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        PendingInteraction.RevealCardsDiscardChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealCardsDiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(discard).isNotNull();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discard.revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("When X exceeds the damaged player's hand, their whole hand is revealed")
    void positiveXRevealsWholeSmallerHand() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        PendingInteraction.RevealCardsDiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(discard).isNotNull();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(discard.revealedCardIds()).hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mana restricted to Myr spells and abilities cannot pay this ability")
    void myrRestrictedManaCannotPayAbility() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        gd.playerManaPools.get(player1.getId()).addMyrOnlyColorless(1);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Choosing X=0 declines the ability and does not spend mana")
    void choosingZeroDeclines() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Paying X with an empty opposing hand still spends the mana")
    void payingWithEmptyHandSpendsMana() {
        harness.setHand(player2, List.of());
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Without available mana, combat damage does not discard a card")
    void noManaDoesNotDiscard() {
        HollowSpecter cardInHand = new HollowSpecter();
        harness.setHand(player2, List.of(cardInHand));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller's choice uses reveal order and leaves unrevealed cards in hand")
    void discardChoiceUsesRevealOrder() {
        HollowSpecter first = new HollowSpecter();
        HollowSpecter hidden = new HollowSpecter();
        HollowSpecter last = new HollowSpecter();
        harness.setHand(player2, List.of(first, hidden, last));
        Permanent specter = addCreatureReady(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.RevealCardsDiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(discard).isNotNull();
        assertThat(discard.revealedCardIds()).containsExactly(last.getId(), first.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, hidden);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(last);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
