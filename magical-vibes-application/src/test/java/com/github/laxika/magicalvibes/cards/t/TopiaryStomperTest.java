package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExpendableLackey;
import com.github.laxika.magicalvibes.cards.b.BotanicalPlaza;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopiaryStomper.class, Forest.class, ExpendableLackey.class, BotanicalPlaza.class})
class TopiaryStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering searches for a basic land and puts it onto the battlefield tapped")
    void enteringSearchesForTappedBasicLand() {
        Forest forest = new Forest();
        Card lackey = new ExpendableLackey();
        harness.setLibrary(player1, List.of(forest, lackey));
        harness.setHand(player1, List.of(new TopiaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(forest);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        Permanent forestPermanent = findPermanent(player1, "Forest");
        assertThat(forestPermanent.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lackey);
    }

    @Test
    @DisplayName("Cannot attack with fewer than seven lands")
    void cannotAttackWithFewerThanSevenLands() {
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 6);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with seven lands")
    void canAttackWithSevenLands() {
        addCreatureReady(player1, new TopiaryStomper());
        addCreatureReady(player2, new ExpendableLackey());
        addForests(player1, 7);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block with fewer than seven lands")
    void cannotBlockWithFewerThanSevenLands() {
        addCreatureReady(player2, new ExpendableLackey());
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 6);

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with seven lands")
    void canBlockWithSevenLands() {
        addCreatureReady(player2, new ExpendableLackey());
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 7);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A nonbasic land is excluded from the basic land search")
    void searchExcludesNonbasicLands() {
        Forest forest = new Forest();
        BotanicalPlaza plaza = new BotanicalPlaza();
        harness.setLibrary(player1, List.of(plaza, forest));
        castStomperAndResolveTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plaza);
        harness.assertNotOnBattlefield(player1, "Botanical Plaza");
    }

    @Test
    @DisplayName("Can fail to find even when a basic land is available")
    void canDeclineBasicLandSearch() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castStomperAndResolveTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("An empty library still completes the search and shuffle")
    void searchCompletesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castStomperAndResolveTriggers();

        harness.assertOnBattlefield(player1, "Topiary Stomper");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("A library without basic lands completes without moving any cards")
    void searchCompletesWithoutBasicLands() {
        BotanicalPlaza plaza = new BotanicalPlaza();
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setLibrary(player1, List.of(plaza, lackey));
        castStomperAndResolveTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plaza, lackey);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Opposing lands and friendly creatures do not satisfy the land requirement")
    void onlyControllersLandsSatisfyRestriction() {
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 6);
        addForests(player2, 7);
        addCreatureReady(player1, new ExpendableLackey());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        addCreatureReady(player2, new ExpendableLackey());
        declareAttackersAndPrepareBlockers(player2, List.of(7));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 7))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped nonbasic seventh land allows attacking")
    void tappedNonbasicLandCountsTowardRequirement() {
        Permanent stomper = addCreatureReady(player1, new TopiaryStomper());
        addCreatureReady(player2, new ExpendableLackey());
        addForests(player1, 6);
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new BotanicalPlaza());
        plaza.tap();

        declareAttackers(player1, List.of(0));

        assertThat(stomper.isAttacking()).isTrue();
        assertThat(stomper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing the seventh land before blocking prevents blocking")
    void blockingRestrictionUsesCurrentLandCount() {
        addCreatureReady(player1, new TopiaryStomper());
        addForests(player1, 7);
        addCreatureReady(player2, new ExpendableLackey());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStomperAndResolveTriggers() {
        harness.setHand(player1, List.of(new TopiaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
