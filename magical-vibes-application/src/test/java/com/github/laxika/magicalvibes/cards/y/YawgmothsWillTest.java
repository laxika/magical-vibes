package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YawgmothsWillTest extends BaseCardTest {

    @Test
    @DisplayName("Plays a land and casts a spell from the graveyard this turn")
    void playsLandAndCastsSpellFromGraveyard() {
        YawgmothsWill will = new YawgmothsWill();
        Forest forest = new Forest();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(will));
        harness.setGraveyard(player1, List.of(forest, bolt));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, bolt);
    }

    @Test
    @DisplayName("Exiles own cards that would enter the graveyard this turn")
    void exilesOwnCardsInsteadOfGraveyard() {
        YawgmothsWill will = new YawgmothsWill();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(will, shock));
        harness.addToBattlefield(player1, bears);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, shock, bears);
    }

    @Test
    @DisplayName("The graveyard replacement expires at the end of the turn")
    void replacementExpiresAtEndOfTurn() {
        YawgmothsWill will = new YawgmothsWill();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(will, shock));
        harness.addToBattlefield(player1, bears);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will).doesNotContain(shock, bears);
    }
}
