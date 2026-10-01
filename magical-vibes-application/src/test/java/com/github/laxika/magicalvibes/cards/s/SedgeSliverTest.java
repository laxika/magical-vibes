package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SedgeSliver.class, SidewinderSliver.class, AshcoatBear.class, Swamp.class})
class SedgeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Each Sliver gets +1/+1 when its controller controls a Swamp")
    void buffsSliversWhoseControllerControlsSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent ownSliver = addCreatureReady(player1, new SidewinderSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());
        Permanent sedgeSliver = addCreatureReady(player1, new SedgeSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sedgeSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sedgeSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(1);

        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sedge Sliver does not boost non-Sliver creatures")
    void doesNotBoostNonSlivers() {
        harness.addToBattlefield(player1, new Swamp());
        addCreatureReady(player1, new SedgeSliver());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Sliver without a Swamp under its controller's control is not boosted")
    void doesNotBoostSliverWithoutSwamp() {
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());
        addCreatureReady(player1, new SedgeSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(1);
    }

    @Test
    @DisplayName("All Slivers gain the black regeneration ability")
    void grantsRegenerationAbilityToOpposingSliver() {
        addCreatureReady(player1, new SedgeSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(opposingSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(opposingSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sedge Sliver can activate the regeneration ability for itself")
    void grantsRegenerationAbilityToItself() {
        Permanent sedgeSliver = addCreatureReady(player1, new SedgeSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sedgeSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(sedgeSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration ability requires black mana")
    void regenerationRequiresBlackMana() {
        addCreatureReady(player1, new SedgeSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain Sedge Sliver's regeneration ability")
    void doesNotGrantRegenerationAbilityToNonSlivers() {
        addCreatureReady(player1, new SedgeSliver());
        addCreatureReady(player1, new AshcoatBear());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
