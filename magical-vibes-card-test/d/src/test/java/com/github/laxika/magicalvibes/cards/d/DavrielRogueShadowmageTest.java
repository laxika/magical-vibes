package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Banehound;
import com.github.laxika.magicalvibes.cards.l.LazotepPlating;
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

@CardUsed({DavrielRogueShadowmage.class, Banehound.class, LazotepPlating.class})
class DavrielRogueShadowmageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent with one or fewer cards during that opponent's upkeep")
    void dealsDamageWhenOpponentHasOneOrFewerCards() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of(new Banehound()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger when the opponent has more than one card")
    void doesNotDealDamageWhenOpponentHasMoreThanOneCard() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of(new Banehound(), new Banehound()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Rechecks the opponent's hand size when the trigger resolves")
    void rechecksHandSizeAtResolution() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of(new Banehound()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).add(new Banehound());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("-1 makes the targeted player discard a card")
    void minusOneMakesTargetPlayerDiscard() {
        Permanent davriel = addReadyDavriel(player1);
        harness.setHand(player2, List.of(new Banehound(), new Banehound()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(davriel.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage when the opponent has no cards")
    void dealsDamageWithEmptyHand() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerDuringControllerUpkeep() {
        addReadyDavriel(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("Upkeep damage does not target and ignores hexproof")
    void upkeepDamageIgnoresHexproof() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of(new LazotepPlating()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.castFromHand(player2, new LazotepPlating(), "{1}{U}");
        harness.passBothPriorities();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("A failed upkeep condition cannot become a trigger later in the upkeep")
    void noTriggerWhenHandShrinksAfterUpkeepBegins() {
        addReadyDavriel(player1);
        harness.setHand(player2, List.of(new Banehound(), new Banehound()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).removeFirst();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("The controller can target themselves with the discard ability")
    void minusOneCanTargetController() {
        Permanent davriel = addReadyDavriel(player1);
        harness.setHand(player1, List.of(new Banehound(), new Banehound()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Banehound");
        assertThat(davriel.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Discard resolves after spending Davriel's last loyalty counter")
    void minusOneResolvesAfterDavrielDies() {
        Permanent davriel = addReadyDavriel(player1);
        davriel.setCounterCount(CounterType.LOYALTY, 1);
        harness.setHand(player2, List.of(new Banehound(), new Banehound()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Davriel, Rogue Shadowmage");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Banehound");
    }

    @Test
    @DisplayName("Targeting an empty hand still spends loyalty and completes normally")
    void minusOneWithEmptyHand() {
        Permanent davriel = addReadyDavriel(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(davriel.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
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
