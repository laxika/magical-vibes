package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HeartSliver;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.m.MuscleSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmorSliver.class, MuscleSliver.class, MoggConscripts.class, HeartSliver.class,
        AmoeboidChangeling.class})
class ArmorSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain Armor Sliver's toughness ability")
    void grantsAbilityToAllSlivers() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        Permanent ownSliver = addCreatureReady(player1, new MuscleSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MuscleSliver());

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
    @DisplayName("The granted ability boosts the Sliver that activates it")
    void grantedAbilityBoostsItsActivatingSliver() {
        addCreatureReady(player1, new ArmorSliver());
        Permanent heartSliver = addCreatureReady(player1, new HeartSliver());
        int baseToughness = gqs.getEffectiveToughness(gd, heartSliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(heartSliver), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, heartSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new ArmorSliver());
        Permanent nonSliver = addCreatureReady(player1, new MoggConscripts());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("A Sliver that loses all creature types no longer has the ability")
    void losingSliverTypeRemovesAbility() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        assertThat(gs.getEffectiveActivatedAbilities(gd, armorSliver)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 1, null, armorSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, armorSliver)).isEmpty();
    }

    @Test
    @DisplayName("An opponent can repeatedly activate a tapped, summoning-sick Sliver")
    void opponentCanActivateRepeatedlyWithoutTapping() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new MuscleSliver());
        opposingSliver.setSummoningSick(true);
        opposingSliver.tap();
        int basePower = gqs.getEffectivePower(gd, opposingSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opposingSliver);
        int armorToughness = gqs.getEffectiveToughness(gd, armorSliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectiveToughness(gd, armorSliver)).isEqualTo(armorToughness);
    }

    @Test
    @DisplayName("An activated toughness ability resolves after its source loses the Sliver type")
    void activatedAbilitySurvivesLosingSliverType() {
        Permanent armorSliver = addCreatureReady(player1, new ArmorSliver());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent heartSliver = addCreatureReady(player1, new HeartSliver());
        int baseToughness = gqs.getEffectiveToughness(gd, armorSliver);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, armorSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, armorSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, heartSliver)).hasSize(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectiveToughness(gd, armorSliver)).isEqualTo(baseToughness + 1);
    }
}
