package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrialOfSolidarity.class, GrizzlyBears.class, CartoucheOfSolidarity.class, Opalescence.class})
class TrialOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives creatures you control +2/+1 and vigilance, not the opponent's")
    void etbBuffsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment (queues ETB trigger)
        harness.passBothPriorities(); // resolve ETB pump

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mine, Keyword.VIGILANCE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("ETB buff wears off at end of turn")
    void buffWearsOffAtEndOfTurn() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mine, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Returns to hand when a Cartouche you control enters")
    void bouncesWhenAllyCartoucheEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TrialOfSolidarity());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve aura (queues its ETB + Trial's bounce)
        harness.passBothPriorities(); // resolve a triggered ability
        harness.passBothPriorities(); // resolve the other triggered ability

        harness.assertNotOnBattlefield(player1, "Trial of Solidarity");
        harness.assertInHand(player1, "Trial of Solidarity");
    }

    @Test
    @DisplayName("Does not return when a Cartouche enters under an opponent's control")
    void staysWhenOpponentCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfSolidarity());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, opponentBears.getId());
        harness.passBothPriorities(); // resolve aura
        harness.passBothPriorities(); // resolve aura's ETB token trigger

        harness.assertOnBattlefield(player1, "Trial of Solidarity");
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the bonus")
    void creatureEnteringBeforeResolutionIsBuffed() {
        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the bonus")
    void creatureEnteringAfterResolutionIsNotBuffed() {
        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A non-Cartouche enchantment does not return the Trial")
    void staysWhenNonCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfSolidarity());
        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Trial of Solidarity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Trial grants itself vigilance when it is a creature")
    void animatedTrialReceivesItsOwnVigilance() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.setHand(player1, List.of(new TrialOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent trial = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof TrialOfSolidarity)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, trial)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, trial)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, trial, Keyword.VIGILANCE)).isTrue();
    }
}
