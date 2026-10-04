package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorgerWurm.class, GrizzlyBears.class})
class GorgerWurmTest extends BaseCardTest {

    private void castWurm() {
        harness.castFromHand(player1, new GorgerWurm(), "{3}{R}{G}");
    }

    private Permanent wurm() {
        return findPermanent(player1, "Gorger Wurm");
    }

    @Test
    @DisplayName("Devouring two creatures gives two +1/+1 counters (Devour 1)")
    void devourTwoAddsTwoCounters() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWurm();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(wurm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWurm();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(wurm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no other creatures, enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castWurm();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt)

        assertThat(wurm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Devour sacrifices only the selected creature and excludes opposing creatures")
    void devourOnlySelectedControlledCreature() {
        Permanent selected = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWurm();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(selected, retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(selected.getId(), retained.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId()));

        assertThat(wurm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(retained).doesNotContain(selected);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(selected.getCard());
    }
}
