package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SpatialContortion;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KazuulsTollCollector.class, StriderHarness.class, SpatialContortion.class})
class KazuulsTollCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches a controlled Equipment to itself")
    void attachesControlledEquipmentToItself() {
        Permanent collector = addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(collector.getId());
    }

    @Test
    @DisplayName("Cannot target an Equipment controlled by an opponent")
    void cannotTargetOpponentsEquipment() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player2);
        prepareSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's main phase")
    void cannotActivateOutsideSorcerySpeed() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a controlled permanent that is not Equipment")
    void cannotTargetNonEquipment() {
        Permanent collector = addReadyCollector(player1);
        prepareSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, collector.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during combat even on its controller's turn")
    void cannotActivateDuringCombat() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with another ability on the stack")
    void cannotActivateWithNonemptyStack() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.activateAbility(player1, 0, null, equipment.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can activate for zero mana while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new KazuulsTollCollector());
        collector.setSummoningSick(true);
        collector.tap();
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(collector.getId());
        assertThat(collector.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can attach multiple Equipment in the postcombat main phase")
    void canAttachMultipleEquipmentInPostcombatMain() {
        Permanent collector = addReadyCollector(player1);
        Permanent first = addEquipment(player1);
        Permanent second = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.getAttachedTo()).isEqualTo(collector.getId());
        assertThat(second.getAttachedTo()).isEqualTo(collector.getId());
    }

    @Test
    @DisplayName("Moves Equipment from another creature to the ability's source")
    void movesEquipmentFromAnotherCreature() {
        Permanent collector = addReadyCollector(player1);
        Permanent otherCollector = addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.activateAbility(player1, 1, null, equipment.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(otherCollector.getId());

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(collector.getId());
    }

    @Test
    @DisplayName("Equipment remains on its current creature if the source leaves before resolution")
    void doesNotMoveEquipmentWhenSourceLeaves() {
        Permanent collector = addReadyCollector(player1);
        Permanent otherCollector = addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.activateAbility(player1, 1, null, equipment.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, equipment.getId());

        harness.setHand(player2, List.of(new SpatialContortion()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, collector.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(collector);
        assertThat(equipment.getAttachedTo()).isEqualTo(otherCollector.getId());
    }

    @Test
    @DisplayName("Does not attach Equipment that changes controllers before resolution")
    void doesNotAttachEquipmentAfterLosingControl() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);
        harness.activateAbility(player1, 0, null, equipment.getId());

        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    private Permanent addReadyCollector(Player player) {
        return addCreatureReady(player, new KazuulsTollCollector());
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new StriderHarness());
    }

    private void prepareSorcerySpeedActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
