package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClutchOfTheUndercity.class, GrizzlyBears.class, Island.class, Shock.class})
class ClutchOfTheUndercityTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentAndItsControllerLosesThreeLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClutchOfTheUndercity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void canReturnALandToItsOwnersHand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ClutchOfTheUndercity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        ClutchOfTheUndercity clutch = new ClutchOfTheUndercity();
        ClutchOfTheUndercity matchingCard = new ClutchOfTheUndercity();
        Shock differentManaValue = new Shock();
        harness.setHand(player1, List.of(clutch));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Clutch of the Undercity");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
