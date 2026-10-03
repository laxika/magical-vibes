package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BontusLastReckoning.class, FrilledSandwalla.class, Swamp.class, Manalith.class})
class BontusLastReckoningTest extends BaseCardTest {

    @Nested
    @DisplayName("Destroy all creatures")
    @CardUsed({BontusLastReckoning.class, FrilledSandwalla.class, Swamp.class})
    class DestroyAllCreatures {

        @Test
        @DisplayName("Destroys all creatures on both sides")
        void destroysAllCreatures() {
            harness.addToBattlefield(player1, new FrilledSandwalla());
            harness.addToBattlefield(player2, new FrilledSandwalla());

            cast();

            harness.assertNotOnBattlefield(player1, "Frilled Sandwalla");
            harness.assertNotOnBattlefield(player2, "Frilled Sandwalla");
            harness.assertInGraveyard(player1, "Frilled Sandwalla");
            harness.assertInGraveyard(player2, "Frilled Sandwalla");
        }

        @Test
        @DisplayName("Leaves lands on the battlefield")
        void leavesLands() {
            harness.addToBattlefield(player1, new FrilledSandwalla());
            harness.addToBattlefield(player1, new Swamp());

            cast();

            harness.assertOnBattlefield(player1, "Swamp");
        }
    }

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    @CardUsed({BontusLastReckoning.class, Swamp.class})
    class LandsDontUntap {

        @Test
        @DisplayName("An untapped land at resolution is still prevented from untapping later")
        void initiallyUntappedLandIsRestricted() {
            Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
            cast();

            assertThat(swamp.isTapped()).isFalse();
            swamp.tap();
            harness.performUntapStep(player1);

            assertThat(swamp.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Controller's lands stay tapped through their next untap step, untap the turn after")
        void landsStayTappedThenUntap() {
            Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
            swamp.tap();

            cast();

            // player1's turn -> player2's untap -> player1's next untap (skip consumed)
            harness.performUntapStep(player2);
            harness.performUntapStep(player1);
            assertThat(swamp.isTapped()).isTrue();

            // Following untap step untaps normally
            harness.performUntapStep(player2);
            harness.performUntapStep(player1);
            assertThat(swamp.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
            opponentSwamp.tap();

            cast();

            // Reach player2's untap step — opponent's land untaps normally
            harness.performUntapStep(player2);

            assertThat(opponentSwamp.isTapped()).isFalse();
        }
    }

    @Test
    void landsEnteringAfterResolutionStayTappedDuringNextUntap() {
        cast();
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();

        harness.performUntapStep(player1);
        assertThat(swamp.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(swamp.isTapped()).isFalse();
    }

    @Test
    void landGivenToOpponentAfterResolutionUntapsForOpponent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        cast();
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        gd.playerBattlefields.get(player2.getId()).add(swamp);

        harness.performUntapStep(player2);

        assertThat(swamp.isTapped()).isFalse();
    }

    @Test
    void landAcquiredAfterResolutionStaysTappedDuringNextUntap() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        swamp.tap();
        cast();
        gd.playerBattlefields.get(player2.getId()).remove(swamp);
        gd.playerBattlefields.get(player1.getId()).add(swamp);

        harness.performUntapStep(player1);

        assertThat(swamp.isTapped()).isTrue();
    }

    @Test
    void multipleResolutionsRestrictOnlyOneUntapStep() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        cast();
        cast();

        harness.performUntapStep(player1);
        assertThat(swamp.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(swamp.isTapped()).isFalse();
    }

    @Test
    void noncreatureArtifactSurvivesAndUntapsNormally() {
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());
        manalith.tap();
        cast();

        harness.assertOnBattlefield(player1, "Manalith");
        harness.performUntapStep(player1);

        assertThat(manalith.isTapped()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new BontusLastReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
