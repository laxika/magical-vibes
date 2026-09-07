package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HardHittingQuestion.class, AirElemental.class, ChandraNalaar.class, GrizzlyBears.class})
class HardHittingQuestionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the controlled creature's power to an opposing creature")
    void dealsPowerDamageToOpposingCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(source, target);

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals the controlled creature's power to an opposing planeswalker")
    void dealsPowerDamageToOpposingPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        cast(source, planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects targets outside a controlled creature and opposing creature or planeswalker")
    void rejectsIllegalTargets() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new HardHittingQuestion()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent source, Permanent target) {
        harness.setHand(player1, List.of(new HardHittingQuestion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
