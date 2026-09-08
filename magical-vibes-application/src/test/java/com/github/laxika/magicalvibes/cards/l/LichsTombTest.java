package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LichsTombTest extends BaseCardTest {

    @Test
    @DisplayName("Controller doesn't lose the game at 0 or less life")
    void controllerDoesNotLoseAtZeroLife() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tomb);
    }

    @Test
    @DisplayName("Losing life triggers a sacrifice for each life lost")
    void losingLifeTriggersSacrificeForEachLifeLost() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        loseLife(2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(tomb.getId(), forest.getId(), bears.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), bears.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Lich's Tomb");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Lich's Tomb itself is an eligible sacrifice")
    void itselfIsEligibleSacrifice() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 20);

        loseLife(1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(tomb.getId(), forest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(tomb.getId()));

        harness.assertNotOnBattlefield(player1, "Lich's Tomb");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void loseLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), amount, "test"));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(amount);
        harness.passBothPriorities();
    }
}
