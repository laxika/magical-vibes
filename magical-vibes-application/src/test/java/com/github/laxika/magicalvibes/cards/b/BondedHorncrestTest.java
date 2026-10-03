package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondedHorncrest.class, DeepFreeze.class})
class BondedHorncrestTest extends BaseCardTest {

    @Test
    void cannotAttackAloneEvenWithAnUndeclaredCompanion() {
        addCreatureReady(player1, new BondedHorncrest());
        addCreatureReady(player1, new BondedHorncrest());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    void twoHorncrestsCanAttackTogether() {
        Permanent first = addCreatureReady(player1, new BondedHorncrest());
        Permanent second = addCreatureReady(player1, new BondedHorncrest());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isTrue();
    }

    @Test
    void mayDeclineToAttack() {
        addCreatureReady(player1, new BondedHorncrest());

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    void cannotBlockAloneEvenWithAnUndeclaredCompanion() {
        prepareCombatWithTwoAttackers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block alone");
    }

    @Test
    void twoHorncrestsCanBlockTheSameAttacker() {
        prepareCombatWithTwoAttackers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void twoHorncrestsCanBlockDifferentAttackers() {
        prepareCombatWithTwoAttackers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    void mayDeclineToBlock() {
        prepareCombatWithTwoAttackers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void isOfferedAsABlockerAfterLosingItsAbilities() {
        addCreatureReady(player1, new BondedHorncrest());
        addCreatureReady(player1, new BondedHorncrest());
        Permanent horncrest = addCreatureReady(player2, new BondedHorncrest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DeepFreeze());
        aura.setAttachedTo(horncrest.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .contains(0);
    }

    @Test
    void canBlockAloneAfterLosingItsAbilities() {
        addCreatureReady(player1, new BondedHorncrest());
        addCreatureReady(player1, new BondedHorncrest());
        Permanent horncrest = addCreatureReady(player2, new BondedHorncrest());
        addCreatureReady(player2, new BondedHorncrest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DeepFreeze());
        aura.setAttachedTo(horncrest.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    private void prepareCombatWithTwoAttackers() {
        addCreatureReady(player1, new BondedHorncrest());
        addCreatureReady(player1, new BondedHorncrest());
        addCreatureReady(player2, new BondedHorncrest());
        addCreatureReady(player2, new BondedHorncrest());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
    }
}
