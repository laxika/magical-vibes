package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenPony.class, DwarvenSoldier.class, GrizzlyBears.class})
class DwarvenPonyTest extends BaseCardTest {

    private void addPony() {
        Permanent pony = harness.addToBattlefieldAndReturn(player1, new DwarvenPony());
        pony.setSummoningSick(false);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants mountainwalk to a Dwarf, then it wears off at end of turn")
    void grantsMountainwalkToDwarf() {
        addPony();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenSoldier());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.MOUNTAINWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dwarf, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Dwarf creature")
    void cannotTargetNonDwarf() {
        addPony();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
