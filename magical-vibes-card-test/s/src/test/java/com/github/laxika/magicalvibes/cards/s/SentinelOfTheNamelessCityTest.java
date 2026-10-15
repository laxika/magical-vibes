package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelOfTheNamelessCity.class})
class SentinelOfTheNamelessCityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Map token")
    void etbCreatesMapToken() {
        castSentinel();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking creates a Map token")
    void attackCreatesMapToken() {
        addCreatureReady(player1, new SentinelOfTheNamelessCity());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Each attacking Sentinel creates its own Map without tapping")
    void eachAttackerCreatesMap() {
        var first = addCreatureReady(player1, new SentinelOfTheNamelessCity());
        var second = addCreatureReady(player1, new SentinelOfTheNamelessCity());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(findPermanents(player2, "Map")).isEmpty();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An attack trigger creates its Map after Sentinel leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        var sentinel = addCreatureReady(player1, new SentinelOfTheNamelessCity());
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        gd.playerGraveyards.get(player1.getId()).add(sentinel.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("A Map is sacrificed to make its target explore an empty library")
    void mapExploresEmptyLibrary() {
        castSentinel();
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var sentinel = findPermanent(player1, "Sentinel of the Nameless City");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, sentinel.getId());
        assertThat(findPermanents(player1, "Map")).isEmpty();
        harness.passBothPriorities();

        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Map's explore can keep a revealed nonland on top")
    void mapExploreKeepsNonland() {
        castSentinel();
        var revealed = new SentinelOfTheNamelessCity();
        harness.setLibrary(player1, List.of(revealed));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var sentinel = findPermanent(player1, "Sentinel of the Nameless City");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, sentinel.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    private void castSentinel() {
        harness.setHand(player1, List.of(new SentinelOfTheNamelessCity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
