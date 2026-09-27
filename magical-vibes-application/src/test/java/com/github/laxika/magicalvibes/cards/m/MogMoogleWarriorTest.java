package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MogMoogleWarrior.class, GrizzlyBears.class, Shock.class})
class MogMoogleWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may discard, draws for discarding, and Mog applies both discard riders")
    void discardRidersApplyToAllDiscardedTypes() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Shock");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        List<Permanent> moogles = findPermanents(player1, "Moogle");
        assertThat(moogles).hasSize(1);
        assertThat(moogles.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining leaves hands and Mog unchanged")
    void decliningDoesNothing() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Shock");
        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Moogle")).isEmpty();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
