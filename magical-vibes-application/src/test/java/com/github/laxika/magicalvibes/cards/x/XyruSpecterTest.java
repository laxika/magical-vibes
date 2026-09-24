package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XyruSpecter.class, DarkRitual.class, Forest.class, GrizzlyBears.class})
class XyruSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged opponent chooses to discard or challenge")
    void damagedOpponentChoosesMode() {
        addAttackingSpecter();
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly("Discard a card", "Challenge");
    }

    @Test
    @DisplayName("A challenge showing two black cards makes the opponent discard two cards")
    void successfulChallengeDiscardsTwo() {
        addAttackingSpecter();
        DarkRitual firstBlackCard = new DarkRitual();
        DarkRitual secondBlackCard = new DarkRitual();
        assertThat(firstBlackCard.getColors()).contains(CardColor.BLACK);
        assertThat(secondBlackCard.getColors()).contains(CardColor.BLACK);
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice revealChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(revealChoice.playerId()).isEqualTo(player1.getId());
        assertThat(revealChoice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A challenge without two revealed black cards does nothing")
    void unsuccessfulChallengeDoesNotDiscard() {
        addAttackingSpecter();
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(blackCard, new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addAttackingSpecter() {
        Permanent specter = addCreatureReady(player1, new XyruSpecter());
        specter.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
