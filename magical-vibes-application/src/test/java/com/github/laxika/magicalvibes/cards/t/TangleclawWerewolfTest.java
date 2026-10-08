package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleclawWerewolf.class, GrizzlyBears.class})
class TangleclawWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Tangleclaw Werewolf can block two creatures")
    void frontFaceCanBlockTwoCreatures() {
        Permanent werewolf = addReadyWerewolf(player2);
        addAttacker(player1);
        addAttacker(player1);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(werewolf);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("{6}{G} transforms Tangleclaw Werewolf")
    void transformsWithActivatedAbility() {
        Permanent werewolf = addReadyWerewolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThat(werewolf.isTransformed()).isTrue();
    }

    @Test
    void transformRequiresGreenMana() {
        Permanent werewolf = addReadyWerewolf(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformRequiresAllSevenMana() {
        Permanent werewolf = addReadyWerewolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(werewolf.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoPendingActivationsDoNotTransformBackToFront() {
        Permanent werewolf = addReadyWerewolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        resolveAllTriggers();

        assertThat(werewolf.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fibrous Entangler must be blocked if able")
    void backFaceMustBeBlockedIfAble() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        werewolf.setAttacking(true);
        addReadyWerewolf(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    void frontFaceCannotBlockThreeCreatures() {
        addReadyWerewolf(player2);
        for (int i = 0; i < 3; i++) {
            addReadyWerewolf(player1).setAttacking(true);
        }
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFaceCanBlockTwoCreatures() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        addReadyWerewolf(player2).setAttacking(true);
        addReadyWerewolf(player2).setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    void backFaceCannotBlockThreeCreatures() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        for (int i = 0; i < 3; i++) {
            addReadyWerewolf(player2).setAttacking(true);
        }
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFaceAttacksWithoutTapping() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        addReadyWerewolf(player2);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(werewolf.isAttacking()).isTrue();
        assertThat(werewolf.isTapped()).isFalse();
    }

    @Test
    void frontFaceTapsWhenAttacking() {
        Permanent werewolf = addReadyWerewolf(player1);
        addReadyWerewolf(player2);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(werewolf.isAttacking()).isTrue();
        assertThat(werewolf.isTapped()).isTrue();
    }

    @Test
    void backFaceMayRemainUnblockedWhenOnlyDefenderIsTapped() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        werewolf.setAttacking(true);
        addReadyWerewolf(player2).tap();
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void oneBlockerSatisfiesBackFaceRequirement() {
        Permanent werewolf = addReadyWerewolf(player1);
        transform(werewolf);
        werewolf.setAttacking(true);
        addReadyWerewolf(player2);
        addReadyWerewolf(player2);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    void additionalBlockerMustBlockBothEntanglersWhenAble() {
        Permanent first = addReadyWerewolf(player1);
        Permanent second = addReadyWerewolf(player1);
        transform(first);
        transform(second);
        first.setAttacking(true);
        second.setAttacking(true);
        addReadyWerewolf(player2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void additionalBlockerCanSatisfyBothEntanglerRequirements() {
        Permanent first = addReadyWerewolf(player1);
        Permanent second = addReadyWerewolf(player1);
        transform(first);
        transform(second);
        first.setAttacking(true);
        second.setAttacking(true);
        addReadyWerewolf(player2);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ))).doesNotThrowAnyException();
    }

    private Permanent addReadyWerewolf(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new TangleclawWerewolf());
    }

    private void addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = addCreatureReady(player, new GrizzlyBears());
        attacker.setAttacking(true);
    }

    private void transform(Permanent werewolf) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(werewolf);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
