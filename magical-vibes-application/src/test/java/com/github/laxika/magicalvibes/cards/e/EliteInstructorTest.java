package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliteInstructor.class, Forest.class, Island.class, SternDismissal.class})
class EliteInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by drawing a card, then discarding a card")
    void entersDrawsThenDiscards() {
        Island discarded = new Island();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new EliteInstructor(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The newly drawn card can be discarded instead of a card already in hand")
    void canDiscardNewlyDrawnCard() {
        Island kept = new Island();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new EliteInstructor());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(drawn));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With an empty hand, the drawn card must be discarded")
    void emptyHandStillDiscardsDrawnCard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new EliteInstructor(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player1, "Elite Instructor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still draws and discards after the instructor is returned to hand")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        EliteInstructor instructor = new EliteInstructor();
        Forest drawn = new Forest();
        Island opponentCard = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, instructor, "{2}{U}");
        harness.setHand(player2, List.of(new SternDismissal(), opponentCard));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.assertNotOnBattlefield(player1, "Elite Instructor");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instructor);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instructor, drawn);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(instructor));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instructor);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }
}
