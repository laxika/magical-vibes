package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
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

@CardUsed({AvenMimeomancer.class, GrizzledLeotau.class, Snakeform.class})
class AvenMimeomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Feather counter sets the creature's base P/T to 3/1 and grants flying")
    void featherCounterSetsBaseStatsAndGrantsFlying() {
        Permanent leotau = castAvenWithLeotau();

        placeFeatherCounter(leotau);

        assertThat(leotau.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature untouched")
    void decliningLeavesCreatureUnchanged() {
        Permanent leotau = castAvenWithLeotau();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, leotau.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(leotau.getCounterCount(CounterType.FEATHER)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature keeps base P/T 3/1 and flying after Aven Mimeomancer leaves the battlefield")
    void effectPersistsAfterAvenLeaves() {
        Permanent leotau = castAvenWithLeotau();
        placeFeatherCounter(leotau);

        // Aven Mimeomancer leaves — the resolved upkeep effect continues for its stated duration.
        Permanent aven = findAven();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(aven.getId()));
        gd.expireFloatingEffectsForDepartedSource(aven.getId());

        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    @Test
    void movingFeatherCounterDoesNotTransferContinuousEffect() {
        Permanent leotau = castAvenWithLeotau();
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        placeFeatherCounter(leotau);

        leotau.setCounterCount(CounterType.FEATHER, 0);
        recipient.setCounterCount(CounterType.FEATHER, 1);

        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, recipient, Keyword.FLYING)).isFalse();
    }

    @Test
    void expiredEffectDoesNotRestartWhenAnotherFeatherCounterIsAdded() {
        Permanent leotau = castAvenWithLeotau();
        placeFeatherCounter(leotau);

        leotau.setCounterCount(CounterType.FEATHER, 0);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isFalse();

        leotau.setCounterCount(CounterType.FEATHER, 1);

        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isFalse();
    }

    @Test
    void upkeepEffectOverridesEarlierSnakeform() {
        Permanent leotau = castAvenWithLeotau();
        castSnakeform(leotau);

        placeFeatherCounter(leotau);

        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    @Test
    void repeatedUpkeepEffectOverridesInterveningSnakeform() {
        Permanent leotau = castAvenWithLeotau();
        placeFeatherCounter(leotau);
        castSnakeform(leotau);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isFalse();

        placeFeatherCounter(leotau);

        assertThat(leotau.getCounterCount(CounterType.FEATHER)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    @Test
    void removingOneOfMultipleFeatherCountersKeepsEffect() {
        Permanent leotau = castAvenWithLeotau();
        placeFeatherCounter(leotau);
        placeFeatherCounter(leotau);

        leotau.setCounterCount(CounterType.FEATHER, 1);

        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    @Test
    void canTargetOpponentsCreatureAndPreservesPowerToughnessModifiers() {
        castAvenWithLeotau();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        placeFeatherCounter(target);

        assertThat(target.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void upkeepEffectResolvesEvenIfMimeomancerLeavesInResponse() {
        Permanent leotau = castAvenWithLeotau();
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, leotau.getId());

        Permanent aven = findAven();
        gd.playerBattlefields.get(player1.getId()).remove(aven);
        gd.expireFloatingEffectsForDepartedSource(aven.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(leotau.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, leotau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leotau)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.FLYING)).isTrue();
    }

    private void castSnakeform(Permanent target) {
        harness.setLibrary(player1, List.of(new GrizzledLeotau()));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    /** Casts Aven Mimeomancer and returns a 1/5 target. */
    private Permanent castAvenWithLeotau() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AvenMimeomancer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Aven spell → enters
        harness.passBothPriorities(); // resolve ETB rule-establishing trigger
        return leotau;
    }

    private void placeFeatherCounter(Permanent target) {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    private Permanent findAven() {
        return findPermanent(player1, "Aven Mimeomancer");
    }

}
