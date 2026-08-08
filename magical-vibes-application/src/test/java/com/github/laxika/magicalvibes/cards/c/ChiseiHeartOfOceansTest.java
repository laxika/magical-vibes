package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChiseiHeartOfOceansTest extends BaseCardTest {

    private boolean controlsChisei(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Chisei, Heart of Oceans"));
    }

    @Test
    @DisplayName("Removing a counter from a permanent you control keeps Chisei")
    void removingACounterKeepsIt() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsChisei(player1)).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the counter removal sacrifices Chisei")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsChisei(player1)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("With no counters anywhere Chisei is sacrificed without a prompt")
    void noCountersSacrificesImmediately() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(controlsChisei(player1)).isFalse();
    }

    @Test
    @DisplayName("Counters on an opponent's permanent do not pay the cost")
    void opponentCountersDoNotCount() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(controlsChisei(player1)).isFalse();
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Several counter-bearing permanents pause for a choice")
    void multipleCandidatesPromptForChoice() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(controlsChisei(player1)).isTrue();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(controlsChisei(player1)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A counter on Chisei itself can pay for it")
    void chiseiOwnCounterPays() {
        Permanent chisei = harness.addToBattlefieldAndReturn(player1, new ChiseiHeartOfOceans());
        chisei.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsChisei(player1)).isTrue();
        assertThat(chisei.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
