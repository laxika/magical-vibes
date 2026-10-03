package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FavorOfJukai;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GenerousVisitor;
import com.github.laxika.magicalvibes.cards.t.TamiyosSafekeeping;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommuneWithSpirits.class, Forest.class, GenerousVisitor.class, FavorOfJukai.class, TamiyosSafekeeping.class})
class CommuneWithSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("offers enchantment and land cards from the top four")
    void offersEnchantmentAndLandCards() {
        FavorOfJukai enchantment = new FavorOfJukai();
        Forest forest = new Forest();
        GenerousVisitor creature = new GenerousVisitor();
        TamiyosSafekeeping instant = new TamiyosSafekeeping();
        setUpAndCast(List.of(enchantment, forest, creature, instant));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId(), forest.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, creature, instant);
    }

    @Test
    @DisplayName("may decline and puts all four cards on the bottom")
    void mayDecline() {
        Card first = new Forest();
        Card second = new GenerousVisitor();
        Card third = new TamiyosSafekeeping();
        Card fourth = new Forest();
        setUpAndCast(List.of(first, second, third, fourth));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    @Test
    void choosesLandAndLeavesUnlookedCardsAboveTheRest() {
        Card enchantment = new FavorOfJukai();
        Card land = new Forest();
        Card creature = new GenerousVisitor();
        Card instant = new TamiyosSafekeeping();
        Card untouchedFirst = new Forest();
        Card untouchedSecond = new FavorOfJukai();
        setUpAndCast(List.of(enchantment, land, creature, instant, untouchedFirst, untouchedSecond));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(enchantment.getId(), land.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(untouchedFirst, untouchedSecond);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(enchantment, creature, instant);
        harness.assertInGraveyard(player1, "Commune with Spirits");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCardsGoToBottomWithoutOfferingAChoice() {
        Card first = new GenerousVisitor();
        Card second = new TamiyosSafekeeping();
        Card third = new GenerousVisitor();
        Card fourth = new TamiyosSafekeeping();
        Card untouched = new Forest();
        setUpAndCast(List.of(first, second, third, fourth, untouched));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Commune with Spirits");
    }

    @Test
    void mayDeclineTheOnlyEligibleCardInAShortLibrary() {
        Card land = new Forest();
        Card creature = new GenerousVisitor();
        setUpAndCast(List.of(land, creature));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayTakeTheOnlyCardInTheLibrary() {
        Card enchantment = new FavorOfJukai();
        setUpAndCast(List.of(enchantment));

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawing() {
        setUpAndCast(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Commune with Spirits");
    }

    private void setUpAndCast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CommuneWithSpirits()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
