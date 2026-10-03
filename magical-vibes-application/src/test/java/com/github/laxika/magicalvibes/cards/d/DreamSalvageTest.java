package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamSalvage.class, GrizzlyBears.class, MindRot.class, ScatheZombies.class})
class DreamSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the number of cards target opponent discarded this turn")
    void drawsEqualToOpponentDiscards() {
        // Opponent discards two cards this turn (Mind Rot).
        harness.setHand(player2, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setHand(player1, List.of(new MindRot(), new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Dream Salvage draws two — one per card discarded this turn.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws nothing when target opponent has discarded no cards this turn")
    void drawsNothingWithoutDiscards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setHand(player1, List.of(new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Counts actual discards when the opponent has only one card")
    void drawsOneWhenOnlyOneCardWasDiscarded() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot(), new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Adds discards from multiple spells during the same turn")
    void countsMultipleDiscardEvents() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new ScatheZombies(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot(), new MindRot(), new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies(),
                new GrizzlyBears(), new ScatheZombies()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not count the caster's own discards")
    void ignoresControllerDiscards() {
        harness.setHand(player1, List.of(new MindRot(), new GrizzlyBears(),
                new ScatheZombies(), new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not count discards from a spell still waiting to resolve")
    void doesNotCountUnresolvedDiscardSpell() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setHand(player1, List.of(new MindRot(), new DreamSalvage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
    }
}
