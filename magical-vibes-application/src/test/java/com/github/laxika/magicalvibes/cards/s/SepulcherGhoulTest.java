package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SepulcherGhoul.class, GrizzlyBears.class})
class SepulcherGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Sepulcher Ghoul +2/+2 until end of turn")
    void sacrificingAnotherCreatureBoostsSelf() {
        Permanent ghoul = addCreatureReady(player1, new SepulcherGhoul());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ghoul).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ghoul = addCreatureReady(player1, new SepulcherGhoul());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addCreatureReady(player1, new SepulcherGhoul());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The ability cannot be activated without another creature")
    void requiresAnotherCreatureToSacrifice() {
        addCreatureReady(player1, new SepulcherGhoul());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
