package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exhaustion.class, Forest.class, GrizzlyBears.class, JayemdaeTome.class})
class ExhaustionTest extends BaseCardTest {

    // ===== Spell resolution =====

    @Nested
    @DisplayName("Spell resolution")
    class SpellResolution {

        @Test
        @DisplayName("Prevents creatures from untapping without tapping them on resolution")
        void setsSkipUntapOnCreatures() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(bears.isTapped()).isFalse();
            bears.tap();
            advanceToNextTurn(player1);
            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Prevents lands target opponent controls from untapping")
        void setsSkipUntapOnLands() {
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            forest.tap();
            advanceToNextTurn(player1);
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Also affects creatures and lands entering before the target's next untap step")
        void affectsPermanentsEnteringAfterResolution() {
            castAndResolveExhaustion(player2.getId());

            Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent forest = harness.enterBattlefieldAndReturn(player2, new Forest());
            bears.setSummoningSick(false);
            bears.tap();
            forest.tap();

            advanceToNextTurn(player1);

            assertThat(bears.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Does not affect non-creature, non-land permanents")
        void doesNotAffectOtherPermanents() {
            harness.addToBattlefield(player2, new JayemdaeTome());
            Permanent artifact = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(artifact.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not affect caster's permanents")
        void doesNotAffectCasterPermanents() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent casterCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(casterCreature.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Cannot target self")
        void cannotTargetSelf() {
            harness.setHand(player1, List.of(new Exhaustion()));
            harness.addMana(player1, ManaColor.BLUE, 3);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be an opponent");
        }
    }

    // ===== Untap step behavior =====

    @Nested
    @DisplayName("Untap step behavior")
    class UntapStepBehavior {

        @Test
        @DisplayName("Tapped creatures and lands do not untap during the next untap step")
        void tappedPermanentsDoNotUntap() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new Forest());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).get(0);
            Permanent forest = gd.playerBattlefields.get(player2.getId()).get(1);
            bears.setSummoningSick(false);
            bears.tap();
            forest.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextTurn(player1);

            assertThat(bears.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected permanents untap normally on the turn after")
        void permanentsUntapOnFollowingTurn() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.setSummoningSick(false);
            bears.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextTurn(player1);
            assertThat(bears.isTapped()).isTrue();

            advanceToNextTurn(player2);
            advanceToNextTurn(player1);
            assertThat(bears.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void castAndResolveExhaustion(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Exhaustion()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
