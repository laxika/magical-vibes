package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThunderSalvo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VadmirNewBlood.class, Shock.class, ThunderSalvo.class})
class VadmirNewBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller commits a crime")
    void putsCounterOnCrime() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());

        castShockAtOpponent();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vadmir)).isEqualTo(3);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has menace and lifelink with four or more +1/+1 counters")
    void gainsKeywordsAtFourCounters() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());

        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isFalse();

        vadmir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Targeting yourself does not trigger or use up the crime trigger")
    void targetingYourselfDoesNotConsumeTrigger() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's crime does not trigger Vadmir")
    void opponentsCrimeDoesNotTrigger() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Targeting an opposing creature triggers Vadmir before the spell resolves")
    void targetingOpposingCreatureTriggersBeforeResolution() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        Permanent opponentVadmir = harness.addToBattlefieldAndReturn(player2, new VadmirNewBlood());
        harness.setHand(player1, List.of(new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, opponentVadmir.getId());
        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentVadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Vadmir, New Blood");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Vadmir, New Blood");
    }

    @Test
    @DisplayName("The trigger is available again on the opponent's turn")
    void triggerResetsOnOpponentsTurn() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        harness.setLibrary(player2, List.of(new Shock()));
        castShockAtOpponent();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        castShockAtOpponent();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The fourth counter grants both keywords and dropping below four removes them")
    void keywordsFollowCounterThreshold() {
        Permanent vadmir = harness.addToBattlefieldAndReturn(player1, new VadmirNewBlood());
        vadmir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isFalse();

        castShockAtOpponent();

        assertThat(vadmir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isTrue();
        vadmir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isTrue();
        vadmir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vadmir, Keyword.LIFELINK)).isFalse();
    }

    private void castShockAtOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
