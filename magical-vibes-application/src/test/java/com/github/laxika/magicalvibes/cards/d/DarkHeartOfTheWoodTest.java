package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkHeartOfTheWood.class, Forest.class, Island.class, TempleGarden.class})
class DarkHeartOfTheWoodTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest gains 3 life")
    void sacrificeForestGainsThreeLife() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Can sacrifice a nonbasic land with the Forest subtype")
    void sacrificeNonbasicForestGainsThreeLife() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefield(player1, new TempleGarden());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Temple Garden");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Forest land")
    void cannotSacrificeNonForestLand() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefield(player1, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("A tapped Forest is sacrificed immediately, before life is gained")
    void tappedForestIsPaidAsCostBeforeResolution() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Dark Heart of the Wood");

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Forest cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsForest() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The ability can be activated during an opponent's upkeep")
    void canActivateDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DarkHeartOfTheWood());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Forest");
    }
}
