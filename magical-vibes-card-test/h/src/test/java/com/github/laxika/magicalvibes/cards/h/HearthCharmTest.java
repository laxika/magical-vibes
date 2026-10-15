package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HearthCharm.class, ManOWar.class, PhyrexianWalker.class, SisaysRing.class, HulkingCyclops.class})
class HearthCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target artifact creature")
    @CardUsed({HearthCharm.class, PhyrexianWalker.class, ManOWar.class, SisaysRing.class})
    class DestroyArtifactCreatureMode {

        @Test
        @DisplayName("Destroys the targeted artifact creature")
        void destroysArtifactCreature() {
            Permanent walker = harness.addToBattlefieldAndReturn(player2, new PhyrexianWalker());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 0, walker.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a nonartifact creature")
        void cannotTargetNonartifact() {
            Permanent creature = harness.addToBattlefieldAndReturn(player2, new ManOWar());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);


            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an artifact that is not a creature")
        void cannotTargetNoncreatureArtifact() {
            Permanent ring = harness.addToBattlefieldAndReturn(player2, new SisaysRing());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);


            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, ring.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Attacking creatures get +1/+0 until end of turn")
    @CardUsed({HearthCharm.class, ManOWar.class, PhyrexianWalker.class})
    class BoostAttackersMode {

        @Test
        @DisplayName("Boosts attacking creatures with +1/+0")
        void boostsAttackingCreatures() {
            Permanent attacker = addCreatureReady(player2, new ManOWar());
            attacker.setAttacking(true);
            Permanent opponentAttacker = addCreatureReady(player2, new ManOWar());
            opponentAttacker.setAttacking(true);
            Permanent nonAttacker = addCreatureReady(player1, new ManOWar());

            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();

            assertThat(attacker.getEffectivePower()).isEqualTo(3);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
            assertThat(opponentAttacker.getEffectivePower()).isEqualTo(3);
            assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(2);
            assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
            assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        void boostBeforeCombatDoesNotAffectLaterAttackers() {
            Permanent creature = addCreatureReady(player1, new ManOWar());
            addCreatureReady(player2, new PhyrexianWalker());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();
            declareAttackersAndPrepareBlockers(List.of(0));

            assertThat(creature.getEffectivePower()).isEqualTo(2);
            assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Boost wears off at end of turn")
        void boostWearsOff() {
            Permanent attacker = addCreatureReady(player1, new ManOWar());
            attacker.setAttacking(true);

            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();
            assertThat(attacker.getEffectivePower()).isEqualTo(3);

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(TurnStep.CLEANUP);

            assertThat(attacker.getEffectivePower()).isEqualTo(2);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature with power 2 or less can't be blocked this turn")
    @CardUsed({HearthCharm.class, ManOWar.class, HulkingCyclops.class, PhyrexianWalker.class, SisaysRing.class})
    class UnblockableMode {

        @Test
        @DisplayName("Makes a creature with power 2 or less unblockable")
        void makesLowPowerCreatureUnblockable() {
            Permanent target = addCreatureReady(player1, new ManOWar());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(target.isCantBeBlocked()).isTrue();
        }

        @Test
        void resolvedUnblockableModePreventsBlockDeclaration() {
            Permanent target = addCreatureReady(player1, new ManOWar());
            Permanent blocker = addCreatureReady(player2, new PhyrexianWalker());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();
            declareAttackersAndPrepareBlockers(List.of(0));

            int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
            int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked");
        }

        @Test
        void powerIncreaseBeforeResolutionMakesTargetIllegal() {
            Permanent target = addCreatureReady(player1, new ManOWar());
            target.setAttacking(true);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.setHand(player1, List.of(new HearthCharm(), new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();
            assertThat(target.getEffectivePower()).isEqualTo(3);
            harness.passBothPriorities();

            assertThat(target.isCantBeBlocked()).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void powerIncreaseAfterResolutionDoesNotRemoveUnblockability() {
            Permanent target = addCreatureReady(player1, new ManOWar());
            target.setAttacking(true);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.setHand(player1, List.of(new HearthCharm(), new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();
            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();

            assertThat(target.getEffectivePower()).isEqualTo(3);
            assertThat(target.isCantBeBlocked()).isTrue();
        }

        @Test
        void canMakeOpponentsZeroPowerCreatureUnblockable() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianWalker());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            assertThat(target.isCantBeBlocked()).isTrue();
        }

        @Test
        void cannotMakeNoncreatureArtifactUnblockable() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new SisaysRing());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a creature with power greater than 2")
        void cannotTargetHighPower() {
            Permanent giant = addCreatureReady(player2, new HulkingCyclops());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, giant.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Unblockable wears off at end of turn")
        void unblockableWearsOff() {
            Permanent target = addCreatureReady(player1, new ManOWar());
            harness.setHand(player1, List.of(new HearthCharm()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();
            assertThat(target.isCantBeBlocked()).isTrue();

            harness.forceStep(TurnStep.END_STEP);
            harness.passUntil(TurnStep.CLEANUP);

            assertThat(target.isCantBeBlocked()).isFalse();
        }
    }
}
