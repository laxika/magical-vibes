package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonOverseerTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Nested
    @DisplayName("Cast without kicker")
    class WithoutKicker {

        @Test
        @DisplayName("Enters the battlefield as 3/1 with no ETB trigger")
        void entersWithNoTrigger() {
            harness.setHand(player1, List.of(new KeldonOverseer()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Keldon Overseer"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No ETB trigger even with opponent creature on battlefield")
        void noTriggerEvenWithTarget() {
            addCreature(player2);
            harness.setHand(player1, List.of(new KeldonOverseer()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).isEmpty();
            // Opponent still controls their creature
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        }
    }

    // ===== Cast with kicker =====

    @Nested
    @DisplayName("Cast with kicker")
    class WithKicker {

        @Test
        @DisplayName("ETB triggered ability goes on the stack when kicked")
        void etbTriggersOnStack() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Keldon Overseer"));
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("Gains control of target creature until end of turn")
        void gainsControlOfTarget() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.untilEndOfTurnStolenCreatures).contains(target.getId());
        }

        @Test
        @DisplayName("Untaps the stolen creature")
        void untapsStolenCreature() {
            Permanent target = addCreature(player2);
            target.tap();
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(target.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Grants haste to the stolen creature")
        void grantsHasteToStolenCreature() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        }

        @Test
        @DisplayName("Control and haste expire at cleanup")
        void controlExpiresAtCleanup() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(target.getId()));
            assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
            assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(target.getId());
        }
    }

    // ===== Helpers =====

    private Permanent addCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castKicked(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KeldonOverseer()));
        // Base cost {2}{R} + Kicker {3}{R} = {5}{R}{R}
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
    }
}
