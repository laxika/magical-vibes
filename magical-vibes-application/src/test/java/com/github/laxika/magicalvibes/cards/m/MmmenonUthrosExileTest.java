package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MmmenonUthrosExile.class, AccordersShield.class, GrizzlyBears.class})
class MmmenonUthrosExileTest extends BaseCardTest {

    @Test
    void artifactEntryPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
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

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, shield.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonartifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MmmenonUthrosExile());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
