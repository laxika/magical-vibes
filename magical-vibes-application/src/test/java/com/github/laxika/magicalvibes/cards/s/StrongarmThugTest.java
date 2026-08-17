package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        DauthiMercenary mercenary = new DauthiMercenary();
        harness.setGraveyard(player1, List.of(mercenary));

        castStrongarmThug();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(mercenary.getId());

        harness.handleMultipleCardsChosen(player1, List.of(mercenary.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dauthi Mercenary");
        harness.assertNotInGraveyard(player1, "Dauthi Mercenary");
    }

    @Test
    @DisplayName("ETB cannot target a non-Mercenary card")
    void onlyTargetsMercenaryCards() {
        DauthiMercenary mercenary = new DauthiMercenary();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(mercenary, bears));

        castStrongarmThug();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(mercenary.getId());
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        DauthiMercenary mercenary = new DauthiMercenary();
        harness.setGraveyard(player1, List.of(mercenary));

        castStrongarmThug();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauthi Mercenary");
        harness.assertNotInHand(player1, "Dauthi Mercenary");
    }
}
