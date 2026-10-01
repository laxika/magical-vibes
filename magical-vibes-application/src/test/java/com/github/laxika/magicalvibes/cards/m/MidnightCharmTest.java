package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.s.SulfurElemental;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightCharm.class, GaeasAnthem.class, SulfurElemental.class})
class MidnightCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 1 damage and gains 1 life")
    void damageModeDealsDamageAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SulfurElemental());
        harness.setLife(player1, 10);
        int targetControllerLife = gd.playerLifeTotals.get(player2.getId());

        castMode(0, target);

        harness.assertLife(player1, 11);
        harness.assertLife(player2, targetControllerLife);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Sulfur Elemental");
    }

    @Test
    @DisplayName("First-strike mode grants first strike until end of turn")
    void firstStrikeModeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SulfurElemental());

        castMode(1, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Tap mode taps the target creature")
    void tapModeTapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SulfurElemental());

        castMode(2, target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("All modes require a creature target")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());

        for (int mode = 0; mode < 3; mode++) {
            int modeIndex = mode;
            assertThatThrownBy(() -> {
                harness.setHand(player1, List.of(new MidnightCharm()));
                harness.addMana(player1, ManaColor.BLACK, 1);
                harness.castModalInstant(player1, 0, modeIndex, List.of(target.getId()));
            })
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a creature");
        }
    }

    private void castMode(int modeIndex, Permanent target) {
        harness.setHand(player1, List.of(new MidnightCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
