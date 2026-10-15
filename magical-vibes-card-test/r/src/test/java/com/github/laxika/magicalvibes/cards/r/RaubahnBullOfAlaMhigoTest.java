package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralSword;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RaubahnBullOfAlaMhigo.class, GiantGrowth.class, GrizzlyBears.class, StriderHarness.class, CoralSword.class})
class RaubahnBullOfAlaMhigoTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay life")
    void wardCountersUnpaidSpell() {
        Permanent raubahn = addReadyRaubahn(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ward uses Raubahn's power when it resolves")
    void wardUsesCurrentPower() {
        Permanent raubahn = addReadyRaubahn(player1);
        raubahn.setPowerModifier(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attacking attaches an optional Equipment to a target attacking creature")
    void attackAttachesEquipmentToTargetAttacker() {
        Permanent raubahn = addReadyRaubahn(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new StriderHarness());
        equipment.setAttachedTo(raubahn.getId());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Attacking may decline the Equipment target")
    void attackMayDeclineEquipment() {
        addReadyRaubahn(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new StriderHarness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent raubahn = addReadyRaubahn(player1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, raubahn.getId());

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ward reads power after the ward trigger is on the stack")
    void wardPowerChangesBeforeResolution() {
        Permanent raubahn = addReadyRaubahn(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        raubahn.setPowerModifier(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(7);
    }

    @Test
    @DisplayName("Ward counters the spell when its controller has insufficient life")
    void wardCannotBePaidWithInsufficientLife() {
        Permanent raubahn = addReadyRaubahn(player1);
        harness.setLife(player2, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, raubahn.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertLife(player2, 1);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(2);
    }

    @Test
    @DisplayName("Raubahn can attach an Equipment to itself when attacking alone")
    void attackAttachesEquipmentToSelf() {
        Permanent raubahn = addReadyRaubahn(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new CoralSword());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, raubahn.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(raubahn.getId());
        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, raubahn)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipment stays unattached if its target stops attacking before resolution")
    void creatureMustStillBeAttackingOnResolution() {
        Permanent raubahn = addReadyRaubahn(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new CoralSword());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, raubahn.getId());
        raubahn.setAttacking(false);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An Equipment that changes controller is not attached on resolution")
    void equipmentMustStillBeControlledOnResolution() {
        Permanent raubahn = addReadyRaubahn(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new CoralSword());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, raubahn.getId());
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Raubahn can attack without controlling any Equipment")
    void attackWithoutEquipment() {
        Permanent raubahn = addReadyRaubahn(player1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, raubahn.getId());
        harness.passBothPriorities();

        assertThat(raubahn.isAttacking()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyRaubahn(Player player) {
        return addCreatureReady(player, new RaubahnBullOfAlaMhigo());
    }
}
