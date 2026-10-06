package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DoubleVision;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeeTheTruth.class, Island.class, DoubleVision.class})
class SeeTheTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand lets you put one card into your hand and reorder the rest")
    void castFromHandChoosesOneCard() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.setHand(player1, List.of(new SeeTheTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first);
    }

    @Test
    @DisplayName("Cast from exile puts all three looked-at cards into your hand")
    void castFromExilePutsAllCardsIntoHand() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        SeeTheTruth spell = new SeeTheTruth();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void castFromHandWithAtMostOneCardNeedsNoChoice(int count) {
        Card card = new Island();
        harness.setLibrary(player1, count == 0 ? List.of() : List.of(card));
        harness.setHand(player1, List.of(new SeeTheTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyElementsOf(count == 0 ? List.of() : List.of(card));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void castFromExileTakesAllAvailableCards(int count) {
        List<Card> cards = List.<Card>of(new Island(), new Island()).subList(0, count);
        SeeTheTruth spell = new SeeTheTruth();
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of());
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void spellCopyStillPutsOneCardIntoHand(boolean originalFromExile) {
        Card card = new Island();
        SeeTheTruth spell = new SeeTheTruth();
        harness.addToBattlefield(player1, new DoubleVision());
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, originalFromExile ? List.of() : List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (originalFromExile) {
            harness.setExile(player1, List.of(spell));
            gd.exilePlayPermissions.put(spell.getId(), player1.getId());
            harness.castFromExile(player1, spell.getId());
        } else {
            harness.castSorcery(player1, 0, 0);
        }

        harness.passBothPriorities();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }
}
