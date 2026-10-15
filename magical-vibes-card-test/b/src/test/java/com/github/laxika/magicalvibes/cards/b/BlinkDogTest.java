package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PlateArmor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlinkDog.class, PlateArmor.class})
class BlinkDogTest extends BaseCardTest {

    @Test
    void teleportPhasesBlinkDogOut() {
        Permanent dog = addDogReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dog);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);
    }

    @Test
    void blinkDogPhasesBackInDuringItsControllersNextUntapStep() {
        Permanent dog = addDogReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dog);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dog);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(dog);
    }

    private Permanent addDogReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BlinkDog());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void teleportCanBeActivatedWhileSummoningSickAndTapped() {
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new BlinkDog());
        dog.setSummoningSick(true);
        dog.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);
        assertThat(dog.isTapped()).isTrue();
    }

    @Test
    void attachedEquipmentReturnsWithDogEvenUnderAnotherPlayersControl() {
        Permanent dog = addDogReady(player1);
        Permanent armor = harness.addToBattlefieldAndReturn(player2, new PlateArmor());
        armor.setAttachedTo(dog.getId());
        dog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(armor);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(armor);

        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(armor);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dog);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(armor);
        assertThat(armor.getAttachedTo()).isEqualTo(dog.getId());
        assertThat(dog.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void doubleStrikeDealsDamageInBothCombatDamageSteps() {
        Permanent dog = addDogReady(player1);
        dog.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertLife(player2, 19);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);
    }

    @Test
    void teleportRemovesAttackingDogFromCombat() {
        Permanent dog = addDogReady(player1);
        dog.setAttacking(true);
        dog.tap();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);
        assertThat(dog.isAttacking()).isFalse();
        harness.resolveCombatDamage();
        harness.assertLife(player2, 20);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
