package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootwiseSurvivor.class, Forest.class})
class RootwiseSurvivorTest extends BaseCardTest {

    @Test
    void tappedSurvivorEarthbendsLandAtPostcombatMain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earthbendTargetMustBeLandControlledBySurvivorController() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId(), player1.getId())
                .doesNotContain(opponentForest.getId());
        harness.handlePermanentChosen(player1, ownForest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, opponentForest)).isTrue();
        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
        assertThat(opponentForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayDeclineTargetingLand() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
