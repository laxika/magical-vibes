package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
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

@CardUsed({HiddenPredators.class, ThunderingGiant.class, ArgothianSwine.class,
        EnchantedEvening.class, Rescind.class})
class HiddenPredatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Beast creature when an opponent controls a creature with power 4 or greater")
    void becomesBeastCreatureWhenOpponentControlsCreatureWithPowerAtLeastFour() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new ThunderingGiant());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenPredators)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("Does not trigger when an opponent controls no creature with power 4 or greater")
    void doesNotTriggerBelowPowerThreshold() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new ArgothianSwine());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isFalse();
    }

    @Test
    @DisplayName("A qualifying creature controlled by this card's controller does not trigger it")
    void doesNotTriggerForControllerCreature() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player1, new ThunderingGiant());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger again after becoming a creature")
    void doesNotTriggerAgainAfterBecomingCreature() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new ThunderingGiant());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
    }

    @Test
    @DisplayName("Can trigger again when a continuous effect keeps it an enchantment")
    void canTriggerAgainWhenContinuousEffectKeepsItAnEnchantment() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player1, new EnchantedEvening());
        harness.addToBattlefield(player2, new ThunderingGiant());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(hiddenPredators.getId());
    }

    @Test
    @DisplayName("Triggers when cast while an opponent already controls a qualifying creature")
    void triggersForCreatureAlreadyPresentWhenEnchantmentResolves() {
        harness.addToBattlefield(player2, new ThunderingGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new HiddenPredators(), "{G}");

        harness.passBothPriorities();

        Permanent hiddenPredators = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Hidden Predators"));
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hiddenPredators)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing the opponent's qualifying creature in response does not stop the transformation")
    void transformsEvenWhenQualifyingCreatureLeavesBeforeResolution() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new ThunderingGiant());
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, giant.getId());

        harness.passBothPriorities();

        harness.assertInHand(player2, "Thundering Giant");
        harness.assertNotOnBattlefield(player2, "Thundering Giant");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenPredators)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenPredators)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hiddenPredators)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated state checks and multiple qualifying creatures produce only one pending trigger")
    void doesNotDuplicatePendingStateTrigger() {
        Permanent hiddenPredators = harness.addToBattlefieldAndReturn(player1, new HiddenPredators());
        harness.addToBattlefield(player2, new ThunderingGiant());
        harness.addToBattlefield(player2, new ThunderingGiant());

        harness.runStateBasedActions();
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(hiddenPredators.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenPredators)).isTrue();
    }
}
