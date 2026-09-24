package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HoverguardObserver;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
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

@CardUsed({NemesisMask.class, MyrMoonvessel.class, HoverguardObserver.class})
class NemesisMaskTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block the equipped creature")
    void allAbleCreaturesMustBlockEquippedCreature() {
        Permanent attacker = addCreatureReady(player1, new MyrMoonvessel());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        mask.setAttachedTo(attacker.getId());

        Permanent blocker1 = addCreatureReady(player2, new MyrMoonvessel());
        Permanent blocker2 = addCreatureReady(player2, new MyrMoonvessel());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Only creatures able to block the equipped creature are forced to block")
    void onlyAbleCreaturesAreForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new HoverguardObserver());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        mask.setAttachedTo(attacker.getId());

        Permanent unableBlocker = addCreatureReady(player2, new MyrMoonvessel());
        Permanent ableBlocker = addCreatureReady(player2, new HoverguardObserver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(unableBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equipping Nemesis Mask attaches it to a creature")
    void equipAttachesMask() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unattached Nemesis Mask does not force blockers")
    void unattachedMaskDoesNotForceBlockers() {
        addCreatureReady(player1, new MyrMoonvessel());
        harness.addToBattlefield(player1, new NemesisMask());
        addCreatureReady(player2, new MyrMoonvessel());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block the equipped creature")
    void tappedCreaturesAreNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new MyrMoonvessel());
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        mask.setAttachedTo(attacker.getId());

        Permanent tappedBlocker = addCreatureReady(player2, new MyrMoonvessel());
        tappedBlocker.tap();

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());

        assertThat(tappedBlocker.isBlocking()).isFalse();
    }
}
