package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MinisterOfImpediments;
import com.github.laxika.magicalvibes.cards.s.SlitheringShade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlivingPsychopath.class, SlitheringShade.class, MinisterOfImpediments.class, AzoriusSignet.class})
class UnlivingPsychopathTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability gives +1/-1 until end of turn")
    void boostsSelfUntilEndOfTurn() {
        Permanent psychopath = addReadyPsychopath(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, psychopath)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, psychopath)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, psychopath)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, psychopath)).isEqualTo(4);
    }

    @Test
    @DisplayName("The second ability destroys a creature with less power")
    void destroysCreatureWithLessPower() {
        Permanent psychopath = addReadyPsychopath(player1);
        Permanent target = addCreatureReady(player2, new SlitheringShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Slithering Shade");
        harness.assertInGraveyard(player2, "Slithering Shade");
        assertThat(psychopath.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability cannot target a creature with equal power")
    void requiresStrictlyLowerPower() {
        addReadyPsychopath(player1);
        Permanent target = addCreatureReady(player2, new MinisterOfImpediments());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than");
        harness.assertOnBattlefield(player2, "Minister of Impediments");
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature permanent")
    void onlyTargetsCreatures() {
        addReadyPsychopath(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertOnBattlefield(player2, "Azorius Signet");
    }

    @Test
    @DisplayName("The second ability rechecks the target's power when it resolves")
    void rechecksTargetPowerOnResolution() {
        Permanent psychopath = addReadyPsychopath(player1);
        Permanent target = addCreatureReady(player2, new SlitheringShade());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Slithering Shade");
        assertThat(psychopath.isTapped()).isTrue();
    }

    private Permanent addReadyPsychopath(Player player) {
        return addCreatureReady(player, new UnlivingPsychopath());
    }
}
