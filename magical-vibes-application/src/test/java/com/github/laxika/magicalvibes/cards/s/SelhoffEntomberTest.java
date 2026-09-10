package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelhoffEntomber.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class SelhoffEntomberTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and discarding a creature draws a card")
    void discardsCreatureAndDraws() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        HillGiant drawn = new HillGiant();
        harness.setHand(player1, List.of(land, discarded));
        harness.setLibrary(player1, List.of(drawn));
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(entomber.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void requiresCreatureCardToDiscard() {
        Permanent entomber = addCreatureReady(player1, new SelhoffEntomber());
        harness.setHand(player1, List.of(new Forest()));
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(entomber.isTapped()).isFalse();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
