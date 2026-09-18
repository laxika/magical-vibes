package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaiLiCensor.class, GrizzlyBears.class})
class DaiLiCensorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Dai Li Censor +2/+2 until end of turn")
    void sacrificesAnotherCreatureAndBoostsItself() {
        Permanent censor = addCensorReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dai Li Censor cannot sacrifice itself")
    void cannotSacrificeItself() {
        addCensorReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dai Li Censor can be activated only once each turn")
    void onlyOnceEachTurn() {
        addCensorReady();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The Dai Li Censor boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent censor = addCensorReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isZero();
        assertThat(censor.getToughnessModifier()).isZero();
    }

    private Permanent addCensorReady() {
        Permanent censor = harness.addToBattlefieldAndReturn(player1, new DaiLiCensor());
        censor.setSummoningSick(false);
        return censor;
    }
}
