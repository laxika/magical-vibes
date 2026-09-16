package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogSmugglers;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongarmThug.class, BogSmugglers.class, RishadanPort.class})
class StrongarmThugTest extends BaseCardTest {

    private void castStrongarmThug() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrongarmThug()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a Mercenary card from its controller's graveyard to hand")
    void returnsMercenaryToHand() {
        BogSmugglers mercenary = new BogSmugglers();
        harness.setGraveyard(player1, List.of(mercenary));

        castStrongarmThug();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(mercenary.getId());

        harness.handleMultipleCardsChosen(player1, List.of(mercenary.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bog Smugglers");
        harness.assertNotInGraveyard(player1, "Bog Smugglers");
    }

    @Test
    @DisplayName("ETB cannot target a non-Mercenary card")
    void onlyTargetsMercenaryCards() {
        BogSmugglers mercenary = new BogSmugglers();
        RishadanPort port = new RishadanPort();
        harness.setGraveyard(player1, List.of(mercenary, port));

        castStrongarmThug();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(mercenary.getId());
    }

    @Test
    @DisplayName("ETB only offers Mercenary cards from its controller's graveyard")
    void onlyOffersControllersGraveyard() {
        BogSmugglers ownMercenary = new BogSmugglers();
        BogSmugglers opponentsMercenary = new BogSmugglers();
        harness.setGraveyard(player1, List.of(ownMercenary));
        harness.setGraveyard(player2, List.of(opponentsMercenary));

        castStrongarmThug();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownMercenary.getId());
    }

    @Test
    @DisplayName("ETB creates no choice when its controller has no Mercenary card")
    void noMercenaryCreatesNoChoice() {
        harness.setGraveyard(player1, List.of(new RishadanPort()));

        castStrongarmThug();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Strongarm Thug");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        BogSmugglers mercenary = new BogSmugglers();
        harness.setGraveyard(player1, List.of(mercenary));

        castStrongarmThug();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bog Smugglers");
        harness.assertNotInHand(player1, "Bog Smugglers");
    }
}
