package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OupheVandals;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
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

@CardUsed({AbunasChant.class, OupheVandals.class, MagmaJet.class})
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

    @Test
    @DisplayName("The shield prevents only five damage across multiple damage events")
    void shieldIsConsumedAcrossDamageEvents() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        cast(new int[]{1}, List.of(creature.getId()), false);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MagmaJet(), new MagmaJet(), new MagmaJet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.getDamagePreventionShield()).isEqualTo(3);
        assertThat(creature.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.getDamagePreventionShield()).isZero();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ouphe Vandals");
    }

    @Test
    @DisplayName("An entwined spell does not gain life if its only target becomes illegal")
    void entwinedSpellDoesNotResolveWithIllegalTarget() {
        Permanent creature = addCreatureReady(player2, new OupheVandals());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId()));

        harness.setHand(player2, List.of(new MagmaJet()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player2, "Ouphe Vandals");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Abuna's Chant");
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
