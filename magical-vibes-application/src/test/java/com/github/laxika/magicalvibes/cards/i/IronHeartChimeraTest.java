package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IronHeartChimera.class, BrassTalonChimera.class, DwarvenVigilantes.class})
class IronHeartChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing puts a +2/+2 counter on target Chimera and grants it vigilance")
    void sacrificeBuffsTargetChimera() {
        Permanent source = addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        activate(target);

        assertSacrificed(source);
        assertBuffed(target);
    }

    @Test
    @DisplayName("Granted vigilance lasts indefinitely - survives cleanup")
    void vigilanceLastsIndefinitely() {
        addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        activate(target);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertBuffed(target);
    }

    @Test
    @DisplayName("Cannot target a non-Chimera creature")
    void cannotTargetNonChimera() {
        Permanent source = addSource();
        Permanent dwarves = harness.addToBattlefieldAndReturn(player1, new DwarvenVigilantes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dwarves.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertNotInGraveyard(player1, "Iron-Heart Chimera");
    }

    @Test
    @DisplayName("Can target a Chimera creature controlled by an opponent")
    void sacrificeBuffsOpponentsChimera() {
        Permanent source = addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrassTalonChimera());

        activate(target);

        assertSacrificed(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertBuffed(target);
    }

    @Test
    @DisplayName("A self-targeted ability does not affect the sacrificed source")
    void selfTargetDoesNotAffectSacrificedSource() {
        Permanent source = addSource();

        activate(source);

        assertSacrificed(source);
        assertThat(source.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before the ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent source = addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        harness.activateAbility(player1, 0, null, target.getId());

        assertSacrificed(source);
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();

        assertBuffed(target);
    }

    @Test
    @DisplayName("A tapped Chimera with summoning sickness can pay the sacrifice cost")
    void tappedSummoningSickSourceCanActivate() {
        Permanent source = addSource();
        source.tap();
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        activate(target);

        assertSacrificed(source);
        assertBuffed(target);
    }

    @Test
    @DisplayName("Two sacrificed Chimeras place two separate +2/+2 counters")
    void multipleSacrificesAccumulateCounters() {
        addSource();
        addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        activate(target);
        activate(target);

        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A target sacrificed in response receives neither the counter nor vigilance")
    void targetSacrificedInResponseReceivesNoEffects() {
        Permanent source = addSource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrassTalonChimera());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertSacrificed(source);
        harness.assertInGraveyard(player1, "Brass-Talon Chimera");
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSource() {
        return harness.addToBattlefieldAndReturn(player1, new IronHeartChimera());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void assertSacrificed(Permanent source) {
        harness.assertNotOnBattlefield(player1, "Iron-Heart Chimera");
        harness.assertInGraveyard(player1, "Iron-Heart Chimera");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
    }

    private void assertBuffed(Permanent target) {
        assertThat(target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }
}
