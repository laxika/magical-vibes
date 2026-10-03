package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({CliffsideRescuer.class, GrizzlyBears.class, Mountain.class, Shock.class})
class CliffsideRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Cliffside Rescuer protects a permanent you control from opponents")
    void sacrificeGrantsProtectionFromOpponents() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromOpponents(gd, target, player2.getId())).isTrue();
        harness.assertInGraveyard(player1, "Cliffside Rescuer");
    }

    @Test
    @DisplayName("Protection from opponents prevents an opponent's spell from targeting the permanent")
    void protectionPreventsOpponentSpellTargeting() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The granted protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromOpponents(gd, target, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's permanent")
    void cannotTargetOpponentPermanent() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    void sacrificeIsPaidBeforeProtectionResolves() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Cliffside Rescuer");
        harness.assertNotOnBattlefield(player1, "Cliffside Rescuer");
        assertThat(gqs.hasProtectionFromOpponents(gd, target, player2.getId())).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromOpponents(gd, target, player2.getId())).isTrue();
    }

    @Test
    void canProtectANoncreaturePermanent() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFromOpponents(gd, target, player2.getId())).isTrue();
        assertThat(gqs.hasProtectionFromOpponents(gd, target, player1.getId())).isFalse();
    }

    @Test
    void canTargetItselfButSacrificeLeavesNoLegalTarget() {
        Permanent rescuer = addCreatureReady(player1, new CliffsideRescuer());

        harness.activateAbility(player1, 0, null, rescuer.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cliffside Rescuer");
        harness.assertNotOnBattlefield(player1, "Cliffside Rescuer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedRescuerCannotActivate() {
        Permanent rescuer = addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        rescuer.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Cliffside Rescuer");
        harness.assertNotInGraveyard(player1, "Cliffside Rescuer");
    }

    @Test
    void summoningSickRescuerCannotActivate() {
        Permanent rescuer = addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        rescuer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Cliffside Rescuer");
        harness.assertNotInGraveyard(player1, "Cliffside Rescuer");
    }

    @Test
    void protectionMakesAnOpponentsPendingSpellTargetIllegal() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void protectionAllowsControllersOwnSpellAndDamage() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void protectedCreatureCannotBeBlockedByOpponent() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void protectedBlockerPreventsOpponentsCombatDamage() {
        addCreatureReady(player1, new CliffsideRescuer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
