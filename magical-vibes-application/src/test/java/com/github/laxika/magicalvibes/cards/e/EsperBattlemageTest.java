package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EsperBattlemageTest extends BaseCardTest {

    private void addBattlemageReady() {
        harness.addToBattlefield(player1, new EsperBattlemage());
        Permanent mage = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Esper Battlemage"))
                .findFirst().orElseThrow();
        mage.setSummoningSick(false);
    }

    // ===== White ability: prevent the next 2 damage to you =====

    @Test
    @DisplayName("White ability prevents the next 2 combat damage dealt to controller")
    void whitePreventsDamageToController() {
        harness.setLife(player1, 20);
        addBattlemageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent attacks the shielded controller with a 2/2.
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 damage fully prevented → life unchanged.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("White ability prevents only the next 2 of a larger combat hit")
    void whitePreventsOnlyTwoOfLargerHit() {
        harness.setLife(player1, 20);
        addBattlemageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setPowerModifier(3);
        attacker.setToughnessModifier(3);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 5 damage → 2 prevented, 3 through.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("White ability prevents noncombat damage to controller")
    void whitePreventsNoncombatDamage() {
        harness.setLife(player1, 20);
        addBattlemageReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Black ability: target creature gets -1/-1 =====

    @Test
    @DisplayName("Black ability gives target creature -1/-1 until end of turn")
    void blackGivesTargetMinusOneMinusOne() {
        addBattlemageReady();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Black ability kills a 1-toughness creature")
    void blackKillsOneToughnessCreature() {
        addBattlemageReady();
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Black debuff wears off at cleanup")
    void blackDebuffWearsOffAtCleanup() {
        addBattlemageReady();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Restrictions =====

    @Test
    @DisplayName("Cannot activate an ability with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new EsperBattlemage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
