package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GloriousAnthem.class, NessianWanderer.class, Shock.class})
class NessianWandererTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control offers a land from the top three")
    void ownEnchantmentEntryOffersLand() {
        Forest forest = new Forest();
        Shock shock = new Shock();
        Shock otherShock = new Shock();
        List<Card> topThree = List.of(forest, shock, otherShock);
        harness.setLibrary(player1, topThree);
        harness.addToBattlefield(player1, new NessianWanderer());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(shock, otherShock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no land among the top three, the cards go to the bottom")
    void noLandAmongTopThree() {
        List<Card> topThree = List.of(new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, topThree);
        harness.addToBattlefield(player1, new NessianWanderer());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topThree);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        List<Card> library = List.of(new Forest(), new Shock(), new Shock());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new NessianWanderer());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Declining a land puts all three looked-at cards below the untouched library")
    void mayDeclineLand() {
        Forest forest = new Forest();
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(forest, firstShock, secondShock, untouched));
        triggerConstellation();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(forest, firstShock, secondShock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only lands in the top three are eligible and only one is taken")
    void choosesOneOfMultipleLandsInTopThree() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        Shock shock = new Shock();
        Forest fourthCard = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand, shock, fourthCard));
        triggerConstellation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstLand.getId(), secondLand.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(secondLand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fourthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(firstLand, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than three cards still offers its land")
    void shortLibrary() {
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, forest));
        triggerConstellation();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a failed draw")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        triggerConstellation();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void triggerConstellation() {
        harness.addToBattlefield(player1, new NessianWanderer());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
