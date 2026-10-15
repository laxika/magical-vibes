package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.f.FiresOfUndeath;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClingingMists.class, DawntreaderElk.class, FiresOfUndeath.class})
class ClingingMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ClingingMists()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Nested
    @DisplayName("Combat damage prevention")
    @CardUsed({ClingingMists.class, DawntreaderElk.class, FiresOfUndeath.class})
    class CombatDamagePrevention {

        @Test
        @DisplayName("Prevents combat damage to players and creatures")
        void preventsDamageToPlayersAndCreatures() {
            Permanent blockedAttacker = addCreatureReady(player2, new DawntreaderElk());
            Permanent unblockedAttacker = addCreatureReady(player2, new DawntreaderElk());
            Permanent blocker = addCreatureReady(player1, new DawntreaderElk());
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            declareAttackers(player2, List.of(0, 1));
            harness.castAndResolveInstant(player1, 0);
            prepareDeclareBlockers(player2);
            gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
            harness.passBothPriorities();

            harness.assertLife(player1, 20);
            assertThat(blockedAttacker.getMarkedDamage()).isZero();
            assertThat(unblockedAttacker.getMarkedDamage()).isZero();
            assertThat(blocker.getMarkedDamage()).isZero();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .containsExactly(blockedAttacker, unblockedAttacker);
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(blocker);
        }

        @Test
        @DisplayName("Does not prevent noncombat damage")
        void doesNotPreventNoncombatDamage() {
            harness.setHand(player1, List.of(new ClingingMists(), new FiresOfUndeath()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castAndResolveInstant(player1, 0);
            harness.castAndResolveInstant(player1, 0, player2.getId());

            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("Combat damage prevention expires at end of turn")
        void preventionExpiresAtEndOfTurn() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.castAndResolveInstant(player1, 0);

            harness.passUntil(player2, TurnStep.UPKEEP);
            attacker.setAttacking(true);
            resolveCombat(player2);

            harness.assertLife(player1, 18);
        }

        @Test
        @DisplayName("Prevents all combat damage after resolving")
        void preventsAllCombatDamage() {
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.preventAllCombatDamage).isTrue();
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Clinging Mists");
        }
    }

    @Nested
    @DisplayName("Fateful hour")
    @CardUsed({ClingingMists.class, DawntreaderElk.class})
    class FatefulHour {

        @Test
        @DisplayName("Fateful hour freezes every attacker but leaves the blocker unaffected")
        void freezesAllAttackersInCombat() {
            Permanent firstAttacker = addCreatureReady(player2, new DawntreaderElk());
            Permanent secondAttacker = addCreatureReady(player2, new DawntreaderElk());
            Permanent blocker = addCreatureReady(player1, new DawntreaderElk());
            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            declareAttackers(player2, List.of(0, 1));
            harness.castAndResolveInstant(player1, 0);
            prepareDeclareBlockers(player2);
            gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
            harness.passBothPriorities();

            harness.assertLife(player1, 5);
            assertThat(blocker.isTapped()).isFalse();
            assertThat(blocker.getSkipUntapCount()).isZero();
            assertThat(blocker.getMarkedDamage()).isZero();
            harness.performUntapStep(player2);
            assertThat(firstAttacker.isTapped()).isTrue();
            assertThat(secondAttacker.isTapped()).isTrue();
            assertThat(firstAttacker.getSkipUntapCount()).isZero();
            assertThat(secondAttacker.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Fateful hour checks life at resolution rather than casting")
        void activatesWhenLifeDropsBeforeResolution() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);
            harness.setLife(player1, 6);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.setLife(player1, 5);
            harness.passBothPriorities();

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Fateful hour does not apply if life rises before resolution")
        void doesNotActivateWhenLifeRisesBeforeResolution() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);
            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.setLife(player1, 6);
            harness.passBothPriorities();

            assertThat(attacker.isTapped()).isFalse();
            assertThat(attacker.getSkipUntapCount()).isZero();
            assertThat(gd.preventAllCombatDamage).isTrue();
        }

        @Test
        @DisplayName("Already tapped attackers skip only their controller's next untap step")
        void freezesAlreadyTappedAttackersUntilTheirControllersNextUntap() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);
            attacker.tap();
            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.castAndResolveInstant(player1, 0);

            harness.performUntapStep(player1);
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
            harness.performUntapStep(player2);

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isZero();
            harness.performUntapStep(player2);
            assertThat(attacker.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Taps attacking creatures when controller has 5 life")
        void tapsAttackingCreaturesAtFiveLife() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Sets skipUntapCount on attacking creatures when controller has 5 life")
        void setsSkipUntapOnAttackingCreaturesAtFiveLife() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Taps attacking creatures when controller has less than 5 life")
        void tapsAttackingCreaturesBelowFiveLife() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 1);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not tap or freeze attacking creatures when controller has more than 5 life")
        void doesNotTapOrFreezeAboveFiveLife() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 6);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            // Attacker may be tapped from attacking, but not from the spell
            assertThat(attacker.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not tap or freeze attacking creatures at default 20 life")
        void doesNotTapOrFreezeAtDefaultLife() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(attacker.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not affect non-attacking creatures even with fateful hour")
        void doesNotAffectNonAttackingCreatures() {
            Permanent nonAttacker = addCreatureReady(player2, new DawntreaderElk());

            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(nonAttacker.isTapped()).isFalse();
            assertThat(nonAttacker.getSkipUntapCount()).isZero();
            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Affected creatures do not untap during next untap step")
        void affectedCreaturesDoNotUntapDuringNextUntapStep() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);

            // Advance to player2's untap step
            harness.performUntapStep(player2);

            // Creature should still be tapped (skip untap consumed)
            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected creatures untap normally on the turn after")
        void affectedCreaturesUntapOnFollowingTurn() {
            Permanent attacker = addCreatureReady(player2, new DawntreaderElk());
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            harness.performUntapStep(player2);
            assertThat(attacker.isTapped()).isTrue();

            harness.performUntapStep(player1);

            harness.performUntapStep(player2);
            assertThat(attacker.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Still prevents combat damage even with fateful hour active")
        void stillPreventsCombatDamageWithFatefulHour() {
            harness.addToBattlefield(player2, new DawntreaderElk());

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.preventAllCombatDamage).isTrue();
        }
    }

}
