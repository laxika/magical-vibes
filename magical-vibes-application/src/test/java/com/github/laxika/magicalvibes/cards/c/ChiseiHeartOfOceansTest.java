package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BakuAltar;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChiseiHeartOfOceans.class, BakuAltar.class, GnarledMass.class})
class ChiseiHeartOfOceansTest extends BaseCardTest {

    @Test
    @DisplayName("Removing a counter from a permanent you control keeps Chisei")
    void removingACounterKeepsIt() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the counter removal sacrifices Chisei")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("With no counters anywhere Chisei is sacrificed without a prompt")
    void noCountersSacrificesImmediately() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        harness.addToBattlefield(player1, new GnarledMass());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Chisei, Heart of Oceans");
    }

    @Test
    @DisplayName("Counters on an opponent's permanent do not pay the cost")
    void opponentCountersDoNotCount() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        opponentBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Several counter-bearing permanents pause for a choice")
    void multipleCandidatesPromptForChoice() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());

        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A counter on a noncreature permanent can pay the cost")
    void nonCreatureCounterPays() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BakuAltar());
        altar.setCounterCount(CounterType.KI, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Choosing a permanent with multiple counter types asks which counter to remove")
    void multipleCounterTypesRequireChoice() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BakuAltar());
        altar.setCounterCount(CounterType.KI, 1);
        altar.setCounterCount(CounterType.CHARGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ki counters");
        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ChiseiHeartOfOceans());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
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

        harness.assertOnBattlefield(player1, "Chisei, Heart of Oceans");
        assertThat(chisei.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
