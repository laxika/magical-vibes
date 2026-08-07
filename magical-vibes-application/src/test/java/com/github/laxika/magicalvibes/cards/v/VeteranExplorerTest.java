package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VeteranExplorerTest extends BaseCardTest {

    private void setupLibrary(Player player, Card... cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private List<Permanent> lands(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
    }

    /** Kills the Veteran Explorer player1 controls with a Flame Javelin cast by the active player2. */
    private void killExplorer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID explorerId = harness.getPermanentId(player1, "Veteran Explorer");
        harness.castInstant(player2, 0, explorerId);
        harness.passBothPriorities(); // Flame Javelin resolves -> Veteran Explorer dies
        harness.passBothPriorities(); // death trigger resolves
    }

    @Test
    @DisplayName("When Veteran Explorer dies each player may fetch two basic lands, in APNAP order")
    void bothPlayersFetchTwoBasicLands() {
        harness.addToBattlefield(player1, new VeteranExplorer());
        setupLibrary(player1, new Forest(), new Forest(), new GrizzlyBears());
        setupLibrary(player2, new Plains(), new Plains(), new GrizzlyBears());

        killExplorer();

        // Active player (player2) is prompted first, and only the basic lands are offered.
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().cards()).extracting(Card::getName).containsExactly("Plains", "Plains");

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The fetched lands enter untapped")
    void fetchedLandsEnterUntapped() {
        harness.addToBattlefield(player1, new VeteranExplorer());
        setupLibrary(player1, new Forest());
        gd.playerDecks.get(player2.getId()).clear();

        killExplorer();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(lands(player1)).singleElement().matches(land -> !land.isTapped());
    }

    @Test
    @DisplayName("The search is a may — a player can decline, or take fewer than two lands")
    void searchIsOptional() {
        harness.addToBattlefield(player1, new VeteranExplorer());
        setupLibrary(player1, new Forest(), new Forest());
        setupLibrary(player2, new Plains(), new Plains());

        killExplorer();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(landCount(player2)).isZero();
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("A player whose library holds no basic land is skipped")
    void playerWithoutBasicLandsIsSkipped() {
        harness.addToBattlefield(player1, new VeteranExplorer());
        setupLibrary(player1, new Forest());
        setupLibrary(player2, new GrizzlyBears());

        killExplorer();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("Nothing happens while Veteran Explorer stays on the battlefield")
    void noSearchWhileAlive() {
        harness.addToBattlefield(player1, new VeteranExplorer());
        setupLibrary(player1, new Forest(), new Forest());
        setupLibrary(player2, new Plains(), new Plains());

        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isZero();
    }
}
