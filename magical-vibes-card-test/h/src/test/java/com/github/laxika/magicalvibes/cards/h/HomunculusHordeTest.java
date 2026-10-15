package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomunculusHorde.class, ImprisonedInTheMoon.class})
class HomunculusHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a token copy")
    void secondDrawCreatesTokenCopy() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(3);

        draw();
        assertThat(gd.stack).isEmpty();

        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
        assertThat(horde.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire on later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(3);

        draw();
        draw();
        resolveTopOfStack();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A Horde entering after the first draw triggers on the second draw")
    void enteringAfterFirstDrawStillTriggers() {
        addCardsToDeck(2);
        draw();
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());

        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger the Horde")
    void opponentsDrawDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        harness.setLibrary(player2, IntStream.range(0, 2)
                .mapToObj(i -> new HomunculusHorde()).toList());

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The second draw triggers during the opponent's turn")
    void secondDrawOnOpponentsTurnTriggers() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        harness.forceActivePlayer(player2);
        addCardsToDeck(2);

        draw();
        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Drawing three cards together produces only one trigger")
    void drawingMultipleCardsTriggersOnce() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The copy retains its ability and both Hordes trigger next turn")
    void tokenCopiesCanCreateFurtherCopies() {
        harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(4);
        draw();
        draw();
        resolveTopOfStack();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        draw();
        assertThat(gd.stack).isEmpty();
        draw();
        assertThat(gd.stack).hasSize(2);
        resolveTopOfStack();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The copy is untapped and does not copy counters")
    void tokenDoesNotCopyTappedStateOrCounters() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        horde.tap();
        horde.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCardsToDeck(2);

        draw();
        draw();
        resolveTopOfStack();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(horde.isTapped()).isTrue();
        assertThat(horde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability still creates a copy after the Horde leaves the battlefield")
    void sourceLeavingDoesNotStopCopy() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        addCardsToDeck(2);
        draw();
        draw();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, horde));
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(p -> p.getCard().isToken());
        harness.assertInGraveyard(player1, "Homunculus Horde");
    }

    @Test
    @DisplayName("A Horde that has lost its abilities does not trigger")
    void losingAbilitiesPreventsDrawTrigger() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new HomunculusHorde());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(horde.getId());
        addCardsToDeck(2);

        draw();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void addCardsToDeck(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new HomunculusHorde()).toList());
    }

    private void draw() {
        draw(player1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
