package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DromadPurebred;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreechingGriffin.class, DromadPurebred.class, CourierHawk.class, Plains.class})
class ScreechingGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted creature can't block Screeching Griffin after the ability resolves")
    void targetedCreatureCannotBlockScreechingGriffin() {
        addCreatureReady(player1, new ScreechingGriffin());
        Permanent blocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeted creature can still block another creature")
    void targetedCreatureCanBlockAnotherCreature() {
        addCreatureReady(player1, new ScreechingGriffin());
        addCreatureReady(player1, new DromadPurebred());
        Permanent blocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability requires a creature target")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new ScreechingGriffin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The restriction does not apply to another Screeching Griffin")
    void targetedCreatureCanBlockAnotherGriffin() {
        addCreatureReady(player1, new ScreechingGriffin());
        addCreatureReady(player1, new ScreechingGriffin());
        Permanent blocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations restrict each targeted creature")
    void multipleCreaturesCannotBlockSource() {
        addCreatureReady(player1, new ScreechingGriffin());
        Permanent firstBlocker = addCreatureReady(player2, new CourierHawk());
        Permanent secondBlocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, firstBlocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, secondBlocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick Griffin can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new ScreechingGriffin());
        griffin.setSummoningSick(true);
        griffin.setTapped(true);
        Permanent blocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        griffin.setTapped(false);
        griffin.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new Plains()));

        addCreatureReady(player1, new ScreechingGriffin());
        Permanent blocker = addCreatureReady(player2, new CourierHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player2, List.of());
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player1, List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
