package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenBloodboiler.class, DwarvenDriller.class, NantukoMonastery.class, SuntailHawk.class})
class DwarvenBloodboilerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Dwarf gives target creature +2/+0")
    void tapsDwarfToBoostTargetCreature() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent target = addCreatureReady(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bloodboiler.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated without an untapped Dwarf")
    void requiresAnUntappedDwarfToPay() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        bloodboiler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("The cost can tap another Dwarf controlled by the activating player")
    void canTapAnotherDwarfYouControl() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent otherDwarf = addCreatureReady(player1, new DwarvenDriller());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        bloodboiler.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(otherDwarf.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Dwarf cannot pay the cost")
    void cannotTapOpponentsDwarf() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent opponentDwarf = addCreatureReady(player2, new DwarvenDriller());
        bloodboiler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentDwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("A non-Dwarf creature cannot pay the cost")
    void cannotTapNonDwarfToPay() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent nonDwarf = addCreatureReady(player1, new SuntailHawk());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        bloodboiler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(nonDwarf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void requiresCreatureTarget() {
        addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NantukoMonastery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent target = addCreatureReady(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Another untapped Dwarf you control can pay the cost")
    void canTapAnotherControlledDwarfAsCost() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent otherDwarf = addCreatureReady(player1, new DwarvenDriller());
        Permanent target = addCreatureReady(player2, new DwarvenDriller());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, otherDwarf.getId());
        harness.passBothPriorities();

        assertThat(bloodboiler.isTapped()).isFalse();
        assertThat(otherDwarf.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Dwarf cannot pay the cost")
    void cannotTapOpponentsDwarfAsCost() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent target = addCreatureReady(player2, new DwarvenDriller());
        bloodboiler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("A summoning-sick Bloodboiler can tap itself and target itself")
    void summoningSickBloodboilerCanBoostItself() {
        Permanent bloodboiler = harness.addToBattlefieldAndReturn(player1, new DwarvenBloodboiler());

        assertThat(bloodboiler.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, bloodboiler.getId());
        assertThat(bloodboiler.isTapped()).isTrue();
        assertThat(bloodboiler.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(bloodboiler.getPowerModifier()).isEqualTo(2);
        assertThat(bloodboiler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick support Dwarf can pay the cost")
    void summoningSickDwarfCanPayCost() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        bloodboiler.tap();

        assertThat(dwarf.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dwarf.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped Bloodboiler can activate repeatedly using different Dwarves")
    void boostsFromSeparateActivationsAccumulate() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent firstDwarf = addCreatureReady(player1, new DwarvenDriller());
        Permanent secondDwarf = addCreatureReady(player1, new DwarvenDriller());
        Permanent target = addCreatureReady(player2, new SuntailHawk());
        bloodboiler.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, firstDwarf.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(firstDwarf.isTapped()).isTrue();
        assertThat(secondDwarf.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The tapping cost remains paid when the target leaves before resolution")
    void targetLeavingDoesNotRefundCost() {
        Permanent bloodboiler = addCreatureReady(player1, new DwarvenBloodboiler());
        Permanent target = addCreatureReady(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(bloodboiler.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
