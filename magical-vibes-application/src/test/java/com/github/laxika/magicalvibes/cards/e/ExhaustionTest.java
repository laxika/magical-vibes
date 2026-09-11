package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Whetstone;
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

@CardUsed({Exhaustion.class, CoralMerfolk.class, Forest.class, Whetstone.class})
class ExhaustionTest extends BaseCardTest {

    // ===== Spell resolution =====

    @Nested
    @CardUsed({Exhaustion.class, CoralMerfolk.class, Forest.class, Whetstone.class})
    @DisplayName("Spell resolution")
    class SpellResolution {

        @Test
        @DisplayName("Sets skipUntapCount on creatures target opponent controls without tapping them")
        void setsSkipUntapOnCreatures() {
            harness.addToBattlefield(player2, new CoralMerfolk());
            Permanent merfolk = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(merfolk.getSkipUntapCount()).isEqualTo(1);
            assertThat(merfolk.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Sets skipUntapCount on lands target opponent controls")
        void setsSkipUntapOnLands() {
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(forest.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not affect non-creature, non-land permanents")
        void doesNotAffectOtherPermanents() {
            harness.addToBattlefield(player2, new Whetstone());
            Permanent artifact = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(artifact.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not affect caster's permanents")
        void doesNotAffectCasterPermanents() {
            harness.addToBattlefield(player1, new CoralMerfolk());
            harness.addToBattlefield(player2, new CoralMerfolk());
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

        @Test
        @DisplayName("Also affects creatures and lands entering before the targeted player's next untap step")
        void affectsPermanentsEnteringAfterResolution() {
            castAndResolveExhaustion(player2.getId());

            harness.addToBattlefield(player2, new CoralMerfolk());
            Permanent merfolk = gd.playerBattlefields.get(player2.getId()).getFirst();
            merfolk.tap();
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).get(1);
            forest.tap();

            advanceToNextUntap(player2);

            assertThat(merfolk.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }
    }

    // ===== Untap step behavior =====

    @Nested
    @CardUsed({Exhaustion.class, CoralMerfolk.class, Forest.class})
    @DisplayName("Untap step behavior")
    class UntapStepBehavior {

        @Test
        @DisplayName("Tapped creatures and lands do not untap during the next untap step")
        void tappedPermanentsDoNotUntap() {
            harness.addToBattlefield(player2, new CoralMerfolk());
            harness.addToBattlefield(player2, new Forest());
            Permanent merfolk = gd.playerBattlefields.get(player2.getId()).get(0);
            Permanent forest = gd.playerBattlefields.get(player2.getId()).get(1);
            merfolk.setSummoningSick(false);
            merfolk.tap();
            forest.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextUntap(player2);

            assertThat(merfolk.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected permanents untap normally on the turn after")
        void permanentsUntapOnFollowingTurn() {
            harness.addToBattlefield(player2, new CoralMerfolk());
            Permanent merfolk = gd.playerBattlefields.get(player2.getId()).getFirst();
            merfolk.setSummoningSick(false);
            merfolk.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToNextUntap(player2);
            assertThat(merfolk.isTapped()).isTrue();

            advanceToNextUntap(player1);
            advanceToNextUntap(player2);
            assertThat(merfolk.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void castAndResolveExhaustion(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Exhaustion()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void advanceToNextUntap(Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(activePlayer, TurnStep.UNTAP);
    }
}
