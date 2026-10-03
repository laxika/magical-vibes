package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodPoet.class, GrizzlyBears.class})
class BloodPoetTest extends BaseCardTest {

    @Test
    void plusOneGivesSparkCounterAndLifelinkUntilEndOfTurn() {
        Permanent poet = addPoet();
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void onlyOneSparkAbilityCanBeActivatedAcrossAllControlledPermanentsEachTurn() {
        addPoet();
        addPoet();
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .hasMessageContaining("Only one spark ability");
    }

    @Test
    void minusThreeMakesOpponentDiscardAndGainsItsManaValue() {
        addPoet();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        gd.playerSparkCounters.put(player1.getId(), 3);
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerSparkCounters.get(player1.getId())).isZero();
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void minusThreeCannotTargetItsController() {
        addPoet();
        gd.playerSparkCounters.put(player1.getId(), 3);
        prepareSparkAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .hasMessageContaining("opponent");
        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void lifelinkExpiresAfterTheTurn() {
        Permanent poet = addPoet();
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, poet, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, poet, Keyword.LIFELINK)).isFalse();
        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void minusThreeRequiresEnoughSparkCounters() {
        addPoet();
        gd.playerSparkCounters.put(player1.getId(), 2);
        prepareSparkAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .hasMessageContaining("Not enough spark counters");

        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sparkCannotBeActivatedOutsideAMainPhase() {
        addPoet();
        prepareSparkAbilityActivation();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .hasMessageContaining("as a sorcery");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSparkCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void sparkCannotBeActivatedWithAnotherAbilityOnTheStack() {
        addPoet();
        harness.addToBattlefield(player2, new BloodPoet());
        prepareSparkAbilityActivation();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .hasMessageContaining("as a sorcery");

        harness.forceActivePlayer(player1);
        harness.passBothPriorities();
    }

    @Test
    void emptyOpposingHandGainsNoLifeFromAnEarlierDiscard() {
        addPoet();
        harness.setHand(player2, List.of());
        gd.lastDiscardedCardManaValue = 3;
        gd.playerSparkCounters.put(player1.getId(), 3);
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSparkCounters.get(player1.getId())).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentChoosesTheDiscardAndLifeIsGainedOnlyAfterThatChoice() {
        addPoet();
        BloodPoet discarded = new BloodPoet();
        BloodPoet retained = new BloodPoet();
        harness.setHand(player2, List.of(retained, discarded));
        gd.playerSparkCounters.put(player1.getId(), 3);
        prepareSparkAbilityActivation();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player2, 1);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    private Permanent addPoet() {
        return harness.addToBattlefieldAndReturn(player1, new BloodPoet());
    }

    private void prepareSparkAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
