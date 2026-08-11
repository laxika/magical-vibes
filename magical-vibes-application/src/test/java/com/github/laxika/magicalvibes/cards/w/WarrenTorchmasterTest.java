package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarrenTorchmasterTest extends BaseCardTest {

    @Test
    void blightsOneCreatureThenTargetsADifferentCreatureForHaste() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        torchmaster.setSummoningSick(false);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, torchmaster.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void decliningBlightDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        torchmaster.setSummoningSick(false);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
