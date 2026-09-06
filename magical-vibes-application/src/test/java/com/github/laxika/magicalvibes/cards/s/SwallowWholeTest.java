package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowWhole.class, GrizzlyBears.class})
class SwallowWholeTest extends BaseCardTest {

    @Test
    void exilesTappedCreatureAndCountersCreatureTappedToPay() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player2.getId()).add(target);

        castSwallowWhole(paymentCreature, target);
        harness.passBothPriorities();

        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(paymentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    void cannotCastWithoutTappingAnUntappedCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new SwallowWhole()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paymentCreature.isTapped()).isFalse();
    }

    @Test
    void cannotTargetUntappedCreature() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        target.tap();
        gd.playerBattlefields.get(player2.getId()).add(target);

        castSwallowWhole(paymentCreature, target);
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
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
