package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimanaSellSword.class, GrizzlyBears.class, Conspiracy.class})
class NimanaSellSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.castFromHand(player1, new NimanaSellSword(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sellSword = findPermanent(player1, "Nimana Sell-Sword");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(sellSword.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally entry triggers both the existing and entering Sell-Swords")
    void anotherAllyEntryTriggersBothSellSwords() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new NimanaSellSword());
        harness.castFromHand(player1, new NimanaSellSword(), "{3}{B}");
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent sellSword = harness.addToBattlefieldAndReturn(player1, new NimanaSellSword());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(sellSword.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.castFromHand(player1, new NimanaSellSword(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Nimana Sell-Sword")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentAllyEntryDoesNotTriggerControlledSellSword() {
        Permanent opposingSellSword = harness.addToBattlefieldAndReturn(player2, new NimanaSellSword());

        harness.castFromHand(player1, new NimanaSellSword(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opposingSellSword.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ownEntryStillTriggersWhenConspiracyRemovesAllyType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new NimanaSellSword(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Nimana Sell-Sword")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
