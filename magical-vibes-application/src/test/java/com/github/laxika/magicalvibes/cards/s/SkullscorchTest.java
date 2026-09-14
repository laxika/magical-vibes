package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChurningEddy;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skullscorch.class, ChurningEddy.class, NantukoShade.class, TaintedIsle.class})
class SkullscorchTest extends BaseCardTest {

    @Test
    @DisplayName("Target player may take 4 damage instead of discarding")
    void targetPlayerMayTakeDamageInsteadOfDiscarding() {
        harness.setHand(player1, List.of(new Skullscorch()));
        harness.setHand(player2, List.of(new ChurningEddy(), new NantukoShade(), new TaintedIsle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the damage discards two cards at random")
    void decliningDamageDiscardsTwoCards() {
        harness.setHand(player1, List.of(new Skullscorch()));
        harness.setHand(player2, List.of(new ChurningEddy(), new NantukoShade(), new TaintedIsle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the damage discards the whole hand when it has fewer than two cards")
    void decliningDamageDiscardsWholeSmallHand() {
        harness.setHand(player1, List.of(new Skullscorch()));
        harness.setHand(player2, List.of(new TaintedIsle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the damage does nothing when the target has no cards")
    void decliningDamageWithEmptyHandDoesNothing() {
        harness.setHand(player1, List.of(new Skullscorch()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can be targeted")
    void controllerCanBeTargeted() {
        harness.setHand(player1, List.of(new Skullscorch(), new NantukoShade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
