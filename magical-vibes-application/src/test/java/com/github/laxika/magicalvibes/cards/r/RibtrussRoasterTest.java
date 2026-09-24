package com.github.laxika.magicalvibes.cards.r;

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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RibtrussRoaster.class, GrizzlyBears.class, Shock.class})
class RibtrussRoasterTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring two creatures adds two counters and creates two Pest tokens at your end step")
    void devourScalesEndStepPestTokens() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRoaster();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent roaster = findPermanent(player1, "Ribtruss Roaster");
        assertThat(roaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Pest")).isEqualTo(2);
    }

    @Test
    @DisplayName("Pest tokens created by Ribtruss Roaster gain 1 life when they die")
    void pestDeathGainsLife() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoaster();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));
        resolveControllerEndStep();

        Permanent pest = findPermanent(player1, "Pest");
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Declining to devour creates no Pest tokens")
    void decliningDevourCreatesNoPests() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castRoaster();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Ribtruss Roaster")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveControllerEndStep();

        assertThat(countPermanents(player1, "Pest")).isZero();
    }

    private void castRoaster() {
        harness.setHand(player1, new ArrayList<>(List.of(new RibtrussRoaster())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
