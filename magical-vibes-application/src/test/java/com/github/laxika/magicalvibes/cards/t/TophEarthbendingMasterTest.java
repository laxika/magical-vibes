package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TophEarthbendingMaster.class, Forest.class, GrizzlyBears.class})
class TophEarthbendingMasterTest extends BaseCardTest {

    @Test
    void landfallGivesAnExperienceCounter() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void attackingEarthbendsAChosenLandByTheNumberOfExperienceCounters() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackTriggerCannotTargetAnOpponentsLand() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
