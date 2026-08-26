package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SedgeSliver.class, BonescytheSliver.class, GrizzlyBears.class, Swamp.class})
class SedgeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Each Sliver gets +1/+1 when its controller controls a Swamp")
    void buffsSliversWhoseControllerControlsSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());
        Permanent sedgeSliver = addCreatureReady(player1, new SedgeSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sedgeSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sedgeSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(2);

        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sedge Sliver does not boost non-Sliver creatures")
    void doesNotBoostNonSlivers() {
        harness.addToBattlefield(player1, new Swamp());
        addCreatureReady(player1, new SedgeSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("All Slivers gain the black regeneration ability")
    void grantsRegenerationAbilityToOpposingSliver() {
        addCreatureReady(player1, new SedgeSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(opposingSliver.getRegenerationShield()).isEqualTo(1);
    }
}
