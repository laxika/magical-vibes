package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JhessianZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SewnEyeDrake.class, JhessianZombies.class})
class SewnEyeDrakeTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyWhenCastWithBlueMana() {
        castAndAttack(ManaColor.BLUE);
    }

    @Test
    void canAttackImmediatelyWhenCastWithRedMana() {
        castAndAttack(ManaColor.RED);
    }

    @Test
    void cannotBeBlockedByGroundCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SewnEyeDrake());
        drake.setAttacking(true);
        harness.addToBattlefield(player2, new JhessianZombies());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByAnotherFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SewnEyeDrake());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SewnEyeDrake());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SewnEyeDrake());
        drake.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castAndAttack(ManaColor hybridPayment) {
        SewnEyeDrake card = new SewnEyeDrake();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, hybridPayment, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent drake = findPermanent(player1, "Sewn-Eye Drake");
        assertThat(drake.getOriginalCard()).isSameAs(card);
        assertThat(drake.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
