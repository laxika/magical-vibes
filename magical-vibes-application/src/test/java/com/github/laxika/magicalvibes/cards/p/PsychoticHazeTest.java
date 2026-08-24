package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
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

@CardUsed({PsychoticHaze.class, GrizzlyBears.class, RavensCrime.class})
class PsychoticHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PsychoticHaze()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(player1Bears.getMarkedDamage()).isEqualTo(1);
        assertThat(player2Bears.getMarkedDamage()).isEqualTo(1);
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

    private PsychoticHaze discardPsychoticHaze() {
        PsychoticHaze haze = new PsychoticHaze();
        harness.setHand(player1, List.of(haze));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return haze;
    }
}
