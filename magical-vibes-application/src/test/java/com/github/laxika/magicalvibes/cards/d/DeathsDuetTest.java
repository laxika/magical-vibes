package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathsDuetTest extends BaseCardTest {

    @Test
    @DisplayName("Returns exactly two target creature cards from the graveyard to hand")
    void returnsExactlyTwoTargetCreatureCards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        Card creature3 = new GrizzlyBears();
        Card spell = new DeathsDuet();
        harness.setGraveyard(player1, List.of(creature1, creature2, creature3));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature3.getId(), spell.getId());
    }

    @Test
    @DisplayName("Cannot cast without two matching creature cards in the graveyard")
    void cannotCastWithoutTwoMatchingCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LeoninScimitar()));
        harness.setHand(player1, List.of(new DeathsDuet()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires 2 matching cards in your graveyard");
    }
}
