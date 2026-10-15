package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Phytotitan.class, TormodsCrypt.class})
class PhytotitanTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns tapped at the beginning of its owner's next upkeep")
    void diesThenReturnsTappedAtOwnersNextUpkeep() {
        killPhytotitan(player1);

        harness.assertInGraveyard(player1, "Phytotitan");
        harness.assertNotOnBattlefield(player1, "Phytotitan");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // the end step must not return it

        harness.assertNotOnBattlefield(player1, "Phytotitan");

        runUpkeepOf(player1);

        Permanent returned = findPermanent(player1, "Phytotitan");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Phytotitan");
    }

    @Test
    @DisplayName("Does not return at an opponent's upkeep")
    void doesNotReturnAtOpponentUpkeep() {
        killPhytotitan(player1);

        runUpkeepOf(player2);

        harness.assertNotOnBattlefield(player1, "Phytotitan");
        harness.assertInGraveyard(player1, "Phytotitan");

        runUpkeepOf(player1);

        harness.assertOnBattlefield(player1, "Phytotitan");
    }

    @Test
    @DisplayName("Returns only once per death")
    void returnsOnlyOncePerDeath() {
        killPhytotitan(player1);

        runUpkeepOf(player1);
        harness.assertOnBattlefield(player1, "Phytotitan");

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Phytotitan"));

        runUpkeepOf(player1);

        harness.assertNotOnBattlefield(player1, "Phytotitan");
    }

    @Test
    @DisplayName("A stolen Phytotitan returns at its owner's upkeep under its owner's control")
    void stolenTitanReturnsAtOwnersUpkeep() {
        killStolenPhytotitan();

        runUpkeepOf(player1);

        assertThat(findPermanent(player1, "Phytotitan").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Phytotitan");
        harness.assertNotInGraveyard(player1, "Phytotitan");
    }

    @Test
    @DisplayName("A stolen Phytotitan does not return at its former controller's upkeep")
    void stolenTitanWaitsThroughFormerControllersUpkeep() {
        killStolenPhytotitan();

        runUpkeepOf(player2);

        harness.assertNotOnBattlefield(player1, "Phytotitan");
        harness.assertNotOnBattlefield(player2, "Phytotitan");
        harness.assertInGraveyard(player1, "Phytotitan");

        runUpkeepOf(player1);
        assertThat(findPermanent(player1, "Phytotitan").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiling Phytotitan in response to its delayed trigger prevents its return")
    void exileInResponseToDelayedTriggerPreventsReturn() {
        harness.addToBattlefield(player2, new TormodsCrypt());
        killPhytotitan(player1);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Phytotitan");
        harness.assertNotInGraveyard(player1, "Phytotitan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Phytotitan"));
    }

    @Test
    @DisplayName("Exiling Phytotitan before its dies trigger resolves prevents its return")
    void exileInResponseToDiesTriggerPreventsReturn() {
        harness.addToBattlefield(player2, new TormodsCrypt());
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new Phytotitan());
        titan.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();
        runUpkeepOf(player1);

        harness.assertNotOnBattlefield(player1, "Phytotitan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(titan.getCard().getId()));
    }

    @Test
    @DisplayName("Dying during its owner's upkeep waits for a later upkeep")
    void dyingDuringUpkeepWaitsForNextUpkeep() {
        advanceToUpkeep(player1);
        killPhytotitan(player1);
        harness.assertInGraveyard(player1, "Phytotitan");
        harness.assertNotOnBattlefield(player1, "Phytotitan");

        runUpkeepOf(player2);
        harness.assertInGraveyard(player1, "Phytotitan");
        runUpkeepOf(player1);
        assertThat(findPermanent(player1, "Phytotitan").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Phytotitan can return again after a second death")
    void returnsAfterSecondDeath() {
        killPhytotitan(player1);
        runUpkeepOf(player1);

        Permanent titan = findPermanent(player1, "Phytotitan");
        titan.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Phytotitan");

        runUpkeepOf(player1);
        assertThat(findPermanent(player1, "Phytotitan").isTapped()).isTrue();
        assertThat(countPermanents(player1, "Phytotitan")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Phytotitan");
    }

    private void killStolenPhytotitan() {
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new Phytotitan());
        gd.stolenCreatures.put(titan.getId(), player1.getId());
        titan.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Phytotitan");
    }

    private void runUpkeepOf(Player player) {
        advanceToUpkeep(player);
        resolveAllTriggers();
    }

    private void killPhytotitan(Player player) {
        Permanent titan = harness.addToBattlefieldAndReturn(player, new Phytotitan());
        titan.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
    }

}
