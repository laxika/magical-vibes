package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreyalisesRadiance.class, SnowCoveredForest.class, RonomUnicorn.class})
class FreyalisesRadianceTest extends BaseCardTest {

    @Test
    @DisplayName("Snow permanents do not untap during their controllers' untap steps")
    void snowPermanentsDoNotUntap() {
        addCreatureReady(player1, new FreyalisesRadiance());
        Permanent snowLand = addCreatureReady(player2, new SnowCoveredForest());
        snowLand.tap();

        advanceToNextTurn(player1);

        assertThat(snowLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-snow permanents untap normally")
    void nonSnowPermanentsUntap() {
        addCreatureReady(player1, new FreyalisesRadiance());
        Permanent creature = addCreatureReady(player2, new RonomUnicorn());
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void snowPermanentsControlledByRadiancesControllerDoNotUntap() {
        addCreatureReady(player1, new FreyalisesRadiance());
        Permanent snowLand = addCreatureReady(player1, new SnowCoveredForest());
        snowLand.tap();

        harness.performUntapStep(player1);

        assertThat(snowLand.isTapped()).isTrue();
    }

    @Test
    void destroyingRadianceAllowsSnowPermanentsToUntap() {
        Permanent radiance = addCreatureReady(player1, new FreyalisesRadiance());
        Permanent snowLand = addCreatureReady(player2, new SnowCoveredForest());
        addCreatureReady(player2, new RonomUnicorn());
        snowLand.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 1, null, radiance.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Freyalise's Radiance");
        harness.performUntapStep(player2);

        assertThat(snowLand.isTapped()).isFalse();
    }

    @Test
    void payingFourManaWithTwoAgeCountersKeepsRadiance() {
        Permanent radiance = addCreatureReady(player1, new FreyalisesRadiance());
        radiance.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(radiance.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(radiance);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Freyalise's Radiance on the battlefield")
    void payingCumulativeUpkeepKeepsRadiance() {
        Permanent radiance = addCreatureReady(player1, new FreyalisesRadiance());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(radiance.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(radiance);
    }

    @Test
    @DisplayName("Cumulative upkeep requires two mana for each age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent radiance = addCreatureReady(player1, new FreyalisesRadiance());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(radiance);
        harness.assertInGraveyard(player1, "Freyalise's Radiance");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Freyalise's Radiance")
    void decliningCumulativeUpkeepSacrificesRadiance() {
        Permanent radiance = addCreatureReady(player1, new FreyalisesRadiance());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(radiance);
        harness.assertInGraveyard(player1, "Freyalise's Radiance");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.equals(player1) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
