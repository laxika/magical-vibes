package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exhaustion.class, Forest.class, ForestBear.class, JayemdaeTome.class})
class ExhaustionTest extends BaseCardTest {

    @Nested
    @DisplayName("Spell resolution")
    class SpellResolution {

        @Test
        @DisplayName("Prevents creatures from untapping without tapping them on resolution")
        void setsSkipUntapOnCreatures() {
            harness.addToBattlefield(player2, new ForestBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(bears.isTapped()).isFalse();
            bears.tap();
            advanceToUpkeep(player2);
            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Prevents lands target opponent controls from untapping")
        void setsSkipUntapOnLands() {
            harness.addToBattlefield(player2, new Forest());
            Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            forest.tap();
            advanceToUpkeep(player2);
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Also affects creatures and lands entering before the target's next untap step")
        void affectsPermanentsEnteringAfterResolution() {
            castAndResolveExhaustion(player2.getId());

            Permanent bears = harness.enterBattlefieldAndReturn(player2, new ForestBear());
            Permanent forest = harness.enterBattlefieldAndReturn(player2, new Forest());
            bears.setSummoningSick(false);
            bears.tap();
            forest.tap();

            advanceToUpkeep(player2);

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
            harness.addToBattlefield(player1, new ForestBear());
            harness.addToBattlefield(player2, new ForestBear());
            Permanent casterCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

            castAndResolveExhaustion(player2.getId());

            assertThat(casterCreature.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not prevent a permanent from untapping after it changes controller")
        void doesNotAffectPermanentAfterControlChange() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new ForestBear());
            bears.setSummoningSick(false);
            bears.tap();

            castAndResolveExhaustion(player2.getId());

            harness.inMutationScope(() -> {
                gd.playerBattlefields.get(player2.getId()).remove(bears);
                gd.playerBattlefields.get(player1.getId()).add(bears);
                bears.recordControlChange();
                bears.setSummoningSick(true);
            });

            advanceToUpkeep(player1);

            assertThat(bears.isTapped()).isFalse();
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

    @Nested
    @DisplayName("Untap step behavior")
    class UntapStepBehavior {

        @Test
        @DisplayName("Tapped creatures and lands do not untap during the next untap step")
        void tappedPermanentsDoNotUntap() {
            harness.addToBattlefield(player2, new ForestBear());
            harness.addToBattlefield(player2, new Forest());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).get(0);
            Permanent forest = gd.playerBattlefields.get(player2.getId()).get(1);
            bears.setSummoningSick(false);
            bears.tap();
            forest.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToUpkeep(player2);

            assertThat(bears.isTapped()).isTrue();
            assertThat(forest.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected permanents untap normally on the turn after")
        void permanentsUntapOnFollowingTurn() {
            harness.addToBattlefield(player2, new ForestBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.setSummoningSick(false);
            bears.tap();

            castAndResolveExhaustion(player2.getId());

            advanceToUpkeep(player2);
            assertThat(bears.isTapped()).isTrue();

            advanceToUpkeep(player1);
            advanceToUpkeep(player2);
            assertThat(bears.isTapped()).isFalse();
        }
    }

    private void castAndResolveExhaustion(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Exhaustion()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

}
