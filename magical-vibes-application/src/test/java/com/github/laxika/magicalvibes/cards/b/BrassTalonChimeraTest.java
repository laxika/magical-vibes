package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrassTalonChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing puts a +2/+2 counter on target Chimera and grants it first strike")
    void sacrificeBuffsTargetChimera() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brass-Talon Chimera");
        harness.assertInGraveyard(player1, "Brass-Talon Chimera");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chimera);

        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike lasts indefinitely — survives cleanup")
    void firstStrikeLastsIndefinitely() {
        harness.addToBattlefield(player1, new BrassTalonChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Chimera creature")
    void cannotTargetNonChimera() {
        harness.addToBattlefield(player1, new BrassTalonChimera());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
