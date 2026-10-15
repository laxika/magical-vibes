package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LadySpiderMaybelleReilly.class, Censor.class, GiantSpider.class, GrizzlyBears.class})
class LadySpiderMaybelleReillyTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card queues a target Spider you control")
    void discardQueuesSpiderTarget() {
        harness.addToBattlefield(player1, new LadySpiderMaybelleReilly());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
        assertThat(choice.validPermanentIds()).contains(spider.getId()).doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Discarding a card puts a +1/+1 counter on the chosen Spider you control")
    void discardPutsCounterOnChosenSpider() {
        harness.addToBattlefield(player1, new LadySpiderMaybelleReilly());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger cannot target an opponent's Spider")
    void cannotTargetOpponentsSpider() {
        harness.addToBattlefield(player1, new LadySpiderMaybelleReilly());
        Permanent opponentSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentSpider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lady Spider can put the discard counter on herself")
    void discardCanTargetLadySpiderHerself() {
        Permanent ladySpider = harness.addToBattlefieldAndReturn(player1, new LadySpiderMaybelleReilly());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ladySpider.getId());
        harness.passBothPriorities();

        assertThat(ladySpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent discarding a card does not trigger Lady Spider")
    void opponentDiscardDoesNotTrigger() {
        Permanent ladySpider = harness.addToBattlefieldAndReturn(player1, new LadySpiderMaybelleReilly());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ladySpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each discard can put another counter on the same Spider")
    void consecutiveDiscardsEachPutCounter() {
        Permanent ladySpider = harness.addToBattlefieldAndReturn(player1, new LadySpiderMaybelleReilly());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new Censor(), new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ladySpider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ladySpider.getId());
        harness.passBothPriorities();

        assertThat(ladySpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
