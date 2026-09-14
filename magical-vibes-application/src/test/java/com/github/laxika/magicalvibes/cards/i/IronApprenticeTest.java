package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({IronApprentice.class, GrizzlyBears.class, Shock.class})
class IronApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent apprentice = castIronApprentice();

        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, moves its counters to a creature you control")
    void deathTriggerMovesCountersToControlledCreature() {
        Permanent apprentice = castIronApprentice();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, apprentice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger cannot target an opponent's creature")
    void deathTriggerTargetsOnlyControlledCreatures() {
        Permanent apprentice = castIronApprentice();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, apprentice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentBears.getId());
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent castIronApprentice() {
        harness.setHand(player1, List.of(new IronApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Iron Apprentice");
    }
}
