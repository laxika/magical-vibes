package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticMeditation.class, ColossodonYearling.class, Island.class})
class MysticMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, then discarding a creature only discards one card")
    void discardingCreatureUsesTheOneCardOption() {
        castMeditation(List.of(new ColossodonYearling(), new Island(), new Island()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        List<Card> hand = gd.playerHands.get(player1.getId());
        int creatureIndex = hand.stream().filter(card -> card.hasType(CardType.CREATURE))
                .map(hand::indexOf).findFirst().orElseThrow();
        assertThat(discard.validIndices()).containsExactly(creatureIndex);
        assertThat(discard.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, creatureIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Colossodon Yearling");
        harness.assertInGraveyard(player1, "Mystic Meditation");
    }

    @Test
    @DisplayName("May still discard two cards when a creature card is available")
    void decliningCreatureOptionDiscardsTwo() {
        castMeditation(List.of(new ColossodonYearling(), new Island(), new Island()));

        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(0, 1, 2);
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Without a creature card, discarding two is mandatory")
    void noCreatureForcesDiscardTwo() {
        castMeditation(List.of(new Island(), new Island(), new Island()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Either creature can satisfy the one-card discard option")
    void choosesBetweenMultipleCreatures() {
        Card firstCreature = new ColossodonYearling();
        Card secondCreature = new ColossodonYearling();
        castMeditation(List.of(firstCreature, new Island(), secondCreature));

        harness.handleMayAbilityChosen(player1, true);

        List<Card> hand = gd.playerHands.get(player1.getId());
        int chosenIndex = hand.indexOf(secondCreature);
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactlyInAnyOrder(
                hand.indexOf(firstCreature), chosenIndex);

        harness.handleCardChosen(player1, chosenIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(firstCreature)
                .doesNotContain(secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(secondCreature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature already in hand can be discarded after drawing three noncreatures")
    void canDiscardCreatureThatWasAlreadyInHand() {
        Card creature = new ColossodonYearling();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new MysticMeditation(), creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(creature);
        harness.assertInGraveyard(player1, "Mystic Meditation");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    private void castMeditation(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MysticMeditation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
