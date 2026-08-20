package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpitefulSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoPlusOneCounters() {
        harness.setHand(player1, List.of(new SpitefulSquad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent squad = findPermanent(player1, "Spiteful Squad");
        assertThat(squad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(2);
    }

    @Test
    @DisplayName("On death, puts all its counters on a creature you control")
    void deathTriggerPutsCountersOnControlledCreature() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        squad.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        killSquad(squad);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The death trigger is not put on the stack without a controlled creature target")
    void deathTriggerNeedsControlledCreatureTarget() {
        Permanent squad = addCreatureReady(player1, new SpitefulSquad());
        squad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        squad.tap();

        killSquad(squad);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    private void killSquad(Permanent squad) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        UUID squadId = squad.getId();
        gs.playCard(gd, player2, 0, 0, squadId, null);
        harness.passBothPriorities();
    }
}
