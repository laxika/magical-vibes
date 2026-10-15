package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RulikMonsWarrenChief.class, Forest.class, NishobaBrawler.class})
class RulikMonsWarrenChiefTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a land on top offers it onto the battlefield tapped")
    void attackingWithLandOnTopOffersTappedLand() {
        addReadyRulik();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.getCard()).isSameAs(forest);
        assertThat(land.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Declining the land creates a Goblin and leaves the land on top")
    void decliningLandCreatesGoblin() {
        addReadyRulik();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("A nonland top card creates a Goblin without moving the card")
    void nonlandTopCardCreatesGoblin() {
        addReadyRulik();
        NishobaBrawler topCard = new NishobaBrawler();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("The controller privately sees a nonland top card before leaving it on top")
    void nonlandTopCardIsShownOnlyToController() {
        addReadyRulik();
        NishobaBrawler topCard = new NishobaBrawler();
        harness.setLibrary(player1, List.of(topCard));
        harness.clearMessages();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anySatisfy(message -> assertThat(message).contains("Nishoba Brawler"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gameLogContains("Nishoba Brawler")).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library still creates an untapped nonattacking red Goblin")
    void emptyLibraryCreatesGoblin() {
        addReadyRulik();
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().isToken()).isTrue();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(goblin.isTapped()).isFalse();
        assertThat(goblin.isAttacking()).isFalse();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller uses their own library and receives the land")
    void opposingControllerGetsTheirOwnTopLand() {
        addCreatureReady(player2, new RulikMonsWarrenChief());
        Forest forest = new Forest();
        NishobaBrawler otherTopCard = new NishobaBrawler();
        harness.setLibrary(player2, List.of(forest));
        harness.setLibrary(player1, List.of(otherTopCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        Permanent land = findPermanent(player2, "Forest");
        assertThat(land.getCard()).isSameAs(forest);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherTopCard);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    private void addReadyRulik() {
        addCreatureReady(player1, new RulikMonsWarrenChief());
    }
}
