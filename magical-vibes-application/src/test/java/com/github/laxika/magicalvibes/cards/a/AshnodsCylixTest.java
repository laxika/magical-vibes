package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AshnodsCylix.class)
class AshnodsCylixTest extends BaseCardTest {

    @Test
    void activationPaysThreeManaAndTapsCylix() {
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(cylix.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Target player keeps one of the top three on top and the rest are exiled")
    void keepsOneOnTopAndExilesRest() {
        harness.addToBattlefield(player1, new AshnodsCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card c0 = new AshnodsCylix();
        Card c1 = new AshnodsCylix();
        Card c2 = new AshnodsCylix();
        Card c3 = new AshnodsCylix();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(c0, c1, c2, c3));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // The target player chooses, not the ability's controller.
        harness.handleCardChosen(player2, 1);

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(2);
        assertThat(deckAfter.get(0).getId()).isEqualTo(c1.getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(c3.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(c0.getId(), c2.getId());
    }

    @Test
    @DisplayName("Looks at only as many cards as the library holds")
    void looksAtOnlyAvailableCards() {
        harness.addToBattlefield(player1, new AshnodsCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card top = new AshnodsCylix();
        Card bottom = new AshnodsCylix();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(top, bottom));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(top.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(bottom.getId());
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new AshnodsCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card c0 = new AshnodsCylix();
        Card c1 = new AshnodsCylix();
        Card c2 = new AshnodsCylix();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(c0, c1, c2));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(c2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(c0.getId(), c1.getId());
    }

    @Test
    @DisplayName("Resolving against an empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new AshnodsCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gd.playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
