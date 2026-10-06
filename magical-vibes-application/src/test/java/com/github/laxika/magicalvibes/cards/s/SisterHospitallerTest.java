package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisterHospitaller.class, HillGiant.class, GrizzlyBears.class, Shock.class})
class SisterHospitallerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target creature from your graveyard and gains life equal to its mana value")
    void etbReturnsCreatureAndGainsLifeEqualToManaValue() {
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));

        castAndResolveHospitaller();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("ETB only targets creature cards in your graveyard")
    void etbFiltersNoncreatureCardsAndOpponentsGraveyard() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));

        castAndResolveHospitaller();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("ETB does nothing when your graveyard has no creature cards")
    void etbDoesNothingWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new Shock()));

        castAndResolveHospitaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB cannot target creatures in an opponent's graveyard")
    void etbDoesNotTargetOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castAndResolveHospitaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB gains no life when its target leaves the graveyard before resolution")
    void etbDoesNotGainLifeWhenTargetLeavesGraveyard() {
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));

        castAndResolveHospitaller();
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(giant));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 20);
    }
    private void castAndResolveHospitaller() {
        harness.castFromHand(player1, new SisterHospitaller(), "{4}{W}{B}");
        harness.passBothPriorities();
    }
}
