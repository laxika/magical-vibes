package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SultaiEmissary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WriteIntoBeing.class, Forest.class, SultaiEmissary.class})
class WriteIntoBeingTest extends BaseCardTest {

    @Test
    void manifestsChosenCardAndPutsTheOtherOnTop() {
        Card manifestedCard = new SultaiEmissary();
        Card topCard = new Forest();
        prepareSpell(List.of(manifestedCard, topCard));

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void manifestsChosenCardAndPutsTheOtherOnBottom() {
        Card bottomCard = new Forest();
        Card manifestedCard = new SultaiEmissary();
        Card nextCard = new Forest();
        prepareSpell(List.of(bottomCard, manifestedCard, nextCard));

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(nextCard);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(bottomCard);
    }

    private void prepareSpell(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new WriteIntoBeing(), "{2}{U}");
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        prepareSpell(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Write into Being");
    }

    @Test
    void singleCardLibraryManifestsWithoutADestinationChoice() {
        Card card = new SultaiEmissary();
        prepareSpell(List.of(card));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(card);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canManifestALandButCannotTurnItFaceUp() {
        Card land = new Forest();
        Card remaining = new SultaiEmissary();
        prepareSpell(List.of(land, remaining));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.handleListChoice(player1, "Top");

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(land);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Top", "Bottom"})
    void returningLookedAtCardToLibraryDoesNotRevealItsName(String destination) {
        Card manifested = new SultaiEmissary();
        Card remaining = new Forest();
        prepareSpell(List.of(manifested, remaining));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifested.getId()));
        int logStart = gd.gameLog.size();
        harness.handleListChoice(player1, destination);

        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()))
                .extracting(GameLogEntry::plainText)
                .noneMatch(message -> message.contains("Forest"));
    }

    @Test
    void mustChooseExactlyOneOfTheTopTwoCards() {
        Card first = new Forest();
        Card second = new SultaiEmissary();
        Card third = new Forest();
        prepareSpell(List.of(first, second, third));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(third.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.handleListChoice(player1, "Top");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(second);
                    assertThat(permanent.isManifested()).isTrue();
                });
    }
}
