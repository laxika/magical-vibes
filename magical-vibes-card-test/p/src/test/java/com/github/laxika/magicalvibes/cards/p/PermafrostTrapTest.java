package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PermafrostTrap.class, ArborElf.class, PilgrimsEye.class, TectonicEdge.class})
class PermafrostTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast for {U} after an opponent green creature entered this turn")
    void castsForAlternateCostAfterOpponentGreenCreatureEntered() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Permafrost Trap");
    }

    @Test
    @DisplayName("Alternate cost requires an opponent green creature to have entered this turn")
    void alternateCostRequiresOpponentGreenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PilgrimsEye());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(creature.getCard()));
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithoutTargets() {
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Permafrost Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        creature.tap();
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isZero();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    void ownGreenCreatureEnteringDoesNotEnableAlternateCost() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new ArborElf());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
