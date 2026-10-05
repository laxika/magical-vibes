package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KangTemporalTyrant.class, Mountain.class})
class KangTemporalTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking connives and puts a +1/+1 counter on Kang after discarding a nonland")
    void attackingConnives() {
        Permanent kang = addReadyKang();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new KangTemporalTyrant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Kang, Temporal Tyrant");

        assertThat(kang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Drains each opponent and gains life on the second card drawn each turn")
    void triggersOnSecondCardDraw() {
        harness.addToBattlefield(player1, new KangTemporalTyrant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding a land to connive does not add a counter")
    void landDiscardDoesNotAddCounter() {
        Permanent kang = addReadyKang();
        harness.setHand(player1, List.of(new KangTemporalTyrant()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(kang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Kang, Temporal Tyrant");
    }

    @Test
    @DisplayName("Kang's current controller connives when control changes before the attack trigger resolves")
    void currentControllerConnivesAfterControlChange() {
        Permanent kang = addReadyKang();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new KangTemporalTyrant()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        gd.playerBattlefields.get(player1.getId()).remove(kang);
        gd.playerBattlefields.get(player2.getId()).add(kang);
        kang.setAttacking(false);
        resolveTopOfStack();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Kang, Temporal Tyrant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The second card drawn while conniving triggers the drain after the discard")
    void connivingSecondDrawTriggersDrain() {
        Permanent kang = addReadyKang();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new KangTemporalTyrant()));
        draw(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            discardByName("Kang, Temporal Tyrant");
            resolveAllTriggers();
        });

        assertThat(kang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The controller's second draw on an opponent's turn triggers the drain")
    void secondDrawOnOpponentsTurnTriggers() {
        harness.addToBattlefield(player1, new KangTemporalTyrant());
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger Kang")
    void opponentsDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new KangTemporalTyrant());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second-draw trigger resolves after Kang leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new KangTemporalTyrant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveTopOfStack();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private Permanent addReadyKang() {
        return addCreatureReady(player1, new KangTemporalTyrant());
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
