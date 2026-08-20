package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimDraugrTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability requires snow mana")
    void requiresSnowMana() {
        Permanent draugr = addReadyDraugr();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, draugr)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, draugr, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Activating gives Grim Draugr +1/+0 and menace until end of turn")
    void boostsAndGrantsMenace() {
        Permanent draugr = addReadyDraugr();
        addSnowMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, draugr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, draugr)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, draugr, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent draugr = addReadyDraugr();
        addSnowMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, draugr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, draugr)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, draugr, Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyDraugr() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent draugr = harness.addToBattlefieldAndReturn(player1, new GrimDraugr());
        draugr.setSummoningSick(false);
        return draugr;
    }

    private void addSnowMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLACK, 1);
        pool.addSnowMana(ManaColor.BLACK, 1);
    }
}
