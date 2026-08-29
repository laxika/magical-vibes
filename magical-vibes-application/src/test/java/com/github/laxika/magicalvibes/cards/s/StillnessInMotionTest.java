package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StillnessInMotion.class, Forest.class, GrizzlyBears.class, Island.class, LlanowarElves.class, Mountain.class, Plains.class, Shock.class, Swamp.class})
class StillnessInMotionTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards without exiling itself while the library is not empty")
    void millsThreeCardsWithoutExilingWhileLibraryIsNotEmpty() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new Mountain()));
        Card stillness = new StillnessInMotion();
        harness.addToBattlefield(player1, stillness);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == stillness);
    }

    @Test
    @DisplayName("Exiles itself and returns five graveyard cards to the top in the chosen order")
    void exilesAndReturnsFiveCardsInChosenOrder() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new Plains();
        Card fourth = new Mountain();
        Card fifth = new Swamp();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(first, second, third, fourth, fifth));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new Shock()));
        Card stillness = new StillnessInMotion();
        harness.addToBattlefield(player1, stillness);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId(), fifth.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, fourth, third, second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third, fourth, fifth);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == stillness);
    }

    @Test
    @DisplayName("Triggers with an empty library and returns all available graveyard cards")
    void triggersWithEmptyLibrary() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new Plains();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(first, second, third));
        harness.setLibrary(player1, List.of());
        Card stillness = new StillnessInMotion();
        harness.addToBattlefield(player1, stillness);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == stillness);
    }
}
