package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpirePatrol.class, AegisAutomaton.class})
class SpirePatrolTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({SpirePatrol.class, AegisAutomaton.class})
    class EnterTheBattlefield {

        @Test
        @DisplayName("Taps target creature an opponent controls")
        void tapsTargetCreature() {
            harness.addToBattlefield(player2, new AegisAutomaton());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            castSpirePatrol(player2);
            resolveAllTriggers();

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Target creature doesn't untap during its controller's next untap step")
        void targetSkipsNextUntap() {
            harness.addToBattlefield(player2, new AegisAutomaton());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castSpirePatrol(player2);
            resolveAllTriggers();

            assertThat(bears.isTapped()).isTrue();
            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        void remainsTappedForOnlyItsControllersNextUntapStep() {
            harness.addToBattlefield(player2, new AegisAutomaton());
            Permanent target = findPermanent(player2, "Aegis Automaton");
            castSpirePatrol(player2);
            resolveAllTriggers();

            harness.performUntapStep(player1);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }

        @Test
        void alreadyTappedCreatureStillSkipsNextUntap() {
            harness.addToBattlefield(player2, new AegisAutomaton());
            Permanent target = findPermanent(player2, "Aegis Automaton");
            target.tap();
            castSpirePatrol(player2);
            resolveAllTriggers();

            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isFalse();
        }

        @Test
        void canEnterWithoutAnOpposingCreature() {
            harness.setHand(player1, List.of(new SpirePatrol()));
            addSpirePatrolMana();
            harness.castCreature(player1, 0);
            resolveAllTriggers();

            harness.assertOnBattlefield(player1, "Spire Patrol");
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({SpirePatrol.class, AegisAutomaton.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("Cannot target own creature")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new AegisAutomaton());
            UUID ownBearId = harness.getPermanentId(player1, "Aegis Automaton");
            harness.setHand(player1, List.of(new SpirePatrol()));
            addSpirePatrolMana();

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castSpirePatrol(Player targetOwner) {
        UUID targetId = harness.getPermanentId(targetOwner, "Aegis Automaton");
        harness.setHand(player1, List.of(new SpirePatrol()));
        addSpirePatrolMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addSpirePatrolMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
