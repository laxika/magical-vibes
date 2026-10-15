package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HoverguardObserver;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
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

@CardUsed({NemesisMask.class, MyrMoonvessel.class, HoverguardObserver.class})
class NemesisMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Competing Masks allow blockers to choose either equipped attacker")
    void competingMasksAllowEitherAttacker() {
        Permanent first = addCreatureReady(player1, new MyrMoonvessel());
        Permanent second = addCreatureReady(player1, new MyrMoonvessel());
        harness.addToBattlefieldAndReturn(player1, new NemesisMask()).setAttachedTo(first.getId());
        harness.addToBattlefieldAndReturn(player1, new NemesisMask()).setAttachedTo(second.getId());
        Permanent blocker1 = addCreatureReady(player2, new MyrMoonvessel());
        Permanent blocker2 = addCreatureReady(player2, new MyrMoonvessel());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mask does not override a creature's blocking restriction")
    void blockingRestrictionStillApplies() {
        Permanent attacker = addCreatureReady(player1, new MyrMoonvessel());
        harness.addToBattlefieldAndReturn(player1, new NemesisMask()).setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new HoverguardObserver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Re-equipping moves the blocking requirement to the new creature")
    void reequippingMovesBlockingRequirement() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        Permanent oldCreature = addCreatureReady(player1, new MyrMoonvessel());
        Permanent newCreature = addCreatureReady(player1, new MyrMoonvessel());
        mask.setAttachedTo(oldCreature.getId());
        Permanent blocker = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();
        assertThat(mask.getAttachedTo()).isEqualTo(newCreature.getId());

        declareAttackersAndPrepareBlockers(List.of(1, 2));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        Permanent creature = addCreatureReady(player2, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new NemesisMask());
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

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
