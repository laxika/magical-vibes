package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImmortalServitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every creature card with mana value X from your graveyard")
    void returnsAllMatchingCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves(),
                new HillGiant()));
        harness.setHand(player1, List.of(new ImmortalServitude()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Hill Giant", "Immortal Servitude");
    }

    @Test
    @DisplayName("Non-creature cards with mana value X stay in the graveyard")
    void ignoresNonCreatureCards() {
        harness.setGraveyard(player1, List.of(new Plains(), new LlanowarElves()));
        harness.setHand(player1, List.of(new ImmortalServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Immortal Servitude");
    }

    @Test
    @DisplayName("Opponent's creature cards with mana value X are not returned")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new ImmortalServitude()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("No creature card matches X, so nothing returns")
    void noMatchReturnsNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImmortalServitude()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Immortal Servitude");
    }
}
