package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShorelineLooter.class, Forest.class, GrizzlyBears.class})
class ShorelineLooterTest extends BaseCardTest {

    @Test
    @DisplayName("Shoreline Looter can't be blocked")
    void cannotBeBlocked() {
        Permanent looter = addCreatureReady(player1, new ShorelineLooter());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        looter.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(looter);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage draws and then discards below threshold")
    void drawsAndDiscardsBelowThreshold() {
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent looter = addCreatureReady(player1, new ShorelineLooter());
        looter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        int discardedIndex = gd.playerHands.get(player1.getId()).indexOf(discarded);
        harness.handleCardChosen(player1, discardedIndex);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Combat damage does not discard at threshold")
    void doesNotDiscardAtThreshold() {
        GrizzlyBears inHand = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));

        Permanent looter = addCreatureReady(player1, new ShorelineLooter());
        looter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand, drawn);
    }

    @ParameterizedTest
    @CsvSource({"6, 7, false", "7, 6, true"})
    @DisplayName("Threshold uses the graveyard count when the ability resolves")
    void checksThresholdAtResolution(int initialCount, int finalCount, boolean mustDiscard) {
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, IntStream.range(0, initialCount)
                .mapToObj(i -> (Card) new Forest()).toList());
        Permanent looter = addCreatureReady(player1, new ShorelineLooter());
        looter.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, IntStream.range(0, finalCount)
                .mapToObj(i -> (Card) new Forest()).toList());
        resolveAllTriggers();

        if (mustDiscard) {
            harness.handleCardChosen(player1, 0);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        } else {
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        }
    }

    @Test
    @DisplayName("An empty hand still discards the drawn card below threshold, regardless of the opponent's graveyard")
    void discardsDrawnCardWithEmptyHandAndOpponentAtThreshold() {
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent looter = addCreatureReady(player1, new ShorelineLooter());
        looter.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }
}
