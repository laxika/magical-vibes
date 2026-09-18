package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OupheVandals;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbunasChant.class, OupheVandals.class})
class AbunasChantTest extends BaseCardTest {

    @Test
    @DisplayName("Life-gain mode gives the controller 5 life")
    void lifeGainMode() {
        harness.setLife(player1, 10);
        cast(new int[]{0}, List.of(), false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Prevention mode shields the target creature from the next 5 damage")
    void preventionMode() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        cast(new int[]{1}, List.of(creature.getId()), false);

        assertThat(creature.getDamagePreventionShield()).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevention mode expires at the end of the turn")
    void preventionModeExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        cast(new int[]{1}, List.of(creature.getId()), false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Entwine pays {2} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        harness.setLife(player1, 10);
        cast(new int[]{0, 1}, List.of(creature.getId()), true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(creature.getDamagePreventionShield()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine without the additional mana is rejected")
    void entwineRequiresAdditionalMana() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention mode rejects a player target")
    void preventionModeRequiresCreatureTarget() {
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (entwined) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
