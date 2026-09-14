package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychoticHaze.class, PardicCollaborator.class, Unhinge.class})
class PsychoticHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent player1Collaborator = harness.addToBattlefieldAndReturn(player1, new PardicCollaborator());
        Permanent player2Collaborator = harness.addToBattlefieldAndReturn(player2, new PardicCollaborator());
        harness.castFromHand(player1, new PsychoticHaze(), "{2}{B}{B}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(player1Collaborator.getMarkedDamage()).isEqualTo(1);
        assertThat(player2Collaborator.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast for its madness cost after being discarded")
    void castsFromMadness() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        PsychoticHaze haze = discardPsychoticHaze();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Psychotic Haze");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(haze.getId()));
    }

    @Test
    @DisplayName("Puts the discarded card into its owner's graveyard when madness is declined")
    void declinesMadnessCast() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        discardPsychoticHaze();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Psychotic Haze");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private PsychoticHaze discardPsychoticHaze() {
        PsychoticHaze haze = new PsychoticHaze();
        harness.setHand(player1, List.of(haze));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return haze;
    }
}
