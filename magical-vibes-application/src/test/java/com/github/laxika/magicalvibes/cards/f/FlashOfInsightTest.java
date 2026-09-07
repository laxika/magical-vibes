package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({FlashOfInsight.class, AirElemental.class, GrizzlyBears.class, Plains.class})
class FlashOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Flashback exiles X blue cards and looks at X cards")
    void flashbackExilesBlueCardsAndResolvesXEffect() {
        Card top1 = new GrizzlyBears();
        Card top2 = new Plains();
        Card top3 = new AirElemental();
        Card belowTop = new GrizzlyBears();
        Card blue1 = new AirElemental();
        Card blue2 = new AirElemental();
        Card blue3 = new AirElemental();
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
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flashback rejects a non-blue or incorrectly sized graveyard selection")
    void flashbackRequiresExactlyXBlueCards() {
        Card spell = new FlashOfInsight();
        Card blue = new AirElemental();
        Card nonBlue = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell, blue, nonBlue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell, blue, nonBlue);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
