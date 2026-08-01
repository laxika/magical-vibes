package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HearthCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target artifact creature")
    class DestroyArtifactCreatureMode {

        @Test
        @DisplayName("Destroys the targeted artifact creature")
        void destroysArtifactCreature() {
            harness.addToBattlefield(player2, new Ornithopter());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID thopterId = harness.getPermanentId(player2, "Ornithopter");
            harness.castInstant(player1, 0, 0, thopterId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a nonartifact creature")
        void cannotTargetNonartifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Attacking creatures get +1/+0 until end of turn")
    class BoostAttackersMode {

        @Test
        @DisplayName("Boosts attacking creatures with +1/+0")
        void boostsAttackingCreatures() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();

            assertThat(attacker.getEffectivePower()).isEqualTo(3);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
            assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
            assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Boost wears off at end of turn")
        void boostWearsOff() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();
            assertThat(attacker.getEffectivePower()).isEqualTo(3);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(attacker.getEffectivePower()).isEqualTo(2);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature with power 2 or less can't be blocked this turn")
    class UnblockableMode {

        @Test
        @DisplayName("Makes a creature with power 2 or less unblockable")
        void makesLowPowerCreatureUnblockable() {
            Permanent target = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(target.isCantBeBlocked()).isTrue();
        }

        @Test
        @DisplayName("Cannot target a creature with power greater than 2")
        void cannotTargetHighPower() {
            Permanent giant = addCreatureReady(player2, new HillGiant());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, giant.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Unblockable wears off at end of turn")
        void unblockableWearsOff() {
            Permanent target = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();
            assertThat(target.isCantBeBlocked()).isTrue();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(target.isCantBeBlocked()).isFalse();
        }
    }
}
