package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightTheWay.class, AirResponseUnit.class, FountainOfYouth.class, GrizzlyBears.class})
class LightTheWayTest extends BaseCardTest {

    @Test
    void putsCounterOnAndUntapsTargetCreature() {
        Permanent target = tappedPermanent(player1, new GrizzlyBears());

        cast(0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnAndUntapsTargetVehicle() {
        Permanent target = tappedPermanent(player1, new AirResponseUnit());

        cast(0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returnsTargetPermanentYouControlToYourHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        cast(1, target.getId());

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Fountain of Youth");
    }

    @Test
    void rejectsPermanentControlledByOpponentForBounceMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new LightTheWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    private void cast(int modeIndex, UUID targetId) {
        harness.setHand(player1, List.of(new LightTheWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of(targetId));
        harness.passBothPriorities();
    }

    private Permanent tappedPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        permanent.tap();
        return permanent;
    }
}
