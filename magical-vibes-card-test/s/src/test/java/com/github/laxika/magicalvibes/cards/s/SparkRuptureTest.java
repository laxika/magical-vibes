package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkRupture.class, SarkhanTheMasterless.class, Forest.class})
class SparkRuptureTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsACard() {
        harness.setHand(player1, List.of(new SparkRupture()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Turns planeswalkers with loyalty into creatures with loyalty-based power and toughness")
    void turnsPlaneswalkersIntoLoyaltyBasedCreatures() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        sarkhan.setSummoningSick(false);

        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power and toughness track loyalty counters and the effect stops at zero")
    void tracksLoyaltyCounters() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 3);

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(3);

        sarkhan.setCounterCount(CounterType.LOYALTY, 6);
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(6);

        sarkhan.setCounterCount(CounterType.LOYALTY, 0);
        assertThat(gqs.isCreature(gd, sarkhan)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isTrue();
    }

    @Test
    @DisplayName("Planeswalkers enter with loyalty counters while Spark Rupture is present")
    void planeswalkerEntersAsCreatureWithLoyalty() {
        harness.addToBattlefield(player1, new SparkRupture());
        harness.setHand(player1, List.of(new SarkhanTheMasterless()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        Permanent sarkhan = findPermanent(player1, "Sarkhan the Masterless");
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(5);
        assertThat(gs.getEffectiveActivatedAbilities(gd, sarkhan)).isEmpty();
    }

    @Test
    @DisplayName("Removing all loyalty counters sends the restored planeswalker to the graveyard")
    void zeroLoyaltyPlaneswalkerDies() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 1);
        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();

        sarkhan.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Sarkhan the Masterless");
        harness.assertInGraveyard(player2, "Sarkhan the Masterless");
    }

    @Test
    @DisplayName("+1/+1 counters modify the loyalty-based base power and toughness")
    void creatureCountersApplyAfterLoyaltyBasedPowerAndToughness() {
        harness.addToBattlefield(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 3);
        sarkhan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(5);
    }

    @Test
    @DisplayName("Leaving the battlefield restores planeswalker type and loyalty abilities")
    void leavingRestoresPlaneswalkerAbilities() {
        Permanent rupture = harness.addToBattlefieldAndReturn(player1, new SparkRupture());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, sarkhan)).isEmpty();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, rupture));

        assertThat(gqs.isCreature(gd, sarkhan)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, sarkhan)).hasSize(2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Spark Rupture does not affect planeswalkers already turned into Dragons by Sarkhan")
    void previouslyAnimatedPlaneswalkerRetainsPowerAndAbilities() {
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(4);

        harness.setHand(player1, List.of(new SparkRupture()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, sarkhan)).hasSize(2);
    }
}
