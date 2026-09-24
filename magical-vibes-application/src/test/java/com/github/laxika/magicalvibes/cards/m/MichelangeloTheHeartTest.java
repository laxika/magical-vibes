package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloTheHeart.class, GrizzlyBears.class, Forest.class})
class MichelangeloTheHeartTest extends BaseCardTest {

    @Test
    void raidPutsCounterOnTargetCreatureAndCreatesFoodAtPostcombatMain() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void doesNotTriggerWithoutRaid() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void raidCanTargetAnyCreatureButNotNoncreatures() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToPostcombatMain(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownTarget.getId()).doesNotContain(ownForest.getId());
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
