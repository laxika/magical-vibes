package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroodguardElite.class, GrizzlyBears.class})
class BroodguardEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new BroodguardElite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent elite = findPermanent(player1, "Broodguard Elite");
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(elite.getEffectivePower()).isEqualTo(3);
        assertThat(elite.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When it leaves, puts its +1/+1 counters on a creature you control")
    void leavingTransfersCountersToControlledCreature() {
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent elite = addCreatureReady(player1, new BroodguardElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        removeElite(elite);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Warp casts it for {X}{G} and exiles it at the next end step")
    void warpCastsAndExilesAtNextEndStep() {
        BroodguardElite eliteCard = new BroodguardElite();
        harness.setHand(player1, List.of(eliteCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 2, null, null, List.of());
        harness.passBothPriorities();

        Permanent elite = findPermanent(player1, "Broodguard Elite");
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(eliteCard.getId())).isNotNull();
    }

    private void removeElite(Permanent elite) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, elite));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
