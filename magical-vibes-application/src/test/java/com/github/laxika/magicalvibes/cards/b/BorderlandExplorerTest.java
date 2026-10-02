package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorderlandExplorer.class, Forest.class, GrizzlyBears.class, Plains.class})
class BorderlandExplorerTest extends BaseCardTest {

    @Test
    void playersWhoDiscardMaySearchForBasicLandsToHand() {
        BorderlandExplorer explorer = new BorderlandExplorer();
        GrizzlyBears controllerDiscard = new GrizzlyBears();
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(explorer, controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(plains));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentDiscard);
    }

    @Test
    void onlyPlayersWhoDiscardedReceiveTheSearchAndTheyMayFailToFind() {
        BorderlandExplorer explorer = new BorderlandExplorer();
        GrizzlyBears controllerDiscard = new GrizzlyBears();
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(explorer, controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(plains));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentDiscard);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
