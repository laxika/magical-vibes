package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.c.CullingDais;
import com.github.laxika.magicalvibes.cards.d.DoublingCube;
import com.github.laxika.magicalvibes.cards.d.DrossHopper;
import com.github.laxika.magicalvibes.cards.d.DuctCrawler;
import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.cards.g.GoldenUrn;
import com.github.laxika.magicalvibes.cards.g.Grindclock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrReservoir;
import com.github.laxika.magicalvibes.cards.n.NeurokReplica;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivatedAbilityExecutionServiceTest extends BaseCardTest {

    // =========================================================================
    // Mana ability — immediate resolution (no stack)
    // =========================================================================

    @Nested
    @DisplayName("mana ability — immediate resolution")
    class ManaAbilityImmediateResolution {

        @Test
        @DisplayName("Pain land: adds mana and deals damage without using the stack")
        void painLandAddsManaAndDealsDamage() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            harness.setLife(player1, 20);

            int idx = indexOf(player1, wastes);
            // Ability index 1 = "{T}: Add {W}. Adarkar Wastes deals 1 damage to you."
            harness.activateAbility(player1, idx, 1, null, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Pain land: colorless ability adds mana without damage")
        void painLandColorlessNoDamage() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            harness.setLife(player1, 20);

            int idx = indexOf(player1, wastes);
            // Ability index 0 = "{T}: Add {C}."
            harness.activateAbility(player1, idx, 0, null, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Pain land: blue ability adds blue mana and deals damage without stack")
        void painLandBlueAbility() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            harness.setLife(player1, 20);

            int idx = indexOf(player1, wastes);
            // Ability index 2 = "{T}: Add {U}. Adarkar Wastes deals 1 damage to you."
            harness.activateAbility(player1, idx, 2, null, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Doubling Cube: doubles all mana in pool immediately")
        void doublingCubeDoublesAllMana() {
            Permanent cube = addReadyPermanent(player1, new DoublingCube());
            // Add 3 colorless to pay the cost, plus extra mana to be doubled
            harness.addMana(player1, ManaColor.COLORLESS, 5);
            harness.addMana(player1, ManaColor.RED, 2);

            int idx = indexOf(player1, cube);
            harness.activateAbility(player1, idx, null, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            // 5 colorless - 3 cost = 2 remaining, doubled to 4
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(4);
            // 2 red doubled to 4
            assertThat(pool.get(ManaColor.RED)).isEqualTo(4);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Myr Reservoir: adds Myr-only colorless mana immediately")
        void myrReservoirAddsMyrOnlyMana() {
            Permanent reservoir = addReadyPermanent(player1, new MyrReservoir());

            int idx = indexOf(player1, reservoir);
            // Ability index 0 = "{T}: Add {C}{C}. Spend this mana only to cast Myr spells..."
            harness.activateAbility(player1, idx, 0, null, null);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            assertThat(pool.getMyrOnlyColorless()).isEqualTo(2);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Mana ability taps the permanent when tap cost is required")
        void manaAbilityTapsPermanent() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());

            int idx = indexOf(player1, wastes);
            harness.activateAbility(player1, idx, 0, null, null);

            assertThat(wastes.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Mana ability logs the activation")
        void manaAbilityLogs() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());

            int idx = indexOf(player1, wastes);
            harness.activateAbility(player1, idx, 0, null, null);

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("activates") && log.contains("Adarkar Wastes"));
        }
    }

    // =========================================================================
    // Self-targeting detection
    // =========================================================================

    @Nested
    @DisplayName("self-targeting detection")
    class SelfTargetingDetection {

        @Test
        @DisplayName("BoostSelfEffect auto-targets source permanent on the stack")
        void boostSelfAutoTargets() {
            Card creature = createCreatureWithBoostSelf();
            Permanent perm = addReadyPermanent(player1, creature);

            int idx = indexOf(player1, perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("RegenerateEffect (non-targeting) auto-targets source permanent")
        void regenerateAutoTargets() {
            Permanent mamba = addReadyPermanent(player1, new BlightMamba());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            int idx = indexOf(player1, mamba);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(mamba.getId());
        }

        @Test
        @DisplayName("AnimateSelfWithStatsEffect auto-targets source permanent")
        void animateSelfAutoTargets() {
            Permanent idol = addReadyPermanent(player1, new GlintHawkIdol());
            harness.addMana(player1, ManaColor.WHITE, 1);

            int idx = indexOf(player1, idol);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(idol.getId());
        }

        @Test
        @DisplayName("AnimateLandEffect auto-targets source permanent")
        void animateLandAutoTargets() {
            Permanent village = addReadyPermanent(player1, new TreetopVillage());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            int idx = indexOf(player1, village);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(village.getId());
        }

        @Test
        @DisplayName("PutChargeCounterOnSelfEffect auto-targets source permanent")
        void putChargeCounterAutoTargets() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());

            int idx = indexOf(player1, grindclock);
            // Ability index 0 = "{T}: Put a charge counter on Grindclock."
            harness.activateAbility(player1, idx, 0, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(grindclock.getId());
        }
    }

    // =========================================================================
    // Stack push — non-mana abilities
    // =========================================================================

    @Nested
    @DisplayName("stack push — non-mana abilities")
    class StackPush {

        @Test
        @DisplayName("Non-mana ability is pushed onto the stack")
        void nonManaAbilityPushedToStack() {
            Card creature = createCreatureWithBoostSelf();
            Permanent perm = addReadyPermanent(player1, creature);

            int idx = indexOf(player1, perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Stack entry has correct controller")
        void stackEntryHasCorrectController() {
            Card creature = createCreatureWithBoostSelf();
            Permanent perm = addReadyPermanent(player1, creature);

            int idx = indexOf(player1, perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Stack entry has correct source permanent ID")
        void stackEntryHasCorrectSourcePermanent() {
            Card creature = createCreatureWithBoostSelf();
            Permanent perm = addReadyPermanent(player1, creature);

            int idx = indexOf(player1, perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Stack entry description includes card name")
        void stackEntryDescriptionIncludesCardName() {
            Permanent mamba = addReadyPermanent(player1, new BlightMamba());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            int idx = indexOf(player1, mamba);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack.getFirst().getDescription()).contains("Blight Mamba");
            assertThat(gd.stack.getFirst().getDescription()).contains("ability");
        }

        @Test
        @DisplayName("Tap ability taps the permanent when pushed to stack")
        void tapAbilityTapsPermanent() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());

            int idx = indexOf(player1, grindclock);
            harness.activateAbility(player1, idx, 0, null, null);

            assertThat(grindclock.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Non-tap ability does not tap the permanent")
        void nonTapAbilityDoesNotTap() {
            Permanent mamba = addReadyPermanent(player1, new BlightMamba());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            int idx = indexOf(player1, mamba);
            harness.activateAbility(player1, idx, null, null);

            assertThat(mamba.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Activation logs the action")
        void activationLogs() {
            Permanent mamba = addReadyPermanent(player1, new BlightMamba());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            int idx = indexOf(player1, mamba);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("activates") && log.contains("Blight Mamba"));
        }
    }

    // =========================================================================
    // Effect snapshotting — cost filtering and baked-in values
    // =========================================================================

    @Nested
    @DisplayName("effect snapshotting")
    class EffectSnapshotting {

        @Test
        @DisplayName("SacrificeSelfCost is filtered out of stack entry effects")
        void sacrificeSelfCostFilteredFromStack() {
            Permanent replica = addReadyPermanent(player1, new NeurokReplica());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
            Permanent target = addReadyPermanent(player2, new GrizzlyBears());

            int idx = indexOf(player1, replica);
            harness.activateAbility(player1, idx, null, target.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .noneMatch(e -> e instanceof SacrificeSelfCost);
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .anyMatch(e -> e instanceof ReturnTargetPermanentToHandEffect);
        }

        @Test
        @DisplayName("CantBlockSourceEffect gets source permanent ID baked in")
        void cantBlockSourceGetsIdBakedIn() {
            Permanent crawler = addReadyPermanent(player1, new DuctCrawler());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            Permanent target = addReadyPermanent(player2, new GrizzlyBears());

            int idx = indexOf(player1, crawler);
            harness.activateAbility(player1, idx, null, target.getId());

            assertThat(gd.stack).hasSize(1);
            CantBlockSourceEffect snapshot = gd.stack.getFirst().getEffectsToResolve().stream()
                    .filter(e -> e instanceof CantBlockSourceEffect)
                    .map(e -> (CantBlockSourceEffect) e)
                    .findFirst().orElseThrow();
            assertThat(snapshot.sourcePermanentId()).isEqualTo(crawler.getId());
        }
    }

    // =========================================================================
    // Self-sacrifice flow (SacrificeSelfCost)
    // =========================================================================

    @Nested
    @DisplayName("self-sacrifice flow")
    class SelfSacrificeFlow {

        @Test
        @DisplayName("SacrificeSelfCost removes permanent from battlefield")
        void sacrificeRemovesPermanent() {
            Permanent replica = addReadyPermanent(player1, new NeurokReplica());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
            Permanent target = addReadyPermanent(player2, new GrizzlyBears());

            int idx = indexOf(player1, replica);
            harness.activateAbility(player1, idx, null, target.getId());

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Neurok Replica"));
        }

        @Test
        @DisplayName("SacrificeSelfCost puts card in graveyard")
        void sacrificePutsCardInGraveyard() {
            Permanent replica = addReadyPermanent(player1, new NeurokReplica());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
            Permanent target = addReadyPermanent(player2, new GrizzlyBears());

            int idx = indexOf(player1, replica);
            harness.activateAbility(player1, idx, null, target.getId());

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Neurok Replica"));
        }

        @Test
        @DisplayName("SacrificeSelfCost still pushes ability on stack")
        void sacrificeStillPushesAbilityOnStack() {
            Permanent replica = addReadyPermanent(player1, new NeurokReplica());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
            Permanent target = addReadyPermanent(player2, new GrizzlyBears());

            int idx = indexOf(player1, replica);
            harness.activateAbility(player1, idx, null, target.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }
    }

    // =========================================================================
    // Charge counter snapshotting before sacrifice
    // =========================================================================

    @Nested
    @DisplayName("charge counter snapshotting")
    class ChargeCounterSnapshotting {

        @Test
        @DisplayName("DrawCardsEqualToChargeCountersOnSourceEffect snapshots counters as xValue")
        void drawCardsSnapshotsChargeCounters() {
            Permanent dais = addReadyPermanent(player1, new CullingDais());
            dais.setChargeCounters(3);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            int idx = indexOf(player1, dais);
            // Ability index 1 = "{1}, Sacrifice Culling Dais: Draw cards equal to charge counters."
            harness.activateAbility(player1, idx, 1, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getXValue()).isEqualTo(3);
            // Dais should be sacrificed
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Culling Dais"));
        }

        @Test
        @DisplayName("GainLifeEqualToChargeCountersOnSourceEffect snapshots counters as xValue")
        void gainLifeSnapshotsChargeCounters() {
            Permanent urn = addReadyPermanent(player1, new GoldenUrn());
            urn.setChargeCounters(5);

            int idx = indexOf(player1, urn);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getXValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("MillTargetPlayerByChargeCountersEffect snapshots counters as xValue")
        void millSnapshotsChargeCounters() {
            Permanent clock = addReadyPermanent(player1, new Grindclock());
            clock.setChargeCounters(4);

            int idx = indexOf(player1, clock);
            // Ability index 1 = "{T}: Target player mills X cards..."
            harness.activateAbility(player1, idx, 1, null, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getXValue()).isEqualTo(4);
        }

        @Test
        @DisplayName("Charge counters snapshot to 0 when permanent has no counters")
        void snapshotZeroCounters() {
            Permanent clock = addReadyPermanent(player1, new Grindclock());
            // No charge counters

            int idx = indexOf(player1, clock);
            harness.activateAbility(player1, idx, 1, null, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getXValue()).isEqualTo(0);
        }
    }

    // =========================================================================
    // Priority clearing after activation
    // =========================================================================

    @Nested
    @DisplayName("post-activation state")
    class PostActivationState {

        @Test
        @DisplayName("Priority is cleared after mana ability resolution")
        void priorityClearedAfterManaAbility() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            gd.priorityPassedBy.add(player1.getId());

            int idx = indexOf(player1, wastes);
            harness.activateAbility(player1, idx, 0, null, null);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Priority is cleared after non-mana ability activation")
        void priorityClearedAfterStackPush() {
            Permanent mamba = addReadyPermanent(player1, new BlightMamba());
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);
            gd.priorityPassedBy.add(player1.getId());

            int idx = indexOf(player1, mamba);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.priorityPassedBy).isEmpty();
        }
    }

    // =========================================================================
    // Pain land damage prevention interaction
    // =========================================================================

    @Nested
    @DisplayName("pain land — damage prevention")
    class PainLandDamagePrevention {

        @Test
        @DisplayName("Pain land damage is prevented when source damage prevention is active")
        void painLandDamagePreventedBySourcePrevention() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            harness.setLife(player1, 20);
            // Set up source-specific damage prevention for this permanent
            gd.playerSourceDamagePreventionIds
                    .computeIfAbsent(player1.getId(), k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(wastes.getId());

            int idx = indexOf(player1, wastes);
            harness.activateAbility(player1, idx, 1, null, null);

            // Mana should be added
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
            // Damage should be prevented
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Pain land damage is reduced by player damage prevention shield")
        void painLandDamageReducedByShield() {
            Permanent wastes = addReadyPermanent(player1, new AdarkarWastes());
            harness.setLife(player1, 20);
            gd.playerDamagePreventionShields.put(player1.getId(), 1);

            int idx = indexOf(player1, wastes);
            harness.activateAbility(player1, idx, 1, null, null);

            // Mana should be added
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
            // 1 damage fully absorbed by shield
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }
    }

    // =========================================================================
    // Non-targeting sacrifice flag
    // =========================================================================

    @Nested
    @DisplayName("non-targeting sacrifice creature cost")
    class NonTargetingSacrificeCreatureCost {

        @Test
        @DisplayName("Sacrifice creature cost ability sets nonTargeting on stack entry")
        void sacrificeCreatureCostSetsNonTargeting() {
            Permanent hopper = addReadyPermanent(player1, new DrossHopper());
            Permanent sacrificeTarget = addReadyPermanent(player1, new GrizzlyBears());

            int idx = indexOf(player1, hopper);
            harness.activateAbility(player1, idx, null, sacrificeTarget.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Card createCreatureWithBoostSelf() {
        Card card = new Card();
        card.setName("Test Boost Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.addActivatedAbility(new com.github.laxika.magicalvibes.model.ActivatedAbility(
                false, null, java.util.List.of(new BoostSelfEffect(1, 1)), "Boost self"
        ));
        return card;
    }
}
