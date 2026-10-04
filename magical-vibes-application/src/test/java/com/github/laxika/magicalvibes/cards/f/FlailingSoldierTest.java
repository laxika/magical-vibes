package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlailingSoldier.class, CrenellatedWall.class})
class FlailingSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay to give the Soldier +1/+1 until end of turn")
    void anyPlayerMayBoostSoldier() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may pay to give the Soldier -1/-1 until end of turn")
    void anyPlayerMayShrinkSoldier() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(-1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Power and toughness modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A single temporary modifier expires during cleanup")
    void modifierExpiresDuringCleanup() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent can boost the Soldier even when their own permanent has an ability")
    void opponentMayBoostWithOwnActivatedPermanent() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        Permanent wall = addCreatureReady(player2, new CrenellatedWall());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);
        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two shrink activations send the Soldier to its owner's graveyard")
    void opponentMayShrinkSoldierToDeath() {
        addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flailing Soldier");
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flailing Soldier");
        harness.assertInGraveyard(player1, "Flailing Soldier");
        harness.assertNotInGraveyard(player2, "Flailing Soldier");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activating player must pay, even when the controller has mana")
    void opponentCannotUseControllersMana() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Soldier can still activate its abilities repeatedly")
    void tappedSummoningSickSoldierMayActivateRepeatedly() {
        harness.addToBattlefield(player1, new FlailingSoldier());
        Permanent soldier = findPermanent(player1, "Flailing Soldier");
        soldier.setSummoningSick(true);
        soldier.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(2);
        assertThat(soldier.getToughnessModifier()).isEqualTo(2);
        assertThat(soldier.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A negative modifier lasts through the end step and expires at cleanup")
    void negativeModifierExpiresDuringCleanup() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(soldier.getPowerModifier()).isEqualTo(-1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(-1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Flailing Soldier");
    }

    @Test
    @DisplayName("A boost in response to two shrink activations can save the Soldier")
    void boostInResponsePreventsLethalShrinking() {
        Permanent soldier = addCreatureReady(player1, new FlailingSoldier());
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();

        harness.passBothPriorities();
        assertThat(soldier.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(soldier.getToughnessModifier()).isZero();
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isEqualTo(-1);
        assertThat(soldier.getToughnessModifier()).isEqualTo(-1);
        harness.assertOnBattlefield(player1, "Flailing Soldier");
        harness.assertNotInGraveyard(player1, "Flailing Soldier");
    }
}
