package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralSliver.class, PlatedSliver.class, AvenEnvoy.class})
class SpectralSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain Spectral Sliver's pump ability")
    void grantsAbilityToAllSlivers() {
        Permanent source = addCreatureReady(player1, new SpectralSliver());
        Permanent ownSliver = addCreatureReady(player1, new PlatedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new PlatedSliver());
        Permanent nonSliver = addCreatureReady(player1, new AvenEnvoy());

        assertThat(gs.getEffectiveActivatedAbilities(gd, source)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("An opposing Sliver can activate the granted pump ability")
    void opposingSliverCanActivateGrantedAbility() {
        addCreatureReady(player1, new SpectralSliver());
        Permanent opposingSliver = addCreatureReady(player2, new PlatedSliver());
        int basePower = gqs.getEffectivePower(gd, opposingSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opposingSliver);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Slivers lose Spectral Sliver's granted ability when it leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SpectralSliver());
        Permanent sliver = addCreatureReady(player1, new PlatedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, source));

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
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
