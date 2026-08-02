package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiMirrorGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and makes a power 2 creature unblockable")
    void makesTargetUnblockable() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, bears.getId());

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature with power 3 is an illegal target")
    void rejectsCreatureWithPowerThree() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SoratamiMirrorGuard());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        // Empty the hand so the returned Island cannot push player1 over the cleanup-step hand limit.
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Guard"), 0, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
