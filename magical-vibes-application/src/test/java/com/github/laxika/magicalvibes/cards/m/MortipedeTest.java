package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DromadPurebred;
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

@CardUsed({Mortipede.class, DromadPurebred.class})
class MortipedeTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability requires all able creatures to block Mortipede")
    void allAbleCreaturesMustBlock() {
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player2, new DromadPurebred());
        addCreatureReady(player2, new DromadPurebred());
        activateMortipedeAbility();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
    }

    @Test
    @DisplayName("Tapped creatures are not required to block Mortipede")
    void tappedCreaturesAreNotRequiredToBlock() {
        addCreatureReady(player1, new Mortipede());
        Permanent untapped = addCreatureReady(player2, new DromadPurebred());
        Permanent tapped = addCreatureReady(player2, new DromadPurebred());
        tapped.tap();
        activateMortipedeAbility();

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Mortipede does not require blockers without activating its ability")
    void noRequirementWithoutActivation() {
        addCreatureReady(player1, new Mortipede());
        Permanent blocker = addCreatureReady(player2, new DromadPurebred());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Able blockers cannot block another attacker instead of the activated Mortipede")
    void blockersCannotBeDivertedToAnotherAttacker() {
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player1, new DromadPurebred());
        addCreatureReady(player2, new DromadPurebred());
        activateMortipedeAbility();

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A blocker may choose either Mortipede when both abilities have resolved")
    void competingMortipedesAllowEitherBlock() {
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player2, new DromadPurebred());
        activateMortipedeAbility();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Mortipede can activate after attackers are declared and before blockers")
    void activationAfterAttackersAreDeclared() {
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player2, new DromadPurebred());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        activateMortipedeAbility();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Mortipede's blocking requirement expires at the end of the turn")
    void requirementExpiresAtEndOfTurn() {
        addCreatureReady(player1, new Mortipede());
        Permanent blocker = addCreatureReady(player2, new DromadPurebred());
        activateMortipedeAbility();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    private void activateMortipedeAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
