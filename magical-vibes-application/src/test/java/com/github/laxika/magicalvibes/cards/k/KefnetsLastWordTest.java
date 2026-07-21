package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KefnetsLastWordTest extends BaseCardTest {

    // ===== Gain control =====

    @Nested
    @DisplayName("Gain control of target artifact, creature, or enchantment")
    class GainControl {

        @Test
        @DisplayName("Gains control of a target creature")
        void gainsControlOfCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(target);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Gains control of a target artifact")
        void gainsControlOfArtifact() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

            cast(target);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Gains control of a target enchantment")
        void gainsControlOfEnchantment() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            pacifism.setAttachedTo(bears.getId());

            cast(pacifism);

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(pacifism.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(pacifism.getId()));
        }

        @Test
        @DisplayName("Control change is permanent — the creature is still yours after end of turn")
        void controlIsPermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            cast(target);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Cannot target a land")
        void cannotTargetLand() {
            harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // a legal target so the spell is playable
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
            harness.setHand(player1, List.of(new KefnetsLastWord()));
            harness.addMana(player1, ManaColor.BLUE, 4);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be an artifact, creature, or enchantment");
        }
    }

    // ===== Lands don't untap =====

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    class LandsDontUntap {

        @Test
        @DisplayName("Marks the controller's lands to skip their next untap")
        void marksControllerLands() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            plains.tap();
            island.tap();

            cast(target);

            assertThat(plains.getSkipUntapCount()).isEqualTo(1);
            assertThat(island.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
            opponentPlains.tap();

            cast(target);

            assertThat(opponentPlains.getSkipUntapCount()).isZero();
        }
    }

    // ===== Helpers =====

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new KefnetsLastWord()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
