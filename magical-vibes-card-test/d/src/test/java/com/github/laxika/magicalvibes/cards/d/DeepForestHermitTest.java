package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepForestHermit.class, VampireHexmage.class})
class DeepForestHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates four Squirrels that get +1/+1")
    void enteringCreatesFourBuffedSquirrels() {
        castAndResolveHermit();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(4);
        for (Permanent squirrel : squirrels) {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The static ability buffs only Squirrels you control")
    void buffsOnlyControlledSquirrels() {
        castAndResolveHermit();
        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        Permanent ownSquirrel = squirrels.get(0);
        Permanent opponentSquirrel = squirrels.get(1);
        gd.playerBattlefields.get(player1.getId()).remove(opponentSquirrel);
        gd.playerBattlefields.get(player2.getId()).add(opponentSquirrel);
        Permanent nonSquirrel = findPermanent(player1, "Deep Forest Hermit");

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSquirrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vanishing removes one time counter at each upkeep and sacrifices on the last")
    void vanishingRemovesCountersAndThenSacrifices() {
        Permanent hermit = addCreatureReady(player1, new DeepForestHermit());
        hermit.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Deep Forest Hermit");
        harness.assertInGraveyard(player1, "Deep Forest Hermit");
    }

    private void castAndResolveHermit() {
        harness.castFromHand(player1, new DeepForestHermit(), "{3}{G}{G}");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Deep Forest Hermit");
    }

    @Test
    void entersWithThreeTimeCountersBeforeTokenTriggerResolves() {
        harness.castFromHand(player1, new DeepForestHermit(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Deep Forest Hermit").getCounterCount(CounterType.TIME))
                .isEqualTo(3);
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Squirrel")).hasSize(4);
    }

    @Test
    void opponentsUpkeepDoesNotRemoveTimeCounters() {
        castAndResolveHermit();
        Permanent hermit = findPermanent(player1, "Deep Forest Hermit");
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(hermit.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void lastCounterCreatesSeparateSacrificeTriggerAndBonusEndsOnSacrifice() {
        castAndResolveHermit();
        Permanent hermit = findPermanent(player1, "Deep Forest Hermit");
        hermit.setCounterCount(CounterType.TIME, 1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hermit.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Deep Forest Hermit");
        assertThat(findPermanents(player1, "Squirrel")).hasSize(4).allSatisfy(squirrel -> {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
        });
    }

    @Test
    void removingLastTimeCounterWithAnotherAbilityTriggersSacrifice() {
        castAndResolveHermit();
        Permanent hermit = findPermanent(player1, "Deep Forest Hermit");
        Permanent hexmage = harness.addToBattlefieldAndReturn(player1, new VampireHexmage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hexmage), null, hermit.getId());
        harness.passBothPriorities();

        assertThat(hermit.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Deep Forest Hermit");
        harness.assertInGraveyard(player1, "Deep Forest Hermit");
    }

    @Test
    void vanishingDoesNotTriggerWithoutTimeCounters() {
        Permanent hermit = addCreatureReady(player1, new DeepForestHermit());
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);
    }
}
