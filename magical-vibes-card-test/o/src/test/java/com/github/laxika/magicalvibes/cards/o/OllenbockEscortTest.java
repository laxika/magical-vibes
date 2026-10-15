package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OllenbockEscort.class, TravelingMinister.class})
class OllenbockEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Ollenbock Escort grants both keywords to a countered creature you control")
    void grantsLifelinkAndIndestructible() {
        harness.addToBattlefield(player1, new OllenbockEscort());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ollenbock Escort");
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new OllenbockEscort());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature without a +1/+1 counter or an opponent's creature")
    void cannotTargetIneligibleCreatures() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new OllenbockEscort());
        Permanent uncountered = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, uncountered.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposing.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(escort);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, even by a tapped summoning-sick Escort")
    void sacrificesImmediatelyWithoutTapOrSummoningSicknessRestriction() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new OllenbockEscort());
        escort.tap();
        escort.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Ollenbock Escort");
        harness.assertInGraveyard(player1, "Ollenbock Escort");
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Losing the last +1/+1 counter before resolution makes the target illegal")
    void doesNotGrantKeywordsIfCounterIsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new OllenbockEscort());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ollenbock Escort");
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the counter after resolution does not remove the granted keywords")
    void retainsKeywordsIfCounterIsRemovedAfterResolution() {
        harness.addToBattlefield(player1, new OllenbockEscort());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A countered Escort may target itself but is sacrificed before resolution")
    void canTargetItselfAndThenFailsToResolve() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new OllenbockEscort());
        escort.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, escort.getId());

        harness.assertNotOnBattlefield(player1, "Ollenbock Escort");
        harness.assertInGraveyard(player1, "Ollenbock Escort");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A counter of another type does not satisfy the targeting restriction")
    void cannotTargetCreatureWithOnlyOtherCounters() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new OllenbockEscort());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        target.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(escort);
        assertThat(gd.stack).isEmpty();
    }
}
