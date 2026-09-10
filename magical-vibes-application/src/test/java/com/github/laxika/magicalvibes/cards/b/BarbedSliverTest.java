package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedSliver.class, MetallicSliver.class, CanyonWildcat.class})
class BarbedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain Barbed Sliver's power ability")
    void grantsAbilityToAllSlivers() {
        Permanent barbedSliver = addCreatureReady(player1, new BarbedSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, barbedSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("The ability gives a Sliver +1/+0 until end of turn")
    void boostsSliverUntilEndOfTurn() {
        Permanent barbedSliver = addCreatureReady(player1, new BarbedSliver());
        int basePower = gqs.getEffectivePower(gd, barbedSliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, barbedSliver)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, barbedSliver)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("A Sliver other than Barbed Sliver can activate the granted ability")
    void anotherSliverCanActivateGrantedAbility() {
        addCreatureReady(player1, new BarbedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());
        int basePower = gqs.getEffectivePower(gd, opposingSliver);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Barbed Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent barbedSliver = addCreatureReady(player1, new BarbedSliver());
        Permanent otherSliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(barbedSliver);

        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).isEmpty();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new BarbedSliver());
        Permanent nonSliver = addCreatureReady(player1, new CanyonWildcat());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }
}
