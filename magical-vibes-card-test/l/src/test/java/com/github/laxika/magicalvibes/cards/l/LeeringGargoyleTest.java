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
        harness.passUntil(TurnStep.UPKEEP);

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

    @Test
    @DisplayName("The stats and flying change only when the ability resolves")
    void effectsWaitForResolution() {
        Permanent gargoyle = addCreatureReady(player1, new LeeringGargoyle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gargoyle.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability affects only its source, not other Gargoyles")
    void affectsOnlySource() {
        Permanent source = addCreatureReady(player1, new LeeringGargoyle());
        Permanent friendly = addCreatureReady(player1, new LeeringGargoyle());
        Permanent opposing = addCreatureReady(player2, new LeeringGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isFalse();
        for (Permanent unaffected : new Permanent[]{friendly, opposing}) {
            assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.FLYING)).isTrue();
            assertThat(unaffected.isTapped()).isFalse();
        }
    }
}
