package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BogardanPhoenix.class)
class BogardanPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("First death: returns to battlefield with a death counter")
    void firstDeathReturnsWithDeathCounter() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        killPhoenix(phoenix);

        Permanent returned = findPermanent(player1, "Bogardan Phoenix");
        assertThat(returned.getCounterCount(CounterType.DEATH)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
    }

    @Test
    @DisplayName("Second death (with death counter): exiles from graveyard")
    void secondDeathExiles() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        phoenix.setCounterCount(CounterType.DEATH, 1);
        var cardId = phoenix.getCard().getId();

        killPhoenix(phoenix);

        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(cardId));
    }

    @Test
    @DisplayName("Returns under its controller's control when an opponent owns it")
    void stolenPhoenixReturnsUnderItsControllersControl() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player2, new BogardanPhoenix());
        gd.stolenCreatures.put(phoenix.getId(), player1.getId());

        killPhoenix(phoenix);

        harness.assertOnBattlefield(player2, "Bogardan Phoenix");
        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player2, "Bogardan Phoenix");
    }

    @Test
    @DisplayName("A returned Phoenix is exiled when it dies again")
    void returnedPhoenixIsExiledOnItsNextDeath() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        killPhoenix(phoenix);
        killPhoenix(findPermanent(player1, "Bogardan Phoenix"));

        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getCard().getId()));
    }

    @Test
    @DisplayName("Removing the death counter allows another return")
    void removingDeathCounterAllowsAnotherReturn() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        killPhoenix(phoenix);
        Permanent returned = findPermanent(player1, "Bogardan Phoenix");
        returned.setCounterCount(CounterType.DEATH, 0);
        killPhoenix(returned);

        assertThat(findPermanent(player1, "Bogardan Phoenix").getCounterCount(CounterType.DEATH))
                .isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An old exile trigger cannot affect a new graveyard entry")
    void exileTriggerDoesNotFollowCardIntoNewGraveyardEntry() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BogardanPhoenix());
        phoenix.setCounterCount(CounterType.DEATH, 1);
        phoenix.setMarkedDamage(phoenix.getEffectiveToughness());
        harness.runStateBasedActions();

        // Model the card leaving for a hand and being discarded before the trigger resolves.
        harness.setGraveyard(player1, java.util.List.of());
        harness.setHand(player1, java.util.List.of(phoenix.getCard()));
        harness.setHand(player1, java.util.List.of());
        harness.setGraveyard(player1, java.util.List.of(phoenix.getCard()));
        gd.markGraveyardEntry(phoenix.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bogardan Phoenix");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Bogardan Phoenix");
    }
    private void killPhoenix(Permanent phoenix) {
        phoenix.setMarkedDamage(phoenix.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve death trigger
    }
}
