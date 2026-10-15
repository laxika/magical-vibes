package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.cards.o.OrcishVeteran;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTown.class, GoblinTownFlunkies.class, OrcishVeteran.class, OrdinaryBear.class,
        BoggartShenanigans.class})
class GoblinTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        Permanent town = harness.enterBattlefieldAndReturn(player1, new GoblinTown());

        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or red mana")
    void manaAbilityAddsChosenMana() {
        Permanent town = addReadyTown();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability puts two counters on a Goblin")
    void sacrificeAbilityBoostsGoblin() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(town);
        harness.assertInGraveyard(player1, "Goblin-town");
    }

    @Test
    @DisplayName("Sacrifice ability also targets an Orc")
    void sacrificeAbilityBoostsOrc() {
        Permanent town = addReadyTown();
        Permanent orc = harness.addToBattlefieldAndReturn(player1, new OrcishVeteran());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, orc.getId());
        harness.passBothPriorities();

        assertThat(orc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a non-Goblin or non-Orc")
    void sacrificeAbilityRejectsOtherCreature() {
        Permanent town = addReadyTown();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        addManaForSacrificeAbility();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityIsSorcerySpeedOnly() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaAbilityAddsBlackWithoutUsingStack() {
        Permanent town = addReadyTown();

        harness.activateAbility(player1, battlefieldIndex(town), 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(town.isTapped()).isTrue();
    }

    @Test
    void sacrificeAbilityRejectsOpponentsGoblin() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(town);
        assertThat(town.isTapped()).isFalse();
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sacrificeAbilityRejectsCombatPhase() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(town);
    }

    @Test
    void sacrificeAbilityRejectsNonemptyStack() {
        Permanent town = addReadyTown();
        Permanent secondTown = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        addManaForSacrificeAbility();
        readyMainPhase();
        harness.activateAbility(player1, battlefieldIndex(town), 1, null, goblin.getId());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(secondTown), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondTown);
        harness.passBothPriorities();
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tappedTownCannotPaySacrificeAbilityCost() {
        Permanent town = harness.enterBattlefieldAndReturn(player1, new GoblinTown());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(town), 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(town);
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndCountersWaitForResolution() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, goblin.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(town);
        harness.assertInGraveyard(player1, "Goblin-town");
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void targetChangingControllersDoesNotReceiveCounters() {
        Permanent town = addReadyTown();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        addManaForSacrificeAbility();
        readyMainPhase();
        harness.activateAbility(player1, battlefieldIndex(town), 1, null, goblin.getId());

        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerBattlefields.get(player2.getId()).add(goblin);
        gd.stolenCreatures.put(goblin.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Goblin-town");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeAbilityCanPutCountersOnNoncreatureGoblin() {
        Permanent town = addReadyTown();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        addManaForSacrificeAbility();
        readyMainPhase();

        harness.activateAbility(player1, battlefieldIndex(town), 1, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Goblin-town");
    }

    private Permanent addReadyTown() {
        return harness.addToBattlefieldAndReturn(player1, new GoblinTown());
    }

    private void addManaForSacrificeAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
