package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellholeFlailer.class, GiantGrowth.class, JaceArchitectOfThought.class})
class HellholeFlailerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castHellholeFlailer(true);

        Permanent flailer = findPermanent(player1, "Hellhole Flailer");
        assertThat(flailer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, flailer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flailer)).isEqualTo(3);
    }

    @Test
    @DisplayName("{2}{B}{R}, Sacrifice: deals damage equal to its power to target player")
    void dealsPowerDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HellholeFlailer());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Hellhole Flailer");
    }

    @Test
    @DisplayName("Sacrificed unleashed Flailer deals 4 damage from last-known power")
    void unleashedPowerIsUsedForDamage() {
        harness.setLife(player2, 20);
        Permanent flailer = addCreatureReady(player1, new HellholeFlailer());
        flailer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Hellhole Flailer");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new HellholeFlailer());
        Permanent otherFlailer = addCreatureReady(player2, new HellholeFlailer());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherFlailer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningUnleashLeavesNoCounter() {
        castHellholeFlailer(false);

        assertThat(findPermanent(player1, "Hellhole Flailer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureWithPlusOneCounterCannotBlock() {
        Permanent flailer = addCreatureReady(player1, new HellholeFlailer());
        flailer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new HellholeFlailer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastPlusOneCounterAllowsBlocking() {
        Permanent flailer = addCreatureReady(player1, new HellholeFlailer());
        flailer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        flailer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new HellholeFlailer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(flailer.isBlocking()).isTrue();
    }

    @Test
    void dealsPowerDamageToPlaneswalker() {
        addCreatureReady(player1, new HellholeFlailer());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, jace.getId());
        harness.assertNotOnBattlefield(player1, "Hellhole Flailer");
        harness.assertInGraveyard(player1, "Hellhole Flailer");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickToDamageItsController() {
        Permanent flailer = harness.addToBattlefieldAndReturn(player1, new HellholeFlailer());
        flailer.setSummoningSick(true);
        flailer.tap();
        harness.setLife(player1, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Hellhole Flailer");
    }

    @Test
    void temporaryPowerBoostIsIncludedInLastKnownPower() {
        Permanent flailer = addCreatureReady(player1, new HellholeFlailer());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, flailer.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Hellhole Flailer");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void castHellholeFlailer(boolean unleash) {
        harness.setHand(player1, List.of(new HellholeFlailer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
