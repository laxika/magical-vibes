package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseberryCultivator.class, CamelliaTheSeedmiser.class, BakersbaneDuo.class})
class CorpseberryCultivatorTest extends BaseCardTest {

    @Test
    void mayForageAtBeginningOfCombatAndPutACounterOnIt() {
        harness.setGraveyard(player1, List.of(new CorpseberryCultivator(), new CorpseberryCultivator(), new CorpseberryCultivator()));
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayForageBySacrificingFoodAndPutACounterOnIt() {
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());
        Permanent food = addFoodToken();

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningToForageDoesNotPutACounterOnIt() {
        harness.setGraveyard(player1, List.of(new CorpseberryCultivator(), new CorpseberryCultivator(), new CorpseberryCultivator()));
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersWhenAnotherAbilityCausesItsControllerToForage() {
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());
        harness.addToBattlefield(player1, new CamelliaTheSeedmiser());
        harness.setGraveyard(player1, List.of(new CorpseberryCultivator(), new CorpseberryCultivator(), new CorpseberryCultivator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotForageDuringOpponentsCombat() {
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());
        harness.setGraveyard(player1, List.of(
                new CorpseberryCultivator(), new CorpseberryCultivator(), new CorpseberryCultivator()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotForageWithOnlyTwoGraveyardCardsAndNoFood() {
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());
        harness.setGraveyard(player1, List.of(new CorpseberryCultivator(), new CorpseberryCultivator()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsForagingDoesNotPutACounterOnIt() {
        Permanent cultivator = harness.addToBattlefieldAndReturn(player1, new CorpseberryCultivator());
        harness.addToBattlefield(player2, new CamelliaTheSeedmiser());
        harness.setGraveyard(player2, List.of(
                new CorpseberryCultivator(), new CorpseberryCultivator(), new CorpseberryCultivator()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent addFoodToken() {
        harness.enterBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FOOD))
                .findFirst().orElseThrow();
    }
}
