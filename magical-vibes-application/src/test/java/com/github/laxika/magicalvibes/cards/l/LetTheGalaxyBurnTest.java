package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LetTheGalaxyBurn.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class LetTheGalaxyBurnTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X plus 2 damage only to creatures that did not enter this turn")
    void damagesCreaturesThatDidNotEnterThisTurn() {
        Permanent olderCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent enteredCreature = harness.enterBattlefieldAndReturn(player2, new HillGiant());

        castWithLandsOnly(0);

        assertThat(olderCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(enteredCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Casts a lesser spell with cascade")
    void cascades() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LetTheGalaxyBurn()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorceryForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Includes the chosen X in the cascade threshold")
    void cascadesIntoSixManaCardWhenXIsOne() {
        LetTheGalaxyBurn cascadeHit = new LetTheGalaxyBurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(cascadeHit));
        harness.setHand(player1, List.of(new LetTheGalaxyBurn()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorceryForX(player1, 0, 1, Map.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(cascadeHit);
    }

    @Test
    @DisplayName("Positive X damages older creatures on both sides but not players or lands")
    void positiveXDamagesBothControllersWithoutDamagingPlayersOrLands() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        castWithLandsOnly(1);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(land.getMarkedDamage()).isZero();
        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife);
    }

    private void castWithLandsOnly(int xValue) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new LetTheGalaxyBurn()));
        harness.addMana(player1, ManaColor.RED, 6 + xValue);

        harness.castSorceryForX(player1, 0, xValue, Map.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
