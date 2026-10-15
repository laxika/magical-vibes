package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunedServitor.class})
class RunedServitorTest extends BaseCardTest {

    @Test
    @DisplayName("When Runed Servitor dies, each player draws a card")
    void diesThenEachPlayerDraws() {
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        Permanent servitor = harness.addToBattlefieldAndReturn(player1, new RunedServitor());
        servitor.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Runed Servitor");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1 + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }

    @Test
    @DisplayName("Sacrificing the opponent's Servitor makes both players draw on resolution")
    void sacrificedOpponentsServitorDrawsForBothPlayers() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RunedServitor()));
        harness.setLibrary(player2, List.of(new RunedServitor()));
        Permanent servitor = harness.addToBattlefieldAndReturn(player2, new RunedServitor());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, servitor));
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Runed Servitor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exiling Runed Servitor does not trigger a draw")
    void exileDoesNotTriggerDraw() {
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();
        Permanent servitor = harness.addToBattlefieldAndReturn(player1, new RunedServitor());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, servitor));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Runed Servitor");
        harness.assertNotInGraveyard(player1, "Runed Servitor");
        assertThat(gd.findExiledCard(servitor.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2);
    }

    @Test
    @DisplayName("Two Servitors dying together each make both players draw")
    void simultaneousDeathsEachTriggerDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RunedServitor(), new RunedServitor()));
        harness.setLibrary(player2, List.of(new RunedServitor(), new RunedServitor()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RunedServitor());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Runed Servitor");
        harness.assertInGraveyard(player2, "Runed Servitor");
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
