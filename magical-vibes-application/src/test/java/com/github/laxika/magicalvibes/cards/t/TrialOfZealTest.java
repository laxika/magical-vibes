package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.g.GustWalker;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfZeal.class, CartoucheOfSolidarity.class, GustWalker.class, GideonOfTheTrials.class})
class TrialOfZealTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to a planeswalker")
    void etbDealsDamageToPlaneswalker() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, gideon.getId());
        resolveAllTriggers();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Gideon of the Trials");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Another Trial entering does not return the first Trial")
    void staysWhenNonCartoucheEnchantmentEnters() {
        harness.addToBattlefield(player1, new TrialOfZeal());
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Trial of Zeal")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("One Cartouche returns every Trial its controller controls")
    void cartoucheReturnsMultipleTrials() {
        harness.addToBattlefield(player1, new TrialOfZeal());
        harness.addToBattlefield(player1, new TrialOfZeal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GustWalker());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Trial of Zeal");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allSatisfy(card -> assertThat(card).isInstanceOf(TrialOfZeal.class));
        harness.assertOnBattlefield(player1, "Cartouche of Solidarity");
    }

    @Test
    @DisplayName("ETB deals 3 damage to target creature, killing a 2/2")
    void etbDealsDamageToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GustWalker());

        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Gust Walker");
        harness.assertInGraveyard(player2, "Gust Walker");
    }

    @Test
    @DisplayName("ETB deals 3 damage to target player")
    void etbDealsDamageToPlayer() {
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Returns to hand when a Cartouche you control enters")
    void bouncesWhenAllyCartoucheEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GustWalker());
        harness.addToBattlefield(player1, new TrialOfZeal());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Trial of Zeal");
        harness.assertInHand(player1, "Trial of Zeal");
    }

    @Test
    @DisplayName("Does not return when a Cartouche enters under an opponent's control")
    void staysWhenOpponentCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfZeal());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GustWalker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, opponentBears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Trial of Zeal");
    }
}
