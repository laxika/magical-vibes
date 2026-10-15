package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfStrength.class, CartoucheOfSolidarity.class, Colossapede.class})
class TrialOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 4/2 green Beast creature token")
    void etbCreatesBeastToken() {
        harness.setHand(player1, List.of(new TrialOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.BEAST))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns to hand when a Cartouche you control enters")
    void bouncesWhenAllyCartoucheEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.addToBattlefield(player1, new TrialOfStrength());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Trial of Strength");
        harness.assertInHand(player1, "Trial of Strength");
    }

    @Test
    @DisplayName("Does not return when a Cartouche enters under an opponent's control")
    void staysWhenOpponentCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfStrength());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new Colossapede());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, opponentBears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Trial of Strength");
    }

    @Test
    @DisplayName("Another Trial entering does not return either non-Cartouche enchantment")
    void staysWhenNonCartoucheEnchantmentEnters() {
        harness.addToBattlefield(player1, new TrialOfStrength());
        harness.setHand(player1, List.of(new TrialOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Trial of Strength")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.BEAST)).hasSize(1);
        harness.assertNotInHand(player1, "Trial of Strength");
    }

    @Test
    @DisplayName("One Cartouche returns every Trial controlled by its controller")
    void cartoucheReturnsMultipleTrials() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.addToBattlefield(player1, new TrialOfStrength());
        harness.addToBattlefield(player1, new TrialOfStrength());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Trial of Strength");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof TrialOfStrength).hasSize(2);
        harness.assertOnBattlefield(player1, "Cartouche of Solidarity");
    }

    @Test
    @DisplayName("A controlled Trial returns to its owner rather than its controller")
    void borrowedTrialReturnsToOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent trial = harness.addToBattlefieldAndReturn(player1, new TrialOfStrength());
        gd.stolenCreatures.put(trial.getId(), player2.getId());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Trial of Strength");
        harness.assertInHand(player2, "Trial of Strength");
        harness.assertNotInHand(player1, "Trial of Strength");
    }
}
