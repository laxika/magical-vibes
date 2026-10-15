package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TidingsOfWar.class)
class TidingsOfWarTest extends BaseCardTest {

    @Test
    void normalCastAmassesGoblinsOne() {
        harness.setHand(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void flashbackAmassesGoblinsThreeAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Tidings of War");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tidings of War"));
    }

    @Test
    void flashbackAddsThreeCountersToExistingArmyWithoutCreatingAnother() {
        harness.setHand(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent army = findPermanent(player1, "Goblin Army");

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(army);
        harness.assertNotInGraveyard(player1, "Tidings of War");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tidings of War"));
    }

    @Test
    void normalCastAddsOneCounterToExistingArmy() {
        harness.setHand(player1, List.of(new TidingsOfWar(), new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent army = findPermanent(player1, "Goblin Army");

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(army);
    }

    @Test
    void opponentsArmyDoesNotPreventCreatingOwnArmy() {
        harness.setHand(player1, List.of(new TidingsOfWar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent opponentsArmy = findPermanent(player1, "Goblin Army");

        harness.forceActivePlayer(player2);
        harness.ensurePriority(player2);
        harness.setHand(player2, List.of(new TidingsOfWar()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player2, 0, 0);

        Permanent ownArmy = findPermanent(player2, "Goblin Army");
        assertThat(ownArmy.getId()).isNotEqualTo(opponentsArmy.getId());
        assertThat(ownArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentsArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Tidings of War");
    }
}
