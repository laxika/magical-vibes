package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarVanguard.class})
class LlanowarVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Llanowar Vanguard gives it +0/+4 until end of turn")
    void tappingBoostsToughnessUntilEndOfTurn() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());

        harness.activateAbility(player1, 0, null, null);
        assertThat(vanguard.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(5);
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("The tap cost is paid immediately but the boost waits for resolution")
    void boostUsesTheStack() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(vanguard.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(5);
    }

    @Test
    @DisplayName("A tapped Vanguard cannot pay the tap cost again")
    void tappedVanguardCannotActivate() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());
        vanguard.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Vanguard cannot pay the tap cost")
    void summoningSickVanguardCannotActivate() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new LlanowarVanguard());
        vanguard.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vanguard.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability boosts only the Vanguard that activated it")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new LlanowarVanguard());
        Permanent other = addCreatureReady(player1, new LlanowarVanguard());
        Permanent opposing = addCreatureReady(player2, new LlanowarVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);
        assertThat(other.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
    }
}
