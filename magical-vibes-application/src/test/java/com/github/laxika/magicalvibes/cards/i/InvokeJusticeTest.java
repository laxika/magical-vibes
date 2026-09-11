package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BoostedSloop;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeJustice.class, GrizzlyBears.class, BoostedSloop.class})
class InvokeJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent and distributes counters among the target player's creatures and Vehicles")
    void returnsPermanentAndDistributesCounters() {
        Card graveyardPermanent = new GrizzlyBears();
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetVehicle = harness.addToBattlefieldAndReturn(player2, new BoostedSloop());
        Permanent casterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardPermanent.getId(),
                List.of(player2.getId(), targetCreature.getId(), targetVehicle.getId()),
                Map.of(targetCreature.getId(), 1, targetVehicle.getId(), 3));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(graveyardPermanent.getId()));
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targetVehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(casterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rejects counter targets not controlled by the chosen player")
    void rejectsPermanentsNotControlledByTargetPlayer() {
        Card graveyardPermanent = new GrizzlyBears();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardPermanent.getId(),
                List.of(player2.getId(), ownCreature.getId()), Map.of(ownCreature.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }
}
