package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalimarDepths.class, GrizzlyBears.class, Shock.class})
class HalimarDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HalimarDepths()));

        harness.playLand(player1, 0);

        Permanent depths = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(depths.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Looks at the top three cards and puts them back in the chosen order")
    void reordersTopThreeCardsOnEnter() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new HalimarDepths()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapping adds one blue mana")
    void tappingAddsBlueMana() {
        Permanent depths = harness.addToBattlefieldAndReturn(player1, new HalimarDepths());
        depths.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(depths.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Reorders all available cards when the library contains fewer than three")
    void reordersShortLibrary() {
        Card first = new HalimarDepths();
        Card second = new HalimarDepths();
        Card opponentCard = new HalimarDepths();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new HalimarDepths()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library needs no ordering decision")
    void emptyLibraryResolvesWithoutInteraction() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HalimarDepths()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
