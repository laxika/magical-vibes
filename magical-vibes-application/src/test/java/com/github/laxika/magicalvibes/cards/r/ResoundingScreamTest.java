package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResoundingScreamTest extends BaseCardTest {

    // ===== Main spell: target player discards a card at random =====

    @Test
    @DisplayName("Target player discards one card at random")
    void discardsOneCardAtRandom() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Cycling reflexive trigger =====

    @Test
    @DisplayName("Cycling makes target player discard two cards at random and draws a card")
    void cyclingDiscardsTwoAndDraws() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt(), new GiantGrowth()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Target discarded two at random.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        // The cycling draw still happens: Scream discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Resounding Scream"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling discards the whole hand when target has fewer than two cards")
    void cyclingDiscardsWholeSmallHand() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ResoundingScream()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        // Cycling draw still resolves.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
