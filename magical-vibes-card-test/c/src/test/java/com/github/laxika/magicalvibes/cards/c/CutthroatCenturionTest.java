package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutthroatCenturion.class, PropheticPrism.class, CopperLonglegs.class})
class CutthroatCenturionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact gives it +2/+2 until end of turn")
    void sacrificesAnotherArtifactAndBoostsItself() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new PropheticPrism());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+2 until end of turn")
    void sacrificesAnotherCreatureAndBoostsItself() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CopperLonglegs());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Copper Longlegs");
    }

    @Test
    @DisplayName("Cannot sacrifice itself")
    void cannotSacrificeItself() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(centurion);
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOnceEachTurn() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new PropheticPrism());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new PropheticPrism());
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(centurion);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new PropheticPrism());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(centurion.getPowerModifier()).isZero();
        assertThat(centurion.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrifice is paid before the boost resolves")
    void paysSacrificeBeforeResolution() {
        Permanent centurion = harness.addToBattlefieldAndReturn(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new PropheticPrism());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Prophetic Prism");
        assertThat(centurion.getPowerModifier()).isZero();
        assertThat(centurion.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact or creature")
    void cannotSacrificeOpponentsPermanents() {
        harness.addToBattlefield(player1, new CutthroatCenturion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact, creature);
    }

    @Test
    @DisplayName("Can activate again during the opponent's next turn")
    void activationLimitResetsEachTurn() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
    }

}
