package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowWhole.class, AlmightyBrushwagg.class})
class SwallowWholeTest extends BaseCardTest {

    @Test
    void exilesTappedCreatureAndCountersCreatureTappedToPay() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        harness.passBothPriorities();

        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    void cannotCastWithoutTappingAnUntappedCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        harness.setHand(player1, List.of(new SwallowWhole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paymentCreature.isTapped()).isFalse();
    }

    @Test
    void cannotTargetUntappedCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());

        harness.setHand(player1, List.of(new SwallowWhole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of(paymentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
        assertThat(paymentCreature.isTapped()).isFalse();
    }

    @Test
    void fizzlesIfTargetBecomesUntappedBeforeResolution() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canTapSummoningSickCreatureToPayCost() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        paymentCreature.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillExilesAndAddsCounterIfPaymentCreatureBecomesUntapped() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        paymentCreature.untap();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(paymentCreature.isTapped()).isFalse();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillExilesIfPaymentCreatureLeavesBattlefield() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        gd.playerBattlefields.get(player1.getId()).remove(paymentCreature);
        gd.playerGraveyards.get(player1.getId()).add(paymentCreature.getCard());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canExileAnotherTappedCreatureYouControl() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(paymentCreature);
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotPayWithAlreadyTappedCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        paymentCreature.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        assertThatThrownBy(() -> castSwallowWhole(paymentCreature, target))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }

    @Test
    void cannotPayWithOpponentsCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        assertThatThrownBy(() -> castSwallowWhole(paymentCreature, target))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paymentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapMoreThanOneCreatureToPayCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();
        harness.setHand(player1, List.of(new SwallowWhole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterFollowsPaymentCreatureIfItsControllerChanges() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        gd.playerBattlefields.get(player1.getId()).remove(paymentCreature);
        gd.playerBattlefields.get(player2.getId()).add(paymentCreature);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noCounterIfTargetLeavesBattlefieldBeforeResolution() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        target.tap();

        castSwallowWhole(paymentCreature, target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSwallowWhole(Permanent paymentCreature, Permanent target) {
        harness.setHand(player1, List.of(new SwallowWhole()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(paymentCreature.getId()));
    }
}
