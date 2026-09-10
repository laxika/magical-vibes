package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DefenseGrid;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeatherseedElf.class, GiantCockroach.class, DefenseGrid.class})
class WeatherseedElfTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Weatherseed Elf grants forestwalk to a target creature")
    void grantsForestwalkToTargetCreature() {
        Permanent elf = addReadyWeatherseedElf();
        Permanent target = addCreatureReady(player2, new GiantCockroach());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(elf.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Forestwalk wears off at end of turn")
    void forestwalkWearsOffAtEndOfTurn() {
        addReadyWeatherseedElf();
        Permanent target = addCreatureReady(player1, new GiantCockroach());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("A noncreature permanent cannot be targeted")
    void cannotTargetNoncreaturePermanent() {
        addReadyWeatherseedElf();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DefenseGrid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWeatherseedElf() {
        return addCreatureReady(player1, new WeatherseedElf());
    }
}
