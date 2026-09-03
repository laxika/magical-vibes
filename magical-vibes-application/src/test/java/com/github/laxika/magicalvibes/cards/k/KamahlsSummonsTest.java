package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamahlsSummons.class, GrizzlyBears.class, Mountain.class, Forest.class})
class KamahlsSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals creature cards and creates that many Bears")
    void revealsCreatureCardsAndCreatesBearsForEachPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        KamahlsSummons summons = new KamahlsSummons();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        Mountain nonCreature = new Mountain();
        Forest opponentNonCreature = new Forest();
        harness.setHand(player1, List.of(summons, firstCreature, secondCreature, nonCreature));
        harness.setHand(player2, List.of(opponentCreature, opponentNonCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice firstChoice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice secondChoice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validCardIds()).containsExactly(opponentCreature.getId());
        harness.handleMultipleCardsChosen(player2, List.of(opponentCreature.getId()));

        assertThat(countPermanents(player1, "Bear")).isEqualTo(2);
        assertThat(countPermanents(player2, "Bear")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCreature, secondCreature, nonCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature, opponentNonCreature);
    }

    @Test
    @DisplayName("Players without creature cards do not receive a reveal prompt")
    void skipsPlayersWithoutCreatureCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new KamahlsSummons(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Bear")).isZero();
        assertThat(countPermanents(player2, "Bear")).isZero();
    }
}
