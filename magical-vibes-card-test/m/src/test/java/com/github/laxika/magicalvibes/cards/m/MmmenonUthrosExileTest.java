package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MmmenonUthrosExile.class, AccordersShield.class, GrizzlyBears.class, MycosynthLattice.class})
class MmmenonUthrosExileTest extends BaseCardTest {

    @Test
    void artifactEntryPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
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
    void canTargetCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, shield.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonartifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutCounterOnItself() {
        Permanent mmmenon = harness.addToBattlefieldAndReturn(player1, new MmmenonUthrosExile());

        harness.castFromHand(player1, new AccordersShield(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mmmenon.getId());
        harness.passBothPriorities();

        assertThat(mmmenon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsArtifactEntryDoesNotTrigger() {
        Permanent mmmenon = harness.addToBattlefieldAndReturn(player1, new MmmenonUthrosExile());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AccordersShield(), "{0}");
        harness.passBothPriorities();

        assertThat(mmmenon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachArtifactEntryInTheSameTurn() {
        Permanent mmmenon = harness.addToBattlefieldAndReturn(player1, new MmmenonUthrosExile());

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new AccordersShield(), "{0}");
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, mmmenon.getId());
            harness.passBothPriorities();
        }

        assertThat(mmmenon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({MmmenonUthrosExile.class, MycosynthLattice.class})
    void triggersForItsOwnEntryWhenItEntersAsAnArtifact() {
        harness.addToBattlefield(player1, new MycosynthLattice());

        harness.castFromHand(player1, new MmmenonUthrosExile(), "{1}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var mmmenonId = harness.getPermanentId(player1, "Mm'menon, Uthros Exile");
        harness.handlePermanentChosen(player1, mmmenonId);
        harness.passBothPriorities();

        Permanent mmmenon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(mmmenonId))
                .findFirst().orElseThrow();
        assertThat(mmmenon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
