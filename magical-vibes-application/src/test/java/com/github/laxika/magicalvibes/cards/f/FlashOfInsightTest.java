package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncestorsChosen;
import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer.CardOrder;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashOfInsight.class, AncestorsChosen.class, AvenFogbringer.class, GiantWarthog.class})
class FlashOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Flashback exiles X blue cards and looks at X cards")
    void flashbackExilesBlueCardsAndResolvesXEffect() {
        Card top1 = new GiantWarthog();
        Card top2 = new AncestorsChosen();
        Card top3 = new AvenFogbringer();
        Card belowTop = new GiantWarthog();
        Card blue1 = new AvenFogbringer();
        Card blue2 = new AvenFogbringer();
        Card blue3 = new AvenFogbringer();
        Card spell = new FlashOfInsight();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top1, top2, top3, belowTop));
        harness.setGraveyard(player1, List.of(spell, blue1, blue2, blue3));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, 3, null, List.of(), List.of(1, 2, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(blue3, blue2, blue1);

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(top1, top2, top3);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.reorderRemainingToBottom()).isTrue();
        assertThat(choice.randomRemainingToBottom()).isFalse();

        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTop, top3, top1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Normal casting looks at X cards and leaves the unchosen cards on the bottom")
    void normalCastLooksAtXCardsAndLeavesSpellInGraveyard() {
        Card top1 = new GiantWarthog();
        Card top2 = new AncestorsChosen();
        Card top3 = new AvenFogbringer();
        Card belowTop = new GiantWarthog();
        Card spell = new FlashOfInsight();

        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(top1, top2, top3, belowTop));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(top1, top2, top3);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.reorderRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTop, top3, top1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X=0 flashback pays mana, skips library input, and exiles the spell")
    void zeroXFlashbackDoesNothingToTheLibrary() {
        Card top = new GiantWarthog();
        Card spell = new FlashOfInsight();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback rejects a non-blue or incorrectly sized graveyard selection")
    void flashbackRequiresExactlyXBlueCards() {
        Card spell = new FlashOfInsight();
        Card blue = new AvenFogbringer();
        Card nonBlue = new GiantWarthog();
        harness.setGraveyard(player1, List.of(spell, blue, nonBlue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, blue, nonBlue);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flashback cannot exile Flash of Insight itself for its additional cost")
    void flashbackCannotExileItselfForItsAdditionalCost() {
        Card spell = new FlashOfInsight();
        Card blue = new AvenFogbringer();
        Card secondBlue = new AvenFogbringer();
        harness.setGraveyard(player1, List.of(spell, blue, secondBlue));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, blue, secondBlue);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
