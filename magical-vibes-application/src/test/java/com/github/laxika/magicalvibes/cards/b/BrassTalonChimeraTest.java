package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IronHeartChimera;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrassTalonChimera.class, IronHeartChimera.class, Warthog.class})
class BrassTalonChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing puts a +2/+2 counter on target Chimera and grants it first strike")
    void sacrificeBuffsTargetChimera() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHeartChimera());

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
    @DisplayName("Granted first strike lasts indefinitely - survives cleanup")
    void firstStrikeLastsIndefinitely() {
        harness.addToBattlefield(player1, new BrassTalonChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronHeartChimera());

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
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new Warthog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chimera);
        harness.assertNotInGraveyard(player1, "Brass-Talon Chimera");
    }

    @Test
    @DisplayName("Can target a Chimera creature controlled by an opponent")
    void sacrificeBuffsOpponentsChimera() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronHeartChimera());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chimera);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }
}
