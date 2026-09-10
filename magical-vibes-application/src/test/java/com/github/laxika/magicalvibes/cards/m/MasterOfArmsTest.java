package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterOfArms.class, RedwoodTreefolk.class})
class MasterOfArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature blocking it")
    void tapsBlockingCreature() {
        addCreatureReady(player1, new MasterOfArms());
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        blockMasterWith(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature that isn't blocking it is an illegal target")
    void nonBlockingCreatureCannotBeTargeted() {
        addCreatureReady(player1, new MasterOfArms());
        addCreatureReady(player2, new RedwoodTreefolk());
        Permanent bystander = addCreatureReady(player2, new RedwoodTreefolk());

        blockMasterWith(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target any creature blocking it")
    void canTargetAnyBlockingCreature() {
        addCreatureReady(player1, new MasterOfArms());
        Permanent firstBlocker = addCreatureReady(player2, new RedwoodTreefolk());
        Permanent secondBlocker = addCreatureReady(player2, new RedwoodTreefolk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, secondBlocker.getId());
        harness.passBothPriorities();

        assertThat(firstBlocker.isTapped()).isFalse();
        assertThat(secondBlocker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires the generic mana in its activation cost")
    void genericManaIsRequired() {
        addCreatureReady(player1, new MasterOfArms());
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        blockMasterWith(0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires white mana in its activation cost")
    void whiteManaIsRequired() {
        addCreatureReady(player1, new MasterOfArms());
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        blockMasterWith(0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Attacks with the Master and blocks it with player2's creature at {@code blockerIndex}. */
    private void blockMasterWith(int blockerIndex) {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveAllTriggers();
    }
}
