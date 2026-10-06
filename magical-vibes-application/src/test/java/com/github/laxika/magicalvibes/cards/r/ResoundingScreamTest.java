package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoundingScream.class, ResoundingRoar.class, ResoundingSilence.class, ResoundingThunder.class, ResoundingWave.class})
class ResoundingScreamTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards one card at random")
    void discardsOneCardAtRandom() {
        harness.setHand(player2, List.of(new ResoundingRoar(), new ResoundingSilence(), new ResoundingThunder()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Targeting a player with an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling makes target player discard two cards at random and draws a card")
    void cyclingDiscardsTwoAndDraws() {
        harness.setHand(player2, List.of(new ResoundingRoar(), new ResoundingSilence(), new ResoundingThunder(), new ResoundingWave()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.setLibrary(player1, List.of(new ResoundingRoar()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Target discarded two at random.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        // The cycling draw still happens: Scream discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Resounding Scream");
        harness.assertInHand(player1, "Resounding Roar");
    }

    @Test
    @DisplayName("Cycling discards the whole hand when target has fewer than two cards")
    void cyclingDiscardsWholeSmallHand() {
        harness.setHand(player2, List.of(new ResoundingRoar()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.setLibrary(player1, List.of(new ResoundingSilence()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        // Cycling draw still resolves.
        harness.assertInHand(player1, "Resounding Silence");
    }

    @Test
    @DisplayName("Cycling discard trigger resolves before the separate cycling draw")
    void cyclingLeavesDrawOnStackAfterDiscardResolves() {
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.setHand(player2, List.of(new ResoundingScream(), new ResoundingScream()));
        harness.setLibrary(player1, List.of(new ResoundingScream()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.assertInGraveyard(player1, "Resounding Scream");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Resounding Scream");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell can target its controller")
    void spellCanDiscardFromItsController() {
        harness.setHand(player1, List.of(new ResoundingScream(), new ResoundingScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
