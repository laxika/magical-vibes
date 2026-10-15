package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeightOfMemory.class})
class WeightOfMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards for the caster")
    void drawsThreeCards() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Started with 1 card (Weight of Memory), cast it (0 cards), drew 3
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills three cards from target player's library")
    void millsThreeCards() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).reversed().stream()
                .limit(10).toList().reversed());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself for mill")
    void canTargetSelfForMill() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setLibrary(player1, gd.playerDecks.get(player1.getId()).reversed().stream()
                .limit(10).toList().reversed());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // 10 - 3 drawn - 3 milled = 4 remaining in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        // 3 milled cards + Weight of Memory itself in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Both draw and mill happen when targeting opponent")
    void bothEffectsHappen() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setLibrary(player2, gd.playerDecks.get(player2.getId()).reversed().stream()
                .limit(10).toList().reversed());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster drew 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Target was milled 3
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Weight of Memory goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Weight of Memory");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Self-targeting draws the top three before milling the next three")
    void drawsBeforeMillingWhenTargetingSelf() {
        WeightOfMemory spell = new WeightOfMemory();
        List<WeightOfMemory> drawn = List.of(new WeightOfMemory(), new WeightOfMemory(), new WeightOfMemory());
        List<WeightOfMemory> milled = List.of(new WeightOfMemory(), new WeightOfMemory(), new WeightOfMemory());
        harness.setLibrary(player1, List.of(drawn.get(0), drawn.get(1), drawn.get(2),
                milled.get(0), milled.get(1), milled.get(2)));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(milled.get(0), milled.get(1), milled.get(2), spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Mills only the available cards from a short library")
    void millsShortLibraryWithoutCausingLoss() {
        WeightOfMemory first = new WeightOfMemory();
        WeightOfMemory second = new WeightOfMemory();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An empty target library does not prevent the caster drawing three")
    void emptyTargetLibraryStillAllowsDraw() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new WeightOfMemory()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
