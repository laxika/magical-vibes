package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KavuGlider.class)
class KavuGliderTest extends BaseCardTest {

    @Test
    @DisplayName("White ability gives Kavu Glider +0/+1 until end of turn")
    void whiteAbilityBoostsToughness() {
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glider.getEffectivePower()).isEqualTo(2);
        assertThat(glider.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blue ability grants flying until end of turn")
    void blueAbilityGrantsFlying() {
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(glider.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Both abilities wear off at end of turn")
    void abilitiesWearOffAtEndOfTurn() {
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(glider.getEffectivePower()).isEqualTo(2);
        assertThat(glider.getEffectiveToughness()).isEqualTo(1);
        assertThat(glider.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each ability affects only the Kavu Glider whose ability was activated")
    void abilitiesAffectOnlyTheirSource() {
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        Permanent otherGlider = addCreatureReady(player1, new KavuGlider());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(glider.getEffectivePower()).isEqualTo(2);
        assertThat(glider.getEffectiveToughness()).isEqualTo(2);
        assertThat(glider.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(otherGlider.getEffectivePower()).isEqualTo(2);
        assertThat(otherGlider.getEffectiveToughness()).isEqualTo(1);
        assertThat(otherGlider.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("White ability can be activated multiple times and its boosts stack")
    void whiteAbilityStacks() {
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glider.getEffectivePower()).isEqualTo(2);
        assertThat(glider.getEffectiveToughness()).isEqualTo(3);
    }
}
