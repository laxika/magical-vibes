package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladFallen.class, CarapaceForger.class, IronMyr.class, SylvokLifestaff.class})
class TelJiladFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Infect deals damage to creatures as -1/-1 counters")
    void infectDealsDamageAsMinusOneCounters() {
        Permanent fallen = addCreatureReady(player1, new TelJiladFallen());
        fallen.setAttacking(true);

        addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Carapace Forger (2/2) receives 3 -1/-1 counters from infect (3 power), killing it
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Infect deals damage to players as poison counters")
    void infectDealsDamageAsPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent fallen = addCreatureReady(player1, new TelJiladFallen());
        fallen.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life unchanged because infect deals poison, not life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // 3 power = 3 poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent fallen = addCreatureReady(player1, new TelJiladFallen());
        fallen.setAttacking(true);

        addCreatureReady(player2, new IronMyr());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent fallen = addCreatureReady(player1, new TelJiladFallen());
        fallen.setAttacking(true);

        Permanent forger = addCreatureReady(player2, new CarapaceForger());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(forger.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new TelJiladFallen());

        // Simulate end of turn cleanup
        fallen.resetModifiers();

        // Static protection should still work (it's on the card, not on the permanent's mutable set)
        Permanent artifactSource = new Permanent(new IronMyr());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, fallen, artifactSource)).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts does not protect from non-artifact sources")
    void staticProtectionDoesNotProtectFromNonArtifacts() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new TelJiladFallen());

        Permanent nonArtifactSource = new Permanent(new CarapaceForger());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, fallen, nonArtifactSource)).isFalse();
    }

    @Test
    @DisplayName("Fallen can block an artifact creature and prevents its combat damage")
    void preventsArtifactCombatDamageWhileBlocking() {
        Permanent myr = addCreatureReady(player1, new IronMyr());
        myr.setAttacking(true);
        Permanent fallen = addCreatureReady(player2, new TelJiladFallen());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(fallen.isBlocking()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tel-Jilad Fallen");
        harness.assertInGraveyard(player1, "Iron Myr");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Protection prevents its controller from targeting Fallen with equip")
    void cannotBeTargetedByOwnEquipment() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new TelJiladFallen());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, fallen.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Infect leaves counters rather than marked damage on a surviving creature")
    void infectCountersPersistOnSurvivingCreature() {
        Permanent fallen = addCreatureReady(player1, new TelJiladFallen());
        fallen.setAttacking(true);
        Permanent forger = addCreatureReady(player2, new CarapaceForger());
        forger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Carapace Forger");
        assertThat(forger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(forger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, forger)).isEqualTo(3);
        assertThat(forger.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tel-Jilad Fallen");
    }

    @Test
    @DisplayName("State-based actions detach artifact equipment from Fallen")
    void artifactEquipmentCannotRemainAttached() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new TelJiladFallen());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        equipment.setAttachedTo(fallen.getId());

        harness.runStateBasedActions();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Sylvok Lifestaff");
        harness.assertOnBattlefield(player1, "Tel-Jilad Fallen");
    }
}
