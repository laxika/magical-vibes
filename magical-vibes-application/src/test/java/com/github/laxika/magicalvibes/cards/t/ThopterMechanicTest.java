package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThopterMechanic.class, GrizzlyBears.class, WrathOfGod.class})
class ThopterMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself for the second card drawn each turn")
    void triggersOnSecondCardDrawn() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Thopter token when it dies")
    void deathCreatesThopterToken() {
        harness.addToBattlefield(player1, new ThopterMechanic());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(1);
        assertThat(thopters.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not trigger the mechanic")
    void ignoresOpponentDraws() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player2, List.of(new ThopterMechanic(), new ThopterMechanic()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second draw also triggers during an opponent's turn")
    void triggersDuringOpponentTurn() {
        harness.forceActivePlayer(player2);
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player1, List.of(new ThopterMechanic(), new ThopterMechanic()));

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts the first draw even when it occurred before entering")
    void countsDrawBeforeEntering() {
        harness.setLibrary(player1, List.of(new ThopterMechanic(), new ThopterMechanic()));
        drawAndResolveTrigger(player1);
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());

        drawAndResolveTrigger(player1);

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void doesNotCountOnlyDrawsSinceEntering() {
        harness.setLibrary(player1, List.of(new ThopterMechanic(), new ThopterMechanic(), new ThopterMechanic()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());

        drawAndResolveTrigger(player1);

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Drawing a third card before resolution does not invalidate the second-draw trigger")
    void thirdDrawDoesNotInvalidatePendingTrigger() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player1, List.of(new ThopterMechanic(), new ThopterMechanic(), new ThopterMechanic()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Simultaneous deaths create a Thopter for each mechanic's controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new ThopterMechanic());
        harness.addToBattlefield(player2, new ThopterMechanic());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (Player player : List.of(player1, player2)) {
            List<Permanent> thopters = findPermanents(player, "Thopter");
            assertThat(thopters).hasSize(1);
            Permanent token = thopters.getFirst();
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("The second-draw count resets at the start of the next turn")
    void triggersAgainOnNextTurn() {
        Permanent mechanic = harness.addToBattlefieldAndReturn(player1, new ThopterMechanic());
        harness.setLibrary(player1, List.of(new ThopterMechanic(), new ThopterMechanic(),
                new ThopterMechanic(), new ThopterMechanic()));
        harness.setLibrary(player2, List.of(new ThopterMechanic()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        drawAndResolveTrigger(player1);
        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        drawAndResolveTrigger(player1);

        assertThat(mechanic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
