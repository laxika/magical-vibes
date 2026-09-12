package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PlagueFiend;
import com.github.laxika.magicalvibes.cards.z.ZerapaMinotaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flay.class, PlagueFiend.class, ZerapaMinotaur.class})
class FlayTest extends BaseCardTest {

    @Test
    @DisplayName("Target player pays {1} after the first random discard to avoid the second")
    void targetPlayerPaysToAvoidSecondDiscard() {
        harness.setHand(player1, List.of(new Flay()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player2, List.of(new PlagueFiend(), new ZerapaMinotaur()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Target player discards a second random card when they decline to pay")
    void targetPlayerDeclinesSecondDiscardPayment() {
        harness.setHand(player1, List.of(new Flay()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player2, List.of(new PlagueFiend(), new ZerapaMinotaur()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Target player who cannot pay still discards a second random card")
    void targetPlayerCannotPayForSecondDiscard() {
        harness.setHand(player1, List.of(new Flay()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player2, List.of(new PlagueFiend(), new ZerapaMinotaur()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flay can target its caster")
    void canTargetAnyPlayer() {
        harness.setHand(player1, List.of(new Flay(), new PlagueFiend(), new ZerapaMinotaur()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
