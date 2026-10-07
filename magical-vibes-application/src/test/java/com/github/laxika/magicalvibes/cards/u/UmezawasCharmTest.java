package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmezawasCharm.class, GrizzlyBears.class, Forest.class, ChangelingOutcast.class})
class UmezawasCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target creature gets +2/+2 until end of turn")
    @CardUsed({UmezawasCharm.class, GrizzlyBears.class})
    class PumpMode {

        @Test
        void boostsTargetCreature() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            cast(0, bear.getId());

            assertThat(bear.getPowerModifier()).isEqualTo(2);
            assertThat(bear.getToughnessModifier()).isEqualTo(2);
        }

        @Test
        void boostWearsOffAtEndOfTurn() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            cast(0, bear.getId());
            harness.forceStep(TurnStep.END_STEP);
            harness.passBothPriorities();

            assertThat(bear.getPowerModifier()).isZero();
            assertThat(bear.getToughnessModifier()).isZero();
        }

        @Test
        void canBoostOpponentsCreatureWithoutGainingLife() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setLife(player1, 15);

            cast(0, bear.getId());

            assertThat(bear.getPowerModifier()).isEqualTo(2);
            assertThat(bear.getToughnessModifier()).isEqualTo(2);
            harness.assertLife(player1, 15);
        }
    }

    @Nested
    @DisplayName("Mode 1: Target creature gets -1/-1 until end of turn")
    @CardUsed({UmezawasCharm.class, GrizzlyBears.class, Forest.class, ChangelingOutcast.class})
    class DebuffMode {

        @Test
        void weakensTargetCreature() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(1, bear.getId());

            assertThat(bear.getPowerModifier()).isEqualTo(-1);
            assertThat(bear.getToughnessModifier()).isEqualTo(-1);
        }

        @Test
        void cannotTargetNonCreaturePermanent() {
            harness.addToBattlefield(player2, new Forest());
            harness.setHand(player1, List.of(new UmezawasCharm()));
            addMana();

            assertThatThrownBy(() -> harness.castInstant(
                    player1, 0, 1, harness.getPermanentId(player2, "Forest")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a creature");
        }

        @Test
        void debuffWearsOffAtEndOfTurn() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(1, bear.getId());
            harness.forceStep(TurnStep.END_STEP);
            harness.passBothPriorities();

            assertThat(bear.getPowerModifier()).isZero();
            assertThat(bear.getToughnessModifier()).isZero();
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        void putsCreatureWithZeroToughnessIntoGraveyard() {
            Permanent outcast = harness.addToBattlefieldAndReturn(player2, new ChangelingOutcast());

            cast(1, outcast.getId());

            harness.assertNotOnBattlefield(player2, "Changeling Outcast");
            harness.assertInGraveyard(player2, "Changeling Outcast");
        }

        @Test
        void canWeakenControllersCreature() {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            cast(1, bear.getId());

            assertThat(bear.getPowerModifier()).isEqualTo(-1);
            assertThat(bear.getToughnessModifier()).isEqualTo(-1);
        }
    }

    @Test
    @DisplayName("Mode 2: You gain 2 life")
    void gainsTwoLife() {
        harness.setLife(player1, 15);
        cast(2, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new UmezawasCharm()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
