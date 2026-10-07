package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SultaiAscendancy.class, AlpineGrizzly.class})
class SultaiAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, surveils 2")
    void surveilsTwoAtBeginningOfOwnUpkeep() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new SultaiAscendancy());
        Card top0 = new AlpineGrizzly();
        Card top1 = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(top0, top1));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new SultaiAscendancy());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can keep both cards on top in reverse order")
    void canKeepBothCardsInReverseOrder() {
        harness.addToBattlefield(player1, new SultaiAscendancy());
        Card first = new AlpineGrizzly();
        Card second = new AlpineGrizzly();
        Card third = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(first, second, third));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can put one card into the graveyard and keep the other above untouched cards")
    void canSplitCardsBetweenLibraryAndGraveyard() {
        harness.addToBattlefield(player1, new SultaiAscendancy());
        Card first = new AlpineGrizzly();
        Card second = new AlpineGrizzly();
        Card third = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(first, second, third));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Surveil 2 looks at only one card when the library has one card")
    void surveilsOnlyAvailableCard() {
        harness.addToBattlefield(player1, new SultaiAscendancy());
        Card onlyCard = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(onlyCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Surveilling an empty library completes without a choice or a draw")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new SultaiAscendancy());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }
}
