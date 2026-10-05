package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.p.ProteanHulk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThoughtKnotSeer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OppositionAgent.class, DiabolicTutor.class, GrizzlyBears.class, Shock.class,
        Humility.class, ProteanHulk.class, ThoughtKnotSeer.class, AvenMindcensor.class})
class OppositionAgentTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Controls an opponent's search and exiles the card they find for its controller to play")
    void controlsOpponentSearch() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Shock chosenCard = new Shock();
        harness.setLibrary(player2, List.of(chosenCard, new GrizzlyBears()));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(search.params().playerId()).isEqualTo(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosenCard);
        assertThat(gd.findExiledCard(chosenCard.getId())).satisfies(entry -> {
            assertThat(entry).isNotNull();
            assertThat(entry.faceDown()).isFalse();
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });
        assertThat(gd.exilePlayPermissions).containsEntry(chosenCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosenCard.getId());
    }

    @Test
    void doesNotControlItsControllersSearch() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Shock found = new Shock();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.findExiledCard(found.getId())).isNull();
    }

    @Test
    void losesSearchControlWhenItsAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new OppositionAgent());
        harness.addToBattlefield(player1, new Humility());
        Shock found = new Shock();
        harness.setLibrary(player2, List.of(found));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(found);
        assertThat(gd.findExiledCard(found.getId())).isNull();
    }

    @Test
    void canCastFoundCardWithOtherColoredManaAfterAgentDies() {
        harness.addToBattlefield(player1, new OppositionAgent());
        Shock found = new Shock();
        harness.setLibrary(player2, List.of(found));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Opposition Agent"));
        harness.assertNotOnBattlefield(player1, "Opposition Agent");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, found.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(found.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(found);
    }

    @Test
    void foundCardsStillCountTowardTheSearchTotalManaValueLimit() {
        harness.addToBattlefield(player1, new OppositionAgent());
        harness.addToBattlefield(player2, new ProteanHulk());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        var hulkId = harness.getPermanentId(player2, "Protean Hulk");

        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void anyColorPermissionDoesNotPayAnExplicitColorlessCost() {
        harness.addToBattlefield(player1, new OppositionAgent());
        ThoughtKnotSeer found = new ThoughtKnotSeer();
        harness.setLibrary(player2, List.of(found));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, found.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(found.getId())).isNotNull();
    }

    @Test
    void laterPicksCannotEscapeTheTopFourCardsSearchRestriction() {
        harness.addToBattlefield(player1, new OppositionAgent());
        harness.addToBattlefield(player1, new AvenMindcensor());
        harness.addToBattlefield(player2, new ProteanHulk());
        ThoughtKnotSeer outsideSearch = new ThoughtKnotSeer();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new Shock(), new Shock(), outsideSearch));
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        var hulkId = harness.getPermanentId(player2, "Protean Hulk");
        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.castAndResolveInstant(player1, 0, hulkId);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch initial = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(initial.params().cards()).doesNotContain(outsideSearch);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch next = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(next).isNotNull();
        assertThat(next.params().cards()).doesNotContain(outsideSearch);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }
}
