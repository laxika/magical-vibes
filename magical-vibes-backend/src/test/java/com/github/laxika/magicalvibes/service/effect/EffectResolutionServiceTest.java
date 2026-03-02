package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.c.ConcussiveBolt;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EffectResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // Helpers
    // =========================================================================

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Spellbook());
        }
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // =========================================================================
    // MetalcraftConditionalEffect
    // (ConcussiveBolt: DealDamageToTargetPlayerEffect(4) + MetalcraftConditionalEffect(TargetPlayerCreaturesCantBlockThisTurnEffect))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftConditionalEffect")
    class ResolveMetalcraftConditionalEffect {

        @Test
        @DisplayName("Skips wrapped effect when metalcraft is not met")
        void skipsWrappedEffectWhenMetalcraftNotMet() {
            harness.setLife(player2, 20);
            Permanent creature = addReadyCreature(player2);

            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Damage effect still resolves
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
            // Metalcraft conditional effect is skipped
            assertThat(creature.isCantBlockThisTurn()).isFalse();
        }

        @Test
        @DisplayName("Logs skip message when metalcraft is not met")
        void logsSkipMessageWhenMetalcraftNotMet() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("Concussive Bolt") && log.contains("does nothing")
                            && log.contains("fewer than three artifacts"));
        }

        @Test
        @DisplayName("Resolves wrapped effect when metalcraft is met")
        void resolvesWrappedEffectWhenMetalcraftMet() {
            harness.setLife(player2, 20);
            Permanent creature = addReadyCreature(player2);

            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            addArtifacts(player1, 3);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
            assertThat(creature.isCantBlockThisTurn()).isTrue();
        }

        @Test
        @DisplayName("Re-checks metalcraft condition at resolution time (intervening-if)")
        void rechecksMetalcraftConditionAtResolutionTime() {
            harness.setLife(player2, 20);
            Permanent creature = addReadyCreature(player2);

            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            addArtifacts(player1, 3);

            harness.castSorcery(player1, 0, player2.getId());

            // Remove artifacts before resolution — metalcraft fails at resolve time
            gd.playerBattlefields.get(player1.getId()).removeIf(
                    p -> p.getCard().getName().equals("Spellbook"));

            harness.passBothPriorities();

            // Damage still applied (non-metalcraft effect)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
            // Can't-block skipped since metalcraft lost
            assertThat(creature.isCantBlockThisTurn()).isFalse();
        }

        @Test
        @DisplayName("Logs skip message when metalcraft lost before resolution")
        void logsSkipMessageWhenMetalcraftLostBeforeResolution() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            addArtifacts(player1, 3);

            harness.castSorcery(player1, 0, player2.getId());

            // Remove artifacts before resolution
            gd.playerBattlefields.get(player1.getId()).removeIf(
                    p -> p.getCard().getName().equals("Spellbook"));

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("Concussive Bolt") && log.contains("does nothing")
                            && log.contains("fewer than three artifacts"));
        }
    }

    // =========================================================================
    // MetalcraftReplacementEffect
    // (GalvanicBlast: MetalcraftReplacementEffect(DealDamageToAnyTargetEffect(2), DealDamageToAnyTargetEffect(4)))
    // =========================================================================

    @Nested
    @DisplayName("resolveMetalcraftReplacementEffect")
    class ResolveMetalcraftReplacementEffect {

        @Test
        @DisplayName("Resolves base effect when metalcraft is not met")
        void resolvesBaseEffectWhenMetalcraftNotMet() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Base effect: 2 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Resolves metalcraft effect when metalcraft is met")
        void resolvesMetalcraftEffectWhenMetalcraftMet() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            addArtifacts(player1, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Metalcraft effect: 4 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        }

        @Test
        @DisplayName("Re-checks metalcraft condition at resolution time")
        void rechecksConditionAtResolutionTime() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            addArtifacts(player1, 3);

            harness.castInstant(player1, 0, player2.getId());

            // Remove artifacts before resolution
            gd.playerBattlefields.get(player1.getId()).clear();

            harness.passBothPriorities();

            // Falls back to base effect: 2 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }
    }

    // =========================================================================
    // Metalcraft condition threshold
    // =========================================================================

    @Nested
    @DisplayName("metalcraftCondition")
    class MetalcraftCondition {

        @Test
        @DisplayName("Two artifacts do not meet metalcraft threshold")
        void twoArtifactsDoNotMeetThreshold() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            addArtifacts(player1, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Only 2 damage (base)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Exactly three artifacts meets metalcraft threshold")
        void exactlyThreeArtifactsMeetsThreshold() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            addArtifacts(player1, 3);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // 4 damage (metalcraft)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        }

        @Test
        @DisplayName("Artifact creatures count toward metalcraft")
        void artifactCreaturesCountForMetalcraft() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            // 2 non-creature artifacts + 1 artifact creature = 3 artifacts total
            addArtifacts(player1, 2);
            harness.addToBattlefield(player1, new Ornithopter());

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // 4 damage (metalcraft met via artifact creature)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        }

        @Test
        @DisplayName("More than three artifacts still meets metalcraft threshold")
        void moreThanThreeArtifactsMeetsThreshold() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);
            addArtifacts(player1, 5);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // 4 damage (metalcraft)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        }

        @Test
        @DisplayName("Zero artifacts does not meet metalcraft threshold")
        void zeroArtifactsDoesNotMeetThreshold() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Only 2 damage (base)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }
    }

    // =========================================================================
    // Multi-effect resolution
    // =========================================================================

    @Nested
    @DisplayName("multiEffectResolution")
    class MultiEffectResolution {

        @Test
        @DisplayName("All effects on a stack entry resolve in sequence")
        void allEffectsOnStackEntryResolveInSequence() {
            harness.setLife(player2, 20);
            Permanent creature = addReadyCreature(player2);

            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            addArtifacts(player1, 3);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            // First effect: 4 damage to target player
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
            // Second effect: creatures of target player can't block
            assertThat(creature.isCantBlockThisTurn()).isTrue();
        }

        @Test
        @DisplayName("Non-metalcraft effects still resolve when metalcraft conditional is skipped")
        void nonMetalcraftEffectsStillResolveWhenMetalcraftSkips() {
            harness.setLife(player2, 20);
            Permanent creature = addReadyCreature(player2);

            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            // No artifacts — metalcraft not met

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            // First effect (non-metalcraft) still fires: 4 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
            // Second effect (metalcraft conditional) is skipped
            assertThat(creature.isCantBlockThisTurn()).isFalse();
        }
    }

    // =========================================================================
    // Resolution state management
    // =========================================================================

    @Nested
    @DisplayName("resolutionStateManagement")
    class ResolutionStateManagement {

        @Test
        @DisplayName("Clears pendingEffectResolutionEntry after all effects resolve")
        void clearsPendingEffectResolutionEntryAfterResolution() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.pendingEffectResolutionEntry).isNull();
        }

        @Test
        @DisplayName("Clears pendingEffectResolutionIndex after all effects resolve")
        void clearsPendingEffectResolutionIndexAfterResolution() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new GalvanicBlast()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.pendingEffectResolutionIndex).isZero();
        }

        @Test
        @DisplayName("Clears pending state after multi-effect spell resolves")
        void clearsPendingStateAfterMultiEffectSpellResolves() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new ConcussiveBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            addArtifacts(player1, 3);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.pendingEffectResolutionEntry).isNull();
            assertThat(gd.pendingEffectResolutionIndex).isZero();
        }
    }
}
