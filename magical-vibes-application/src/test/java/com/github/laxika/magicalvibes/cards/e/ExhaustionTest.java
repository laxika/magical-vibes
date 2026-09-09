package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Exhaustion.class, AlabornTrooper.class, Forest.class, JayemdaeTome.class})
class ExhaustionTest extends BaseCardTest {

    // ===== Spell resolution =====

    @Nested
    @DisplayName("Spell resolution")
    class SpellResolution {

        @Test
        @DisplayName("Sets skipUntapCount on creatures target opponent controls without tapping them")
        void setsSkipUntapOnCreatures() {
            Permanent trooper = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());

            castAndResolveExhaustion(player2.getId());

            assertThat(trooper.getSkipUntapCount()).isEqualTo(1);
            assertThat(trooper.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Sets skipUntapCount on lands target opponent controls")
        void setsSkipUntapOnLands() {
            Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

            castAndResolveExhaustion(player2.getId());

            assertThat(forest.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not affect non-creature, non-land permanents")
        void doesNotAffectOtherPermanents() {
            Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());

            castAndResolveExhaustion(player2.getId());

            assertThat(artifact.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not affect caster's permanents")
        void doesNotAffectCasterPermanents() {
            Permanent casterCreature = harness.addToBattlefieldAndReturn(player1, new AlabornTrooper());
            harness.addToBattlefield(player2, new AlabornTrooper());

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
            Permanent trooper = addCreatureReady(player2, new AlabornTrooper());
            Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
            trooper.tap();
            forest.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextTurn(player1);

            assertThat(trooper.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected permanents untap normally on the turn after")
        void permanentsUntapOnFollowingTurn() {
            Permanent trooper = addCreatureReady(player2, new AlabornTrooper());
            trooper.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextTurn(player1);
            assertThat(trooper.isTapped()).isTrue();

            advanceToNextTurn(player2);
            advanceToNextTurn(player1);
            assertThat(trooper.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Only the target opponent's creatures and lands stay tapped")
        void onlyTargetCreaturesAndLandsStayTapped() {
            Permanent creature = addCreatureReady(player2, new AlabornTrooper());
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
            Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
            creature.tap();
            land.tap();
            artifact.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextTurn(player1);

            assertThat(creature.isTapped()).isTrue();
            assertThat(land.isTapped()).isTrue();
            assertThat(artifact.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void castAndResolveExhaustion(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Exhaustion()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
