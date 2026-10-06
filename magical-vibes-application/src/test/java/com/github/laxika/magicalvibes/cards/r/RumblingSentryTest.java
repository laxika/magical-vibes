package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DaybreakChimera;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumblingSentry.class, DaybreakChimera.class})
class RumblingSentryTest extends BaseCardTest {

    @Test
    void entersWithScryOne() {
        harness.setHand(player1, List.of(new RumblingSentry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void canKeepTheTopCardWithoutChangingTheRemainingLibrary() {
        DaybreakChimera top = new DaybreakChimera();
        RumblingSentry next = new RumblingSentry();
        harness.setLibrary(player1, List.of(top, next));

        harness.enterBattlefieldAndReturn(player1, new RumblingSentry());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canPutTheTopCardOnTheBottom() {
        DaybreakChimera top = new DaybreakChimera();
        RumblingSentry next = new RumblingSentry();
        harness.setLibrary(player1, List.of(top, next));

        harness.enterBattlefieldAndReturn(player1, new RumblingSentry());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryNeedsNoScryChoice() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new RumblingSentry());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void enteringUnderTheOtherPlayersControlScriesOnlyTheirLibrary() {
        RumblingSentry firstPlayersTop = new RumblingSentry();
        DaybreakChimera secondPlayersTop = new DaybreakChimera();
        harness.setLibrary(player1, List.of(firstPlayersTop));
        harness.setLibrary(player2, List.of(secondPlayersTop));

        harness.enterBattlefieldAndReturn(player2, new RumblingSentry());
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(secondPlayersTop);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstPlayersTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondPlayersTop);
    }
}
