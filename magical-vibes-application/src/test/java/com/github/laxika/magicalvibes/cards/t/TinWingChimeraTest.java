package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BrassTalonChimera;
import com.github.laxika.magicalvibes.cards.d.DwarvenVigilantes;
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

@CardUsed({TinWingChimera.class, BrassTalonChimera.class, DwarvenVigilantes.class})
class TinWingChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing puts a +2/+2 counter on target Chimera and grants it flying")
    void sacrificeBuffsTargetChimera() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new TinWingChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tin-Wing Chimera");
        harness.assertInGraveyard(player1, "Tin-Wing Chimera");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chimera);

        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying lasts indefinitely — survives cleanup")
    void flyingLastsIndefinitely() {
        harness.addToBattlefield(player1, new TinWingChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Chimera creature")
    void cannotTargetNonChimera() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TinWingChimera());
        Permanent nonChimera = harness.addToBattlefieldAndReturn(player1, new DwarvenVigilantes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonChimera.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertNotInGraveyard(player1, "Tin-Wing Chimera");
    }

    @Test
    @DisplayName("Can target a Chimera creature controlled by an opponent")
    void sacrificeBuffsOpponentsChimera() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new TinWingChimera());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrassTalonChimera());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chimera);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Targeting the source still sacrifices it without granting anything")
    void selfTargetDoesNotAffectSacrificedSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TinWingChimera());

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tin-Wing Chimera");
        harness.assertInGraveyard(player1, "Tin-Wing Chimera");
        assertThat(source.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }
}
