package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({ElvishPathcutter.class, LlanowarElves.class, GrizzlyBears.class})
class ElvishPathcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants forestwalk to target Elf creature")
    void grantsForestwalkToTargetElf() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk wears off at end of turn")
    void forestwalkWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target Elf creatures")
    void cannotTargetNonElfCreature() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf creature");
    }
}
