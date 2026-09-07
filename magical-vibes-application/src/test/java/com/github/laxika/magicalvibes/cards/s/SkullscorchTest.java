package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skullscorch.class, Forest.class, Island.class, GrizzlyBears.class})
class SkullscorchTest extends BaseCardTest {

    @Test
    @DisplayName("Target player may take 4 damage instead of discarding")
    void targetPlayerMayTakeDamageInsteadOfDiscarding() {
        harness.setHand(player1, List.of(new Skullscorch()));
        harness.setHand(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.setHand(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
