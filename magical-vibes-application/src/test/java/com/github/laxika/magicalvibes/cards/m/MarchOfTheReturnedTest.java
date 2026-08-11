package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MarchOfTheReturnedTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creature cards from the graveyard to hand")
    void returnsTwoTargetCreatureCardsToHand() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());

        List<UUID> selected = new ArrayList<>(choice.validCardIds());
        harness.handleMultipleCardsChosen(player1, selected);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature1.getId(), creature2.getId());
    }

    @Test
    @DisplayName("Allows choosing only one creature card")
    void returnsOneTargetCreatureCard() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature2.getId())
                .doesNotContain(creature1.getId());
    }

    @Test
    @DisplayName("Only creature cards are valid targets")
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Choosing zero targets returns nothing")
    void choosingZeroTargetsReturnsNothing() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MarchOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }
}
