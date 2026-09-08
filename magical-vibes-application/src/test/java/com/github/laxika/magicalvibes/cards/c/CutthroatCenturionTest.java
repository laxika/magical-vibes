package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CutthroatCenturionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact gives it +2/+2 until end of turn")
    void sacrificesAnotherArtifactAndBoostsItself() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Sacrificing another creature gives it +2/+2 until end of turn")
    void sacrificesAnotherCreatureAndBoostsItself() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isEqualTo(2);
        assertThat(centurion.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(centurion);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent centurion = addCreatureReady(player1, new CutthroatCenturion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(centurion.getPowerModifier()).isZero();
        assertThat(centurion.getToughnessModifier()).isZero();
    }
}
