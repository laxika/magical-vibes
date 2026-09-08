package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LeeringGargoyle.class)
class LeeringGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Leering Gargoyle gets -2/+2 and loses flying")
    void activationSwapsStatsAndLosesFlying() {
        Permanent gargoyle = addCreatureReady(player1, new LeeringGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent gargoyle = addCreatureReady(player1, new LeeringGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Leering Gargoyle")
    void activationTapsSource() {
        Permanent gargoyle = addCreatureReady(player1, new LeeringGargoyle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gargoyle.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A summoning-sick Leering Gargoyle cannot activate its tap ability")
    void summoningSickCreatureCannotActivateAbility() {
        harness.addToBattlefield(player1, new LeeringGargoyle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
