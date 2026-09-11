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

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(graveyardPermanent.getId()));
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targetVehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(casterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The returned permanent may receive all four counters")
    void returnedPermanentCanReceiveCounters() {
        Card graveyardPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, List.of(graveyardPermanent.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Recipients with shroud may receive counters because they are not targets")
    void shroudDoesNotPreventDistribution() {
        Card graveyardPermanent = new GrizzlyBears();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        recipient.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.SHROUD);
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("A targeted player with no eligible permanents receives no counters")
    void noEligibleRecipientsStillReturnsPermanent() {
        Card graveyardPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardPermanent));
        harness.setHand(player1, List.of(new InvokeJustice()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, List.of(graveyardPermanent.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
