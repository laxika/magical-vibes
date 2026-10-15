package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SylvanScrying;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OppositionAgent.class, SylvanScrying.class, Plains.class, Forest.class})
class OppositionAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Opposition Agent controls an opponent's search and exiles the found card")
    void controlsOpponentSearchAndExilesFoundCard() {
        harness.addToBattlefield(player1, new OppositionAgent());
        harness.setHand(player2, List.of(new SylvanScrying()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLibrary(player2, List.of(new Plains(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosen);
        assertThat(gd.exilePlayPermissions.get(chosen.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void controllerCanFailToFindInARestrictedSearch() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Plains land = new Plains();
        harness.setLibrary(player2, List.of(land));
        harness.setHand(player2, List.of(new SylvanScrying()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void controllerCanPlayAnExiledLandOnTheirOwnTurn() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Plains land = new Plains();
        harness.setLibrary(player2, List.of(land));
        harness.setHand(player2, List.of(new SylvanScrying()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
