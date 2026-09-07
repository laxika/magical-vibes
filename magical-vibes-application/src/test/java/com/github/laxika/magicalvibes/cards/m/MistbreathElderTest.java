package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistbreathElder.class, GrizzlyBears.class})
class MistbreathElderTest extends BaseCardTest {

    @Test
    void returnsAnotherCreatureAndGetsCounter() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void withNoOtherCreatureMayReturnItself() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elder);
        assertThat(gd.playerHands.get(player1.getId())).contains(elder.getCard());
    }

    @Test
    void decliningSelfReturnDoesNothing() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elder);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
