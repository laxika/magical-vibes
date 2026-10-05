package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalastriaNightwatch.class, FountainOfYouth.class})
class KalastriaNightwatchTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying when its controller gains life")
    void gainsFlyingOnControllerLifeGain() {
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted only when the life-gain trigger resolves")
    void flyingWaitsForTriggerResolution() {
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({KalastriaNightwatch.class})
    @DisplayName("One life-gain event triggers each controlled Nightwatch once regardless of amount")
    void lifeGainTriggersEachControlledNightwatchOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KalastriaNightwatch());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.assertLife(player1, 25);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @CardUsed({KalastriaNightwatch.class})
    @DisplayName("Gaining zero life does not trigger Nightwatch")
    void zeroLifeGainDoesNotTrigger() {
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new KalastriaNightwatch());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(nightwatch.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
