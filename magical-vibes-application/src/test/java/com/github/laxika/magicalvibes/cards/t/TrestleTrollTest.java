package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AssassinsStrike;
import com.github.laxika.magicalvibes.cards.g.GolgariLonglegs;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrestleTroll.class, TowerDrake.class, AssassinsStrike.class, GolgariLonglegs.class})
class TrestleTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the ability on the stack with its source recorded")
    void activatingAbilityPutsOnStack() {
        Permanent troll = addCreatureReady(player1, new TrestleTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        StackEntry entry = gameData.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(troll.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new TrestleTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Trestle Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without all required mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new TrestleTroll());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void defenderCannotAttack() {
        addCreatureReady(player1, new TrestleTroll());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Trestle Troll").isAttacking()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new TowerDrake());
        addCreatureReady(player2, new TrestleTroll());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Trestle Troll");
        harness.assertInGraveyard(player1, "Tower Drake");
        harness.assertLife(player2, 20);
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new TrestleTroll());
        troll.setSummoningSick(true);
        troll.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    void regenerationProtectsOnlyItsSourceFromTheNextDestruction() {
        Permanent troll = addCreatureReady(player1, new TrestleTroll());
        Permanent otherTroll = addCreatureReady(player1, new TrestleTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(troll.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(troll.isTapped()).isFalse();
        assertThat(otherTroll.getRegenerationShield()).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AssassinsStrike(), new AssassinsStrike()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, List.of(troll.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll, otherTroll);
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(troll.getMarkedDamage()).isZero();

        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, List.of(troll.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherTroll);
        harness.assertInGraveyard(player1, "Trestle Troll");
    }

    @Test
    void regenerationReplacesLethalCombatDamageAndRemovesTrollFromCombat() {
        Permanent troll = addCreatureReady(player1, new TrestleTroll());
        addCreatureReady(player2, new GolgariLonglegs());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Trestle Troll");
        harness.assertOnBattlefield(player2, "Golgari Longlegs");
        harness.assertLife(player1, 20);
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(troll.isBlocking()).isFalse();
        assertThat(troll.getBlockingTargets()).isEmpty();
    }

    @Test
    void unusedRegenerationShieldExpiresAtEndOfTurn() {
        Permanent troll = addCreatureReady(player1, new TrestleTroll());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isZero();
    }
}
