package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArmorSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain Armor Sliver's toughness ability")
    void grantsAbilityToAllSlivers() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, armorSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("The ability gives a Sliver +0/+1 until end of turn")
    void boostsSliverUntilEndOfTurn() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        int baseToughness = gqs.getEffectiveToughness(gd, armorSliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, armorSliver)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, armorSliver)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new ArmorSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }
}
