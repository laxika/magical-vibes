package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

class SivvisValorTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects all damage to the target creature to the spell's controller")
    void redirectsAllDamageToController() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer1 = addReady(player1, new ProdigalPyromancer());
        Permanent pyromancer2 = addReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer1), null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, pyromancer2), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be cast for its alternate cost by tapping a creature while controlling a Plains")
    void castsForAlternateCost() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent paymentCreature = addReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(paymentCreature.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(plains.isTapped()).isFalse();
        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
