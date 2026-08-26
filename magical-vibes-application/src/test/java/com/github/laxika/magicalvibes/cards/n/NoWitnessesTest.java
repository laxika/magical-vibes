package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoWitnesses.class, GrizzlyBears.class})
class NoWitnessesTest extends BaseCardTest {

    @Test
    @DisplayName("The player with the most creatures investigates before all creatures are destroyed")
    void playerWithMostCreaturesInvestigatesThenAllCreaturesAreDestroyed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Every player tied for the most creatures investigates")
    void everyPlayerTiedForMostCreaturesInvestigates() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("All players investigate when no creatures are on the battlefield")
    void allPlayersInvestigateWhenCreatureCountsAreTiedAtZero() {
        cast();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new NoWitnesses()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
