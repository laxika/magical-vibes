package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrowFromTheSaddle.class, GrizzlyBears.class, HillGiant.class})
class ThrowFromTheSaddleTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Mount gets +1/+1 until end of turn and deals its boosted power")
    void boostsNonMountAndDealsBoostedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castThrow(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Mount gets a +1/+1 counter instead and deals its power")
    void putsCounterOnMountAndDealsPower() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(mount).setSubtypes(List.of(CardSubtype.MOUNT));
        harness.addToBattlefield(player2, new HillGiant());
        castThrow(mount);

        assertThat(mount.getPowerModifier()).isZero();
        assertThat(mount.getToughnessModifier()).isZero();
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The spell requires a creature you control and a creature you do not control")
    void restrictsTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ThrowFromTheSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castThrow(Permanent target) {
        harness.setHand(player1, List.of(new ThrowFromTheSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(target.getId(),
                harness.getPermanentId(player2, "Hill Giant")));
        harness.passBothPriorities();
    }
}
