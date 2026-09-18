package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreachingLeviathan.class, BeaconOfUnrest.class, AirElemental.class, GrizzlyBears.class})
class BreachingLeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand taps nonblue creatures and skips their next untap")
    void castFromHandTapsNonblueCreaturesAndSkipsUntap() {
        Permanent nonblue = addCreatureReady(player1, new GrizzlyBears());
        Permanent blue = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new BreachingLeviathan()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(nonblue.isTapped()).isTrue();
        assertThat(blue.isTapped()).isFalse();

        advanceToUpkeep(player1);

        assertThat(nonblue.isTapped()).isTrue();
        assertThat(blue.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering from the graveyard does not trigger the hand-cast ability")
    void enteringFromGraveyardDoesNotTriggerAbility() {
        Permanent nonblue = addCreatureReady(player1, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new BreachingLeviathan()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(nonblue.isTapped()).isFalse();
    }
}
