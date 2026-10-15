package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sleep.class, RuneclawBear.class, RodOfRuin.class, MindControl.class})
class SleepTest extends BaseCardTest {

    @CardUsed({Sleep.class, RuneclawBear.class, RodOfRuin.class, MindControl.class})
    @Nested
    @DisplayName("Spell resolution")
    class SpellResolution {

        @Test
        @DisplayName("Taps all creatures target player controls")
        void tapsAllCreatures() {
            harness.addToBattlefield(player2, new RuneclawBear());
            harness.addToBattlefield(player2, new RuneclawBear());
            List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
            assertThat(battlefield).hasSize(2);
            assertThat(battlefield).allMatch(p -> !p.isTapped());

            castAndResolveSleep(player2.getId());

            assertThat(battlefield).allMatch(Permanent::isTapped);
        }

        @Test
        @DisplayName("Sets skipUntapCount on all creatures target player controls")
        void setsSkipUntapOnAllCreatures() {
            harness.addToBattlefield(player2, new RuneclawBear());
            harness.addToBattlefield(player2, new RuneclawBear());
            List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());

            castAndResolveSleep(player2.getId());

            assertThat(battlefield).allMatch(p -> p.getSkipUntapCount() == 1);
        }

        @Test
        @DisplayName("Does not tap non-creature permanents")
        void doesNotTapNonCreatures() {
            harness.addToBattlefield(player2, new RodOfRuin());
            Permanent artifact = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(artifact.isTapped()).isFalse();

            castAndResolveSleep(player2.getId());

            assertThat(artifact.isTapped()).isFalse();
            assertThat(artifact.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Already tapped creatures also get skipUntapCount set")
        void alreadyTappedCreaturesAlsoGetSkipUntap() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.tap();
            assertThat(bears.isTapped()).isTrue();

            castAndResolveSleep(player2.getId());

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not affect caster's creatures when targeting opponent")
        void doesNotAffectCasterCreatures() {
            harness.addToBattlefield(player1, new RuneclawBear());
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent casterCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

            castAndResolveSleep(player2.getId());

            assertThat(casterCreature.isTapped()).isFalse();
            assertThat(casterCreature.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Can target self to tap own creatures")
        void canTargetSelf() {
            harness.addToBattlefield(player1, new RuneclawBear());
            Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

            castAndResolveSleep(player1.getId());

            assertThat(ownCreature.isTapped()).isTrue();
            assertThat(ownCreature.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Sleep goes to graveyard after resolving")
        void goesToGraveyard() {
            harness.addToBattlefield(player2, new RuneclawBear());

            castAndResolveSleep(player2.getId());

            harness.assertInGraveyard(player1, "Sleep");
        }
    }

    @CardUsed({Sleep.class, RuneclawBear.class, RodOfRuin.class, MindControl.class})
    @Nested
    @DisplayName("Untap step behavior")
    class UntapStepBehavior {

        @Test
        @DisplayName("Affected creature untaps normally after changing controllers")
        void changedControllerUntapsNormally() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveSleep(player2.getId());
            harness.setHand(player1, List.of(new MindControl()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            harness.castEnchantment(player1, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
            assertThat(bears.isTapped()).isTrue();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Creatures entering after Sleep resolves untap normally")
        void laterArrivalsUntapNormally() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent affected = gd.playerBattlefields.get(player2.getId()).getFirst();
            castAndResolveSleep(player2.getId());
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent arrival = gd.playerBattlefields.get(player2.getId()).getLast();
            arrival.tap();

            advanceToNextTurn(player1);

            assertThat(affected.isTapped()).isTrue();
            assertThat(arrival.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Affected creatures do not untap during next untap step")
        void creaturesDoNotUntapDuringNextUntapStep() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.setSummoningSick(false);

            castAndResolveSleep(player2.getId());
            assertThat(bears.isTapped()).isTrue();

            // Advance to player2's untap step
            advanceToNextTurn(player1);

            // Creature should still be tapped
            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected creatures untap normally on the turn after")
        void creaturesUntapOnFollowingTurn() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.setSummoningSick(false);

            castAndResolveSleep(player2.getId());

            // Advance through player2's turn (skip untap consumed)
            advanceToNextTurn(player1);
            assertThat(bears.isTapped()).isTrue();

            // Advance through player1's turn
            advanceToNextTurn(player2);

            // Advance to player2's next turn — creature should untap now
            advanceToNextTurn(player1);
            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Non-creature permanents of target player still untap normally")
        void nonCreaturesStillUntap() {
            harness.addToBattlefield(player2, new RodOfRuin());
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent artifact = gd.playerBattlefields.get(player2.getId()).get(0);
            Permanent bears = gd.playerBattlefields.get(player2.getId()).get(1);
            artifact.tap();
            bears.setSummoningSick(false);

            castAndResolveSleep(player2.getId());

            // Advance to player2's untap step
            advanceToNextTurn(player1);

            // Artifact should untap, creature should not
            assertThat(artifact.isTapped()).isFalse();
            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Resolves without error when target player has no creatures")
        void worksWithEmptyBattlefield() {
            castAndResolveSleep(player2.getId());

            // No error, Sleep goes to graveyard
            harness.assertInGraveyard(player1, "Sleep");
        }
    }

    private void castAndResolveSleep(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Sleep()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
