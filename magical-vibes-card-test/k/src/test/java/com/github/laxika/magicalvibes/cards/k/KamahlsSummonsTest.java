package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamahlsSummons.class, ElvishWarrior.class, Mountain.class, Forest.class})
class KamahlsSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals creature cards and creates that many Bears")
    void revealsCreatureCardsAndCreatesBearsForEachPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        KamahlsSummons summons = new KamahlsSummons();
        ElvishWarrior firstCreature = new ElvishWarrior();
        ElvishWarrior secondCreature = new ElvishWarrior();
        ElvishWarrior opponentCreature = new ElvishWarrior();
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
        Permanent bear = findPermanent(player1, "Bear");
        assertThat(bear.getCard().isToken()).isTrue();
        assertThat(bear.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(bear.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(bear.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
        assertThat(bear.getCard().getPower()).isEqualTo(2);
        assertThat(bear.getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCreature, secondCreature, nonCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature, opponentNonCreature);
    }

    @Test
    @DisplayName("The active player chooses first when player two is active")
    void activePlayerChoosesFirstWhenPlayerTwoIsActive() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        KamahlsSummons summons = new KamahlsSummons();
        ElvishWarrior player1Creature = new ElvishWarrior();
        ElvishWarrior player2Creature = new ElvishWarrior();
        harness.setHand(player1, List.of(player1Creature));
        harness.setHand(player2, List.of(summons, player2Creature));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castSorcery(player2, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice firstChoice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validCardIds()).containsExactly(player2Creature.getId());
        harness.handleMultipleCardsChosen(player2, List.of(player2Creature.getId()));

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice secondChoice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validCardIds()).containsExactly(player1Creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(player1Creature.getId()));

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bear")).isEqualTo(1);
    }

    @Test
    @DisplayName("Players without creature cards do not receive a reveal prompt")
    void skipsPlayersWithoutCreatureCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new KamahlsSummons(), new ElvishWarrior()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Bear")).isZero();
        assertThat(countPermanents(player2, "Bear")).isZero();
    }

    @Test
    @DisplayName("Resolves without a prompt when neither player has a creature card")
    void resolvesWithoutPromptWhenNeitherPlayerHasCreatureCards() {
        KamahlsSummons summons = new KamahlsSummons();
        Mountain player1NonCreature = new Mountain();
        Forest player2NonCreature = new Forest();
        harness.setHand(player1, List.of(summons, player1NonCreature));
        harness.setHand(player2, List.of(player2NonCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Bear")).isZero();
        assertThat(countPermanents(player2, "Bear")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1NonCreature);
        harness.assertInGraveyard(player1, "Kamahl's Summons");
    }

    @Test
    @DisplayName("A player may reveal only some creatures while the opponent declines")
    void revealsOnlySelectedCreaturesAfterBothPlayersChoose() {
        ElvishWarrior selected = new ElvishWarrior();
        ElvishWarrior unselected = new ElvishWarrior();
        ElvishWarrior opponentCreature = new ElvishWarrior();
        harness.setHand(player1, List.of(new KamahlsSummons(), selected, unselected));
        harness.setHand(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(countPermanents(player1, "Bear")).isZero();
        assertThat(countPermanents(player2, "Bear")).isZero();
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bear")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected, unselected);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Kamahl's Summons");
    }

    @Test
    @DisplayName("An opponent can reveal creatures when the caster has an empty hand")
    void opponentRevealsWhenCasterHasNoCreatures() {
        ElvishWarrior creature = new ElvishWarrior();
        harness.setHand(player2, List.of(creature));
        harness.castFromHand(player1, new KamahlsSummons(), "{3}{G}");
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));

        assertThat(countPermanents(player1, "Bear")).isZero();
        assertThat(countPermanents(player2, "Bear")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Kamahl's Summons");
    }

    @Test
    @DisplayName("Selected creatures remain hidden until all players have chosen")
    void doesNotRevealSelectedCardsBeforeOpponentChooses() throws Exception {
        ElvishWarrior firstCreature = new ElvishWarrior();
        ElvishWarrior secondCreature = new ElvishWarrior();
        harness.setHand(player1, List.of(new KamahlsSummons(), firstCreature));
        harness.setHand(player2, List.of(secondCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        List<GameEventEnvelope> events = new ArrayList<>();
        boolean revealedBeforeSecondChoice;

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));
            revealedBeforeSecondChoice = events.stream()
                    .anyMatch(event -> event.fact() instanceof GameEventFact.PrivateReveal);
            harness.handleMultipleCardsChosen(player2, List.of(secondCreature.getId()));
        }

        assertThat(revealedBeforeSecondChoice).isFalse();
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .hasSize(2);
        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bear")).isEqualTo(1);
    }
}
