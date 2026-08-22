package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DavrielRogueShadowmage.class, GrizzlyBears.class})
class DavrielRogueShadowmageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent with one or fewer cards during that opponent's upkeep")
    void dealsDamageWhenOpponentHasOneOrFewerCards() {
        harness.addToBattlefield(player1, new DavrielRogueShadowmage());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger when the opponent has more than one card")
    void doesNotDealDamageWhenOpponentHasMoreThanOneCard() {
        harness.addToBattlefield(player1, new DavrielRogueShadowmage());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Rechecks the opponent's hand size when the trigger resolves")
    void rechecksHandSizeAtResolution() {
        harness.addToBattlefield(player1, new DavrielRogueShadowmage());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("-1 makes the targeted player discard a card")
    void minusOneMakesTargetPlayerDiscard() {
        Permanent davriel = addReadyDavriel(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(davriel.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private Permanent addReadyDavriel(Player player) {
        Permanent davriel = harness.addToBattlefieldAndReturn(player, new DavrielRogueShadowmage());
        davriel.setCounterCount(CounterType.LOYALTY, 3);
        davriel.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return davriel;
    }
}
