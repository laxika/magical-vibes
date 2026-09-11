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

@CardUsed({SpectralSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class SpectralSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain Spectral Sliver's pump ability")
    void grantsAbilityToAllSlivers() {
        Permanent source = addCreatureReady(player1, new SpectralSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, source)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability boosts that Sliver until end of turn")
    void boostsTheSliverUntilEndOfTurn() {
        Permanent sliver = addCreatureReady(player1, new SpectralSliver());
        int basePower = gqs.getEffectivePower(gd, sliver);
        int baseToughness = gqs.getEffectiveToughness(gd, sliver);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(baseToughness);
    }
}
