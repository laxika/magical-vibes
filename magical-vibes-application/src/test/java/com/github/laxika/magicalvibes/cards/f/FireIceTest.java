package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireIce.class, GrizzlyBears.class})
class FireIceTest extends BaseCardTest {

    @Test
    void fireDealsTwoDamageDividedAmongTwoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castFire(Map.of(player2.getId(), 1, bears.getId(), 1),
                List.of(player2.getId(), bears.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fireUsesItsRedModeCost() {
        castFire(Map.of(player2.getId(), 2), List.of(player2.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void iceTapsTargetPermanentAndDrawsACard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        harness.castModalInstant(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void iceCannotTargetAPlayer() {
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFire(Map<java.util.UUID, Integer> damageAssignments, List<java.util.UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);
        gs.playCard(gd, player1, 0, 0, null, damageAssignments, targets, List.of());
        harness.passBothPriorities();
    }

    private void addMana(ManaColor color) {
        harness.addMana(player1, color, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
