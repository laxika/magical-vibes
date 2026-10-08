package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MineWorker;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YotianDissident.class, AccordersShield.class, GrizzlyBears.class, MineWorker.class, MycosynthLattice.class})
class YotianDissidentTest extends BaseCardTest {

    @Test
    void artifactEntryPutsCounterOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new YotianDissident());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new YotianDissident());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonartifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new YotianDissident());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutCounterOnItself() {
        Permanent dissident = harness.addToBattlefieldAndReturn(player1, new YotianDissident());
        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, dissident.getId());
        assertThat(dissident.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(dissident.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTargetTheEnteringArtifactCreature() {
        harness.addToBattlefield(player1, new YotianDissident());
        harness.castFromHand(player1, new MineWorker(), "{2}");
        harness.passBothPriorities();

        Permanent worker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof MineWorker)
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, worker.getId());
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsArtifactEntryDoesNotTrigger() {
        Permanent dissident = harness.addToBattlefieldAndReturn(player1, new YotianDissident());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MineWorker(), "{2}");
        harness.passBothPriorities();

        assertThat(dissident.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownEntryTriggersWhenLatticeMakesItAnArtifact() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.castFromHand(player1, new YotianDissident(), "{G}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Yotian Dissident"));
        harness.passBothPriorities();

        Permanent dissident = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof YotianDissident)
                .findFirst().orElseThrow();
        assertThat(dissident.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
