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

    private Permanent addPoet() {
        return harness.addToBattlefieldAndReturn(player1, new BloodPoet());
    }

    private void prepareSparkAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
