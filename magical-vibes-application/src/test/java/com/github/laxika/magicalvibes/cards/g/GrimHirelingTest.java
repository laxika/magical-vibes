package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimHireling.class, GrizzlyBears.class})
class GrimHirelingTest extends BaseCardTest {

    @Test
    void createsTwoTreasuresOnceForMultipleCombatDamageDealers() {
        addCreatureReady(player1, new GrimHireling());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void sacrificesTreasuresForMinusXMinusX() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(2));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(hireling), 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isZero();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    void triggersForItsOwnCombatDamage() {
        addCreatureReady(player1, new GrimHireling());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void eachHirelingTriggersOnceForTheSameCombatDamage() {
        addCreatureReady(player1, new GrimHireling());
        addCreatureReady(player1, new GrimHireling());
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForOpponentsCombatDamage() {
        addCreatureReady(player1, new GrimHireling());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();
        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void zeroXNeedsNoTreasuresAndDoesNotChangeTheTarget() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        Permanent target = addCreatureReady(player2, new GrimHireling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(hireling), 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grim Hireling");
    }

    @Test
    void sacrificesTreasuresAsACostAndKillsAnOpposingCreature() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        Permanent target = addCreatureReady(player2, new GrimHireling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(hireling), 0, 2, target.getId());
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.assertOnBattlefield(player2, "Grim Hireling");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grim Hireling");
        harness.assertInGraveyard(player2, "Grim Hireling");
    }

    @Test
    void chosenTreasureIsSacrificedAndReductionExpiresAtEndOfTurn() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        Permanent target = addCreatureReady(player2, new GrimHireling());
        Permanent treasure = findPermanent(player1, "Treasure");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(hireling), 0, 1, target.getId());
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).doesNotContain(treasure);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void cannotPayPositiveXWithNonTreasurePermanents() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        Permanent target = addCreatureReady(player1, new GrimHireling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(hireling), 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hireling, target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringCombatEvenWithZeroX() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(hireling), 0, 0, hireling.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(hireling), 0, 0, hireling.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotSacrificeMoreTreasuresThanItControls() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(hireling), 0, 3, hireling.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateInResponseToItsOwnAbility() {
        Permanent hireling = addCreatureReady(player1, new GrimHireling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, indexOf(hireling), 0, 0, hireling.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(hireling), 0, 0, hireling.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grim Hireling");
    }
}
