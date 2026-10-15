package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.FabledPassage;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necromentia.class, AlpineWatchdog.class, Plains.class, FabledPassage.class})
class NecromentiaTest extends BaseCardTest {

    private void addManaAndCast() {
        harness.setHand(player1, List.of(new Necromentia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Necromentia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Exiles selected copies and gives the opponent Zombies for hand copies")
    void exilesCopiesAndCreatesZombiesForHandCopies() {
        Card handCard1 = new AlpineWatchdog();
        Card handCard2 = new AlpineWatchdog();
        Card graveyardCard = new AlpineWatchdog();
        Card libraryCard = new AlpineWatchdog();

        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2, new Plains())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.setLibrary(player2, List.of(libraryCard));

        addManaAndCast();

        PendingInteraction.ColorChoice nameChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(nameChoice.options()).contains("Alpine Watchdog").doesNotContain("Plains");
        harness.handleListChoice(player1, "Alpine Watchdog");
        harness.handleMultipleCardsChosen(player1,
                List.of(handCard1.getId(), handCard2.getId(), graveyardCard.getId(), libraryCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Alpine Watchdog"))
                .hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .hasSize(2)
                .allMatch(p -> p.getEffectivePower() == 2 && p.getEffectiveToughness() == 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("Selecting no hand copies creates no Zombies")
    void noHandCopiesCreateNoZombies() {
        Card handCard = new AlpineWatchdog();
        Card graveyardCard = new AlpineWatchdog();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));

        addManaAndCast();
        harness.handleListChoice(player1, "Alpine Watchdog");
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"));
        harness.assertInHand(player2, "Alpine Watchdog");
        harness.assertNotInGraveyard(player2, "Alpine Watchdog");
    }

    @Test
    @DisplayName("May exile zero copies even when matching cards exist")
    void mayExileZeroCopies() {
        Card handCard = new AlpineWatchdog();
        Card graveyardCard = new AlpineWatchdog();
        Card libraryCard = new AlpineWatchdog();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(libraryCard));

        addManaAndCast();
        harness.handleListChoice(player1, "Alpine Watchdog");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can name a card absent from the opponent's zones")
    void canNameAbsentCard() {
        harness.setHand(player2, List.of(new Plains()));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains()));

        addManaAndCast();
        harness.handleListChoice(player1, "Alpine Watchdog");

        harness.assertInHand(player2, "Plains");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Rejects basic land names but allows nonbasic land names")
    void allowsNonbasicLandNames() {
        Card passage = new FabledPassage();
        harness.setHand(player2, List.of(passage, new Plains()));

        addManaAndCast();
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Plains"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Fabled Passage");
        harness.handleMultipleCardsChosen(player1, List.of(passage.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(passage);
        harness.assertInHand(player2, "Plains");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .hasSize(1);
    }
}
