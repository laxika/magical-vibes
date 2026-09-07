package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneforgeAcolyte.class, HadaFreeblade.class, LeoninScimitar.class,
        GrizzlyBears.class})
class StoneforgeAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort finds an Equipment among the top four and lets you order the rest")
    void cohortFindsEquipmentAndOrdersRest() {
        Card first = new GrizzlyBears();
        Card equipment = new LeoninScimitar();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, equipment, third, fourth));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();

        assertThat(acolyte.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(equipment);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        remaining.indexOf(fourth),
                        remaining.indexOf(third),
                        remaining.indexOf(first))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Cohort puts all four cards on the bottom when no Equipment is found")
    void putsAllCardsOnBottomWhenNoEquipmentIsFound() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        remaining.indexOf(fourth),
                        remaining.indexOf(third),
                        remaining.indexOf(second),
                        remaining.indexOf(first))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
