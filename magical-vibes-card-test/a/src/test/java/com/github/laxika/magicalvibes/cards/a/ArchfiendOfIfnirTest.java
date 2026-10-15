package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unburden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendOfIfnir.class, Censor.class, Colossapede.class, GrizzlyBears.class, Unburden.class})
class ArchfiendOfIfnirTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card puts a -1/-1 counter on each creature opponents control")
    void cyclingCountersOpponentCreatures() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // A second cycling card to cycle — cycling is a discard, so it triggers the ability.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the -1/-1 trigger

        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cycling Archfiend of Ifnir itself does not trigger the ability")
    void cyclingSelfDoesNotTrigger() {
        // The ability only functions on the battlefield; cycling Archfiend from hand can't trigger it.
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchfiendOfIfnir()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(oppBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        harness.assertInGraveyard(player1, "Archfiend of Ifnir");
    }

    @Test
    void discardingTwoCardsTriggersOnceForEachCard() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new Censor(), new Colossapede()));
        harness.setHand(player2, List.of(new Unburden()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstOpponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(secondOpponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void opponentsDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new Unburden()));
        harness.setHand(player2, List.of(new Censor(), new Colossapede()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void cyclingAnotherArchfiendTriggersTheBattlefieldCopyOnlyOnce() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        Colossapede drawnCard = new Colossapede();
        harness.setHand(player1, List.of(new ArchfiendOfIfnir()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Archfiend of Ifnir");
    }

    @Test
    void creaturesEnteringBeforeTriggerResolvesReceiveCounters() {
        harness.addToBattlefield(player1, new ArchfiendOfIfnir());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.passBothPriorities();

        assertThat(lateCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
