package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Quagnoth.class, AugurOfSkulls.class})
class QuagnothTest extends BaseCardTest {

    @Test
    void returnsToHandWhenDiscardedByOpponent() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Quagnoth");
        harness.assertNotInGraveyard(player2, "Quagnoth");
    }

    @Test
    void doesNotTriggerWhenControllerDiscardsIt() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player1, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Quagnoth");
        harness.assertNotInHand(player1, "Quagnoth");
    }
}
