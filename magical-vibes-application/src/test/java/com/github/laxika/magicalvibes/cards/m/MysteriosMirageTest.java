package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.p.PulsatingIllusion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriosMirage.class, PulsatingIllusion.class, AvenFlock.class})
class MysteriosMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 Illusion Villain at your end step after you discard")
    void createsTokenAfterControllerDiscards() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a token when you did not discard this turn")
    void doesNotCreateTokenWithoutDiscard() {
        harness.addToBattlefield(player1, new MysteriosMirage());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
