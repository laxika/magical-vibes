package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LuxCannon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SeismicAssault;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbilityActivationServiceTest extends BaseCardTest {

    // =========================================================================
    // tapPermanent
    // =========================================================================

    @Nested
    @DisplayName("tapPermanent")
    class TapPermanent {

        @Test
        @DisplayName("Tapping a land awards the correct mana")
        void tappingLandAwardsMana() {
            harness.addToBattlefield(player1, new Island());

            Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Island"))
                    .findFirst().orElseThrow();
            island.setSummoningSick(false);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            int blueBefore = pool.get(ManaColor.BLUE);

            gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
            assertThat(island.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot tap an already tapped permanent")
        void cannotTapAlreadyTapped() {
            harness.addToBattlefield(player1, new Island());

            Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Island"))
                    .findFirst().orElseThrow();
            island.setSummoningSick(false);
            island.tap();

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, idx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        @Test
        @DisplayName("Cannot tap a permanent with no tap effects")
        void cannotTapWithNoTapEffects() {
            harness.addToBattlefield(player1, new GrizzlyBears());

            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            bears.setSummoningSick(false);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, idx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no tap effects");
        }

        @Test
        @DisplayName("Summoning sick creature with tap-for-mana cannot be tapped")
        void summoningSickCreatureCannotTap() {
            harness.addToBattlefield(player1, new LlanowarElves());

            Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                    .findFirst().orElseThrow();
            // summoningSick is true by default

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(elves);
            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, idx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("summoning sickness");
        }

        @Test
        @DisplayName("Non-summoning-sick creature with tap-for-mana can be tapped")
        void nonSummoningSickCreatureCanTap() {
            harness.addToBattlefield(player1, new LlanowarElves());

            Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                    .findFirst().orElseThrow();
            elves.setSummoningSick(false);

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            int greenBefore = pool.get(ManaColor.GREEN);

            gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));

            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
        }

        @Test
        @DisplayName("Summoning sick land can be tapped (lands are not creatures)")
        void summoningSickLandCanBeTapped() {
            harness.addToBattlefield(player1, new Island());

            Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Island"))
                    .findFirst().orElseThrow();
            // summoningSick is true by default for all permanents, but lands should still be tappable

            ManaPool pool = gd.playerManaPools.get(player1.getId());
            int blueBefore = pool.get(ManaColor.BLUE);

            gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        }

        @Test
        @DisplayName("Arrest blocks creature tap abilities")
        void arrestBlocksCreatureTapAbilities() {
            Permanent elves = addReadyCreature(player1, new LlanowarElves());

            // Attach Arrest aura to the creature
            attachArrestAura(elves);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(elves);
            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, idx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated");
        }

        @Test
        @DisplayName("Invalid permanent index throws")
        void invalidPermanentIndexThrows() {
            assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 999))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent index");
        }

        @Test
        @DisplayName("Tapping a land logs the action")
        void tappingLandLogsAction() {
            harness.addToBattlefield(player1, new Island());

            Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Island"))
                    .findFirst().orElseThrow();
            island.setSummoningSick(false);

            gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(island));

            assertThat(gd.gameLog).anyMatch(log -> log.contains("taps Island"));
        }
    }

    // =========================================================================
    // sacrificePermanent
    // =========================================================================

    @Nested
    @DisplayName("sacrificePermanent")
    class SacrificePermanent {

        @Test
        @DisplayName("Sacrificing puts ability on the stack and card in graveyard")
        void sacrificePutsAbilityOnStack() {
            Permanent aura = addReadyPermanent(player1, new AuraOfSilence());
            harness.addToBattlefield(player2, new LuxCannon());
            UUID targetId = harness.getPermanentId(player2, "Lux Cannon");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
            harness.sacrificePermanent(player1, idx, targetId);

            // Card should be in graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Aura of Silence"));

            // Ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Cannot sacrifice permanent with no ON_SACRIFICE effects")
        void cannotSacrificeWithoutEffects() {
            Permanent bears = addReadyPermanent(player1, new GrizzlyBears());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
            assertThatThrownBy(() -> harness.sacrificePermanent(player1, idx, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no sacrifice abilities");
        }

        @Test
        @DisplayName("Pithing Needle blocks sacrifice abilities")
        void pithingNeedleBlocksSacrifice() {
            Permanent aura = addReadyPermanent(player1, new AuraOfSilence());
            harness.addToBattlefield(player2, new LuxCannon());
            UUID targetId = harness.getPermanentId(player2, "Lux Cannon");

            // Add Pithing Needle naming Aura of Silence
            addPithingNeedleNaming(player2, "Aura of Silence");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
            assertThatThrownBy(() -> harness.sacrificePermanent(player1, idx, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated")
                    .hasMessageContaining("Pithing Needle");
        }

        @Test
        @DisplayName("Invalid permanent index throws")
        void invalidIndexThrows() {
            assertThatThrownBy(() -> harness.sacrificePermanent(player1, 999, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent index");
        }

        @Test
        @DisplayName("Sacrifice logs the action")
        void sacrificeLogsAction() {
            Permanent aura = addReadyPermanent(player1, new AuraOfSilence());
            harness.addToBattlefield(player2, new LuxCannon());
            UUID targetId = harness.getPermanentId(player2, "Lux Cannon");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);
            harness.sacrificePermanent(player1, idx, targetId);

            assertThat(gd.gameLog).anyMatch(log -> log.contains("sacrifices Aura of Silence"));
        }
    }

    // =========================================================================
    // activateAbility — tap/mana cost basics
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — tap and mana costs")
    class ActivateAbilityTapAndMana {

        @Test
        @DisplayName("Tap ability: already tapped permanent throws")
        void tapAbilityAlreadyTappedThrows() {
            harness.addToBattlefield(player1, new LuxCannon());

            Permanent cannon = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Lux Cannon"))
                    .findFirst().orElseThrow();
            cannon.setSummoningSick(false);
            cannon.tap();

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        @Test
        @DisplayName("Tap ability: summoning sick creature throws")
        void tapAbilitySummoningSickCreatureThrows() {
            Card creature = createCreatureWithTapAbility();
            gd.playerBattlefields.get(player1.getId()).add(new Permanent(creature));
            // summoningSick is true by default

            int idx = gd.playerBattlefields.get(player1.getId()).size() - 1;
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("summoning sickness");
        }

        @Test
        @DisplayName("Mana cost: insufficient mana throws")
        void insufficientManaThrows() {
            Card artifact = createArtifactWithManaAbility("{2}");
            Permanent perm = addReadyPermanent(player1, artifact);
            // Do NOT add any mana

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        @Test
        @DisplayName("Mana cost: sufficient mana succeeds and is deducted")
        void sufficientManaSucceeds() {
            Card artifact = createArtifactWithManaAbility("{1}");
            Permanent perm = addReadyPermanent(player1, artifact);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("No activated abilities on permanent throws")
        void noActivatedAbilitiesThrows() {
            Permanent bears = addReadyPermanent(player1, new GrizzlyBears());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no activated ability");
        }

        @Test
        @DisplayName("Invalid ability index throws")
        void invalidAbilityIndexThrows() {
            Permanent cannon = addReadyPermanent(player1, new LuxCannon());

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 99, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid ability index");
        }
    }

    // =========================================================================
    // activateAbility — timing restrictions
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — timing restrictions")
    class ActivateAbilityTimingRestrictions {

        @Test
        @DisplayName("SORCERY_SPEED: cannot activate on opponent's turn")
        void sorcerySpeedCannotActivateOnOpponentTurn() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sorcery speed");
        }

        @Test
        @DisplayName("SORCERY_SPEED: cannot activate outside main phase")
        void sorcerySpeedCannotActivateOutsideMainPhase() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("main phase");
        }

        @Test
        @DisplayName("SORCERY_SPEED: cannot activate with non-empty stack")
        void sorcerySpeedCannotActivateWithStack() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // Put something on the stack
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castCreature(player1, 0);
            assertThat(gd.stack).isNotEmpty();

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stack is empty");
        }

        @Test
        @DisplayName("SORCERY_SPEED: succeeds during own main phase with empty stack")
        void sorcerySpeedSucceedsDuringMainPhase() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("ONLY_DURING_YOUR_UPKEEP: wrong step throws")
        void upkeepOnlyWrongStepThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("ONLY_DURING_YOUR_UPKEEP: opponent's upkeep throws")
        void upkeepOnlyOpponentTurnThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP);
            Permanent perm = addReadyPermanent(player1, card);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.UPKEEP);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("METALCRAFT: fewer than 3 artifacts throws")
        void metalcraftInsufficientArtifactsThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.METALCRAFT);
            Permanent perm = addReadyPermanent(player1, card);
            // Only one artifact on the field (the card itself)

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("three or more artifacts");
        }

        @Test
        @DisplayName("METALCRAFT: exactly 3 artifacts succeeds")
        void metalcraftThreeArtifactsSucceeds() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.METALCRAFT);
            Permanent perm = addReadyPermanent(player1, card);
            // Add two more artifacts for metalcraft (card itself is one)
            addReadyPermanent(player1, createGenericArtifact("Artifact A"));
            addReadyPermanent(player1, createGenericArtifact("Artifact B"));

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("POWER_4_OR_GREATER: low power creature throws")
        void power4OrGreaterLowPowerThrows() {
            Card card = createCreatureWithTimingRestriction(ActivationTimingRestriction.POWER_4_OR_GREATER, 2, 2);
            Permanent perm = addReadyPermanent(player1, card);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("power is 4 or greater");
        }

        @Test
        @DisplayName("POWER_4_OR_GREATER: high power creature succeeds")
        void power4OrGreaterHighPowerSucceeds() {
            Card card = createCreatureWithTimingRestriction(ActivationTimingRestriction.POWER_4_OR_GREATER, 5, 5);
            Permanent perm = addReadyPermanent(player1, card);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.stack).hasSize(1);
        }
    }

    // =========================================================================
    // activateAbility — loyalty abilities
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — loyalty abilities")
    class ActivateAbilityLoyalty {

        @Test
        @DisplayName("Cannot activate loyalty ability on opponent's turn")
        void cannotActivateLoyaltyOnOpponentTurn() {
            Permanent koth = addReadyKoth(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Loyalty abilities");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability outside main phase")
        void cannotActivateLoyaltyOutsideMainPhase() {
            Permanent koth = addReadyKoth(player1);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("main phase");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability with non-empty stack")
        void cannotActivateLoyaltyWithStack() {
            Permanent koth = addReadyKoth(player1);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // Put something on the stack
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castCreature(player1, 0);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stack is empty");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability twice per turn")
        void cannotActivateLoyaltyTwicePerTurn() {
            Permanent koth = addReadyKoth(player1);
            // Koth +1 needs a Mountain target
            harness.addToBattlefield(player1, new Mountain());
            Permanent mountain1 = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mountain"))
                    .findFirst().orElseThrow();
            mountain1.setSummoningSick(false);
            harness.addToBattlefield(player1, new Mountain());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int kothIdx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            UUID mountainId = mountain1.getId();
            harness.activateAbility(player1, kothIdx, 0, null, mountainId);
            harness.passBothPriorities();

            // Second activation should fail
            UUID mountain2Id = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mountain") && !p.getId().equals(mountainId))
                    .findFirst().orElseThrow().getId();

            int kothIdx2 = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            assertThatThrownBy(() -> harness.activateAbility(player1, kothIdx2, 0, null, mountain2Id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("one loyalty ability");
        }

        @Test
        @DisplayName("Cannot activate negative loyalty ability with insufficient counters")
        void cannotActivateNegativeLoyaltyWithInsufficientCounters() {
            Permanent koth = addReadyKoth(player1);
            koth.setLoyaltyCounters(1); // Koth's -2 ability needs at least 2

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 1, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty counters");
        }

        @Test
        @DisplayName("Positive loyalty cost adds counters")
        void positiveLoyaltyCostAddsCounters() {
            Permanent koth = addReadyKoth(player1);
            harness.addToBattlefield(player1, new Mountain());
            Permanent mountain = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mountain"))
                    .findFirst().orElseThrow();
            mountain.setSummoningSick(false);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            int loyaltyBefore = koth.getLoyaltyCounters();
            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(koth);
            harness.activateAbility(player1, idx, 0, null, mountain.getId());

            // +1 loyalty cost
            assertThat(koth.getLoyaltyCounters()).isEqualTo(loyaltyBefore + 1);
        }
    }

    // =========================================================================
    // activateAbility — Pithing Needle / Arrest
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — Pithing Needle and Arrest")
    class ActivateAbilityBlockers {

        @Test
        @DisplayName("Pithing Needle blocks non-mana activated abilities")
        void pithingNeedleBlocksNonManaAbilities() {
            Permanent cannon = addReadyPermanent(player1, new LuxCannon());
            cannon.setChargeCounters(3);

            addPithingNeedleNaming(player2, "Lux Cannon");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated")
                    .hasMessageContaining("Pithing Needle");
        }

        @Test
        @DisplayName("Pithing Needle does not block mana abilities")
        void pithingNeedleDoesNotBlockManaAbilities() {
            harness.addToBattlefield(player1, new Island());
            Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Island"))
                    .findFirst().orElseThrow();
            island.setSummoningSick(false);

            addPithingNeedleNaming(player2, "Island");

            // Tapping Island for mana should still work — ON_TAP is tapped via tapPermanent, not activateAbility
            ManaPool pool = gd.playerManaPools.get(player1.getId());
            int blueBefore = pool.get(ManaColor.BLUE);
            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
            gs.tapPermanent(gd, player1, idx);
            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        }

        @Test
        @DisplayName("Pithing Needle naming a different card does not block")
        void pithingNeedleDifferentNameDoesNotBlock() {
            Permanent cannon = addReadyPermanent(player1, new LuxCannon());

            addPithingNeedleNaming(player2, "Grizzly Bears");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            harness.activateAbility(player1, idx, null, null);
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Arrest blocks all activated abilities of enchanted creature")
        void arrestBlocksAllAbilities() {
            NantukoHusk huskCard = new NantukoHusk();
            Permanent husk = new Permanent(huskCard);
            husk.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(husk);

            attachArrestAura(husk);

            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(husk);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, bearsId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated");
        }
    }

    // =========================================================================
    // activateAbility — counter costs
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — counter costs")
    class ActivateAbilityCounterCosts {

        @Test
        @DisplayName("RemoveChargeCountersFromSourceCost: insufficient counters throws")
        void removeChargeCountersInsufficientThrows() {
            Permanent cannon = addReadyPermanent(player1, new LuxCannon());
            cannon.setChargeCounters(2);

            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, 1, null, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough charge counters");
        }

        @Test
        @DisplayName("RemoveChargeCountersFromSourceCost: removes exactly the required count")
        void removeChargeCountersExactCount() {
            Permanent cannon = addReadyPermanent(player1, new LuxCannon());
            cannon.setChargeCounters(5);

            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(cannon);
            harness.activateAbility(player1, idx, 1, null, targetId);

            assertThat(cannon.getChargeCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: no counters throws")
        void removeCounterNoCountersThrows() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1, card);
            // No counters on the permanent

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No counters to remove");
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: prefers removing -1/-1 counter first")
        void removeCounterPrefersMinusOneMinusOne() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1, card);
            perm.setPlusOnePlusOneCounters(2);
            perm.setMinusOneMinusOneCounters(1);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(perm.getMinusOneMinusOneCounters()).isEqualTo(0);
            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: removes +1/+1 if no -1/-1 counters")
        void removeCounterFallsToPlusOnePlusOne() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1, card);
            perm.setPlusOnePlusOneCounters(3);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);

            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(2);
        }
    }

    // =========================================================================
    // activateAbility — discard card type cost
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — discard card type cost")
    class ActivateAbilityDiscardCost {

        @Test
        @DisplayName("Triggers discard-cost choice when land card in hand")
        void triggersDiscardChoice() {
            addReadySeismicAssault(player1);
            harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

            harness.activateAbility(player1, 0, null, player2.getId());

            assertThat(gd.interaction.awaitingInputType())
                    .isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No valid discard card throws")
        void noValidDiscardCardThrows() {
            addReadySeismicAssault(player1);
            harness.setHand(player1, List.of(new GrizzlyBears()));

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must discard a land card");
        }

        @Test
        @DisplayName("Completing discard choice puts ability on stack")
        void completingDiscardChoicePutsAbilityOnStack() {
            addReadySeismicAssault(player1);
            harness.setHand(player1, List.of(new Mountain()));

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.handleCardChosen(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Mountain"));
        }

        @Test
        @DisplayName("Choosing invalid card index throws")
        void choosingInvalidCardIndexThrows() {
            addReadySeismicAssault(player1);
            harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

            harness.activateAbility(player1, 0, null, player2.getId());

            assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid card index");
        }

        @Test
        @DisplayName("Only land cards are valid for land discard cost")
        void onlyLandCardsAreValid() {
            addReadySeismicAssault(player1);
            harness.setHand(player1, List.of(new GrizzlyBears(), new Plains(), new Mountain()));

            harness.activateAbility(player1, 0, null, player2.getId());

            // Indices 1 and 2 should be valid (both lands), index 0 is a creature
            assertThat(gd.interaction.awaitingCardChoiceValidIndices()).containsExactlyInAnyOrder(1, 2);
        }
    }

    // =========================================================================
    // activateAbility — sacrifice creature cost
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — sacrifice creature cost")
    class ActivateAbilitySacrificeCreature {

        @Test
        @DisplayName("Sacrifice creature cost: creature goes to graveyard and ability goes on stack")
        void sacrificeCreatureCostWorks() {
            addNantukoHuskReady(player1);
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.activateAbility(player1, 0, null, bearsId);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Must choose a creature to sacrifice")
        void mustChooseCreatureToSacrifice() {
            addNantukoHuskReady(player1);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must choose a creature to sacrifice");
        }

        @Test
        @DisplayName("Cannot sacrifice a non-creature")
        void cannotSacrificeNonCreature() {
            addNantukoHuskReady(player1);
            Permanent artifact = addReadyPermanent(player1, createGenericArtifact("Test Artifact"));

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must sacrifice a creature");
        }

        @Test
        @DisplayName("Cannot sacrifice opponent's creature")
        void cannotSacrificeOpponentCreature() {
            addNantukoHuskReady(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must sacrifice a creature you control");
        }
    }

    // =========================================================================
    // activateAbility — sacrifice artifact cost
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — sacrifice artifact cost")
    class ActivateAbilitySacrificeArtifact {

        @Test
        @DisplayName("Auto-selects single artifact for sacrifice")
        void autoSelectsSingleArtifact() {
            Card card = createCardWithSacrificeArtifactAbility();
            Permanent source = addReadyPermanent(player1, card);
            Permanent target = addReadyPermanent(player1, createGenericArtifact("Sacrifice Target"));

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
            harness.activateAbility(player1, idx, null, null);

            // Single artifact should be auto-sacrificed, no prompt
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Sacrifice Target"));
        }

        @Test
        @DisplayName("No artifact to sacrifice throws")
        void noArtifactToSacrificeThrows() {
            Card card = createCardWithSacrificeArtifactAbility();
            addReadyPermanent(player1, card);
            // Source is an enchantment, not an artifact — no artifacts to sacrifice

            int idx = gd.playerBattlefields.get(player1.getId()).size() - 1;
            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No artifact to sacrifice");
        }

        @Test
        @DisplayName("Multiple artifacts prompt for choice")
        void multipleArtifactsPromptChoice() {
            Card card = createCardWithSacrificeArtifactAbility();
            Permanent source = addReadyPermanent(player1, card);
            addReadyPermanent(player1, createGenericArtifact("Artifact A"));
            addReadyPermanent(player1, createGenericArtifact("Artifact B"));

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
            harness.activateAbility(player1, idx, null, null);

            assertThat(gd.interaction.awaitingInputType())
                    .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        }
    }

    // =========================================================================
    // activateAbility — per-turn activation limit
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — activation limits")
    class ActivateAbilityLimits {

        @Test
        @DisplayName("Activation limit per turn blocks excess activations")
        void activationLimitBlocksExcess() {
            Card card = createArtifactWithLimitedAbility(1);
            Permanent perm = addReadyPermanent(player1, card);

            int idx = gd.playerBattlefields.get(player1.getId()).indexOf(perm);
            harness.activateAbility(player1, idx, null, null);
            harness.passBothPriorities();

            assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no more than 1 times each turn");
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

    private Permanent addReadyCreature(Player player, Card card) {
        return addReadyPermanent(player, card);
    }

    private Permanent addNantukoHuskReady(Player player) {
        return addReadyPermanent(player, new NantukoHusk());
    }

    private Permanent addReadyKoth(Player player) {
        KothOfTheHammer card = new KothOfTheHammer();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadySeismicAssault(Player player) {
        return addReadyPermanent(player, new SeismicAssault());
    }

    private void attachArrestAura(Permanent target) {
        Card aura = new Card();
        aura.setName("Arrest");
        aura.setType(CardType.ENCHANTMENT);
        aura.setManaCost("{2}{W}");
        aura.setColor(CardColor.WHITE);
        aura.addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
        aura.addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());

        Permanent auraPerm = new Permanent(aura);
        auraPerm.setSummoningSick(false);
        auraPerm.setAttachedTo(target.getId());
        // Find which player controls the target and add the aura to their battlefield
        for (UUID pid : gd.playerIds) {
            if (gd.playerBattlefields.get(pid).contains(target)) {
                gd.playerBattlefields.get(pid).add(auraPerm);
                break;
            }
        }
    }

    private void addPithingNeedleNaming(Player player, String cardName) {
        PithingNeedle needleCard = new PithingNeedle();
        Permanent needle = new Permanent(needleCard);
        needle.setSummoningSick(false);
        needle.setChosenName(cardName);
        gd.playerBattlefields.get(player.getId()).add(needle);
    }

    private Card createCreatureWithTapAbility() {
        Card card = new Card();
        card.setName("Test Tap Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new BoostSelfEffect(1, 1)), "Tap to boost"
        ));
        return card;
    }

    private Card createArtifactWithManaAbility(String manaCost) {
        Card card = new Card();
        card.setName("Test Mana Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, manaCost, List.of(new PutChargeCounterOnSelfEffect()), "Pay mana to add counter"
        ));
        return card;
    }

    private Card createArtifactWithTimingRestriction(ActivationTimingRestriction restriction) {
        Card card = new Card();
        card.setName("Test Timing Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new PutChargeCounterOnSelfEffect()),
                "Test ability", restriction
        ));
        return card;
    }

    private Card createCreatureWithTimingRestriction(ActivationTimingRestriction restriction, int power, int toughness) {
        Card card = new Card();
        card.setName("Test Timing Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new BoostSelfEffect(1, 1)),
                "Test ability", null, null, null, restriction
        ));
        return card;
    }

    private Card createGenericArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        return card;
    }

    private Card createArtifactWithRemoveCounterAbility() {
        Card card = new Card();
        card.setName("Test Counter Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new RemoveCounterFromSourceCost(), new PutChargeCounterOnSelfEffect()),
                "Remove counter, add charge counter"
        ));
        return card;
    }

    private Card createCardWithSacrificeArtifactAbility() {
        Card card = new Card();
        card.setName("Test Sac Artifact Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new SacrificeArtifactCost(), new PutChargeCounterOnSelfEffect()),
                "Sacrifice an artifact: put a charge counter"
        ));
        return card;
    }

    private Card createArtifactWithLimitedAbility(int maxPerTurn) {
        Card card = new Card();
        card.setName("Test Limited Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null, List.of(new PutChargeCounterOnSelfEffect()),
                "Limited ability", maxPerTurn
        ));
        return card;
    }
}
