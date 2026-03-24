package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbilityActivationServiceTest {

    private static final GameQueryService.StaticBonus EMPTY_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(), false, false, false, Set.of(), false, 0, 0, false);

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private TargetLegalityService targetLegalityService;
    @Mock private ActivatedAbilityExecutionService activatedAbilityExecutionService;
    @Mock private PlayerInputService playerInputService;
    @Mock private SessionManager sessionManager;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private ExileService exileService;

    @InjectMocks
    private AbilityActivationService service;

    private GameData gameData;
    private Player player1;
    private Player player2;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player1 = new Player(player1Id, "Player1");
        player2 = new Player(player2Id, "Player2");

        gameData = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gameData.playerIds.add(player1Id);
        gameData.playerIds.add(player2Id);
        gameData.orderedPlayerIds.add(player1Id);
        gameData.orderedPlayerIds.add(player2Id);
        gameData.playerBattlefields.put(player1Id, new ArrayList<>());
        gameData.playerBattlefields.put(player2Id, new ArrayList<>());
        gameData.playerManaPools.put(player1Id, new ManaPool());
        gameData.playerManaPools.put(player2Id, new ManaPool());
        gameData.playerHands.put(player1Id, new ArrayList<>());
        gameData.playerHands.put(player2Id, new ArrayList<>());
        gameData.playerGraveyards.put(player1Id, new ArrayList<>());
        gameData.playerGraveyards.put(player2Id, new ArrayList<>());
        gameData.playerIdToName.put(player1Id, "Player1");
        gameData.playerIdToName.put(player2Id, "Player2");
        gameData.activePlayerId = player1Id;
        gameData.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    // =========================================================================
    // tapPermanent
    // =========================================================================

    @Nested
    @DisplayName("tapPermanent")
    class TapPermanent {

        @Test
        @DisplayName("Tapping a land awards the correct mana")
        void tappingLandAwardsMana() {
            Card island = createLandWithManaAbility("Island", ManaColor.BLUE);
            Permanent perm = addReadyPermanent(player1Id, island);

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(false);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            int blueBefore = pool.get(ManaColor.BLUE);

            service.tapPermanent(gameData, player1, 0);

            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
            assertThat(perm.isTapped()).isTrue();
            verify(triggerCollectionService).checkLandTapTriggers(gameData, player1Id, perm.getId());
            verify(triggerCollectionService).checkEnchantedPermanentTapTriggers(gameData, perm);
            verify(gameBroadcastService).broadcastGameState(gameData);
        }

        @Test
        @DisplayName("Cannot tap an already tapped permanent")
        void cannotTapAlreadyTapped() {
            Card island = createLandWithManaAbility("Island", ManaColor.BLUE);
            Permanent perm = addReadyPermanent(player1Id, island);
            perm.tap();

            assertThatThrownBy(() -> service.tapPermanent(gameData, player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        @Test
        @DisplayName("Cannot tap a permanent with no tap effects")
        void cannotTapWithNoTapEffects() {
            Card card = createCreatureCard("Grizzly Bears", 2, 2);
            Permanent perm = addReadyPermanent(player1Id, card);

            assertThatThrownBy(() -> service.tapPermanent(gameData, player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no tap effects");
        }

        @Test
        @DisplayName("Summoning sick creature with tap-for-mana cannot be tapped")
        void summoningSickCreatureCannotTap() {
            Card elves = createCreatureWithTapAbility("Llanowar Elves", ManaColor.GREEN);
            Permanent perm = new Permanent(elves);
            // summoningSick is true by default
            gameData.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)).thenReturn(false);

            assertThatThrownBy(() -> service.tapPermanent(gameData, player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("summoning sickness");
        }

        @Test
        @DisplayName("Non-summoning-sick creature with tap-for-mana can be tapped")
        void nonSummoningSickCreatureCanTap() {
            Card elves = createCreatureWithTapAbility("Llanowar Elves", ManaColor.GREEN);
            Permanent perm = addReadyPermanent(player1Id, elves);

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(true);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            int greenBefore = pool.get(ManaColor.GREEN);

            service.tapPermanent(gameData, player1, 0);

            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
        }

        @Test
        @DisplayName("Summoning sick land can be tapped (lands are not creatures)")
        void summoningSickLandCanBeTapped() {
            Card island = createLandWithManaAbility("Island", ManaColor.BLUE);
            Permanent perm = new Permanent(island);
            // summoningSick is true by default for all permanents, but lands should still be tappable
            gameData.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(false);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            int blueBefore = pool.get(ManaColor.BLUE);

            service.tapPermanent(gameData, player1, 0);

            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        }

        @Test
        @DisplayName("Arrest blocks creature tap abilities")
        void arrestBlocksCreatureTapAbilities() {
            Card elves = createCreatureWithTapAbility("Llanowar Elves", ManaColor.GREEN);
            Permanent perm = addReadyPermanent(player1Id, elves);

            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.tapPermanent(gameData, player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated");
        }

        @Test
        @DisplayName("Invalid permanent index throws")
        void invalidPermanentIndexThrows() {
            assertThatThrownBy(() -> service.tapPermanent(gameData, player1, 999))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent index");
        }

        @Test
        @DisplayName("Tapping a land logs the action")
        void tappingLandLogsAction() {
            Card island = createLandWithManaAbility("Island", ManaColor.BLUE);
            Permanent perm = addReadyPermanent(player1Id, island);

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(false);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.tapPermanent(gameData, player1, 0);

            verify(gameBroadcastService).logAndBroadcast(eq(gameData), eq("Player1 taps Island."));
        }
    }

    // =========================================================================
    // sacrificePermanent
    // =========================================================================

    @Nested
    @DisplayName("sacrificePermanent")
    class SacrificePermanent {

        @Test
        @DisplayName("Sacrificing puts ability on the stack and removes permanent")
        void sacrificePutsAbilityOnStack() {
            Card aura = createCardWithSacrificeAbility("Aura of Silence");
            Permanent perm = addReadyPermanent(player1Id, aura);

            Card targetCard = createGenericArtifact("Lux Cannon");
            Permanent target = addReadyPermanent(player2Id, targetCard);
            UUID targetId = target.getId();

            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.findPermanentById(gameData, targetId)).thenReturn(target);
            when(gameQueryService.hasProtectionFrom(eq(gameData), eq(target), any())).thenReturn(false);
            when(gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, perm)).thenReturn(false);
            when(gameQueryService.hasProtectionFromSourceSubtypes(gameData, target, perm)).thenReturn(false);

            service.sacrificePermanent(gameData, player1, 0, targetId);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, perm);
            verify(triggerCollectionService).checkAllyPermanentSacrificedTriggers(gameData, player1Id, perm.getCard());
            verify(permanentRemovalService).removeOrphanedAuras(gameData);
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }

        @Test
        @DisplayName("Cannot sacrifice permanent with no ON_SACRIFICE effects")
        void cannotSacrificeWithoutEffects() {
            Card card = createCreatureCard("Grizzly Bears", 2, 2);
            Permanent perm = addReadyPermanent(player1Id, card);

            assertThatThrownBy(() -> service.sacrificePermanent(gameData, player1, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no sacrifice abilities");
        }

        @Test
        @DisplayName("Pithing Needle blocks sacrifice abilities")
        void pithingNeedleBlocksSacrifice() {
            Card aura = createCardWithSacrificeAbility("Aura of Silence");
            Permanent perm = addReadyPermanent(player1Id, aura);

            addPithingNeedle(player2Id, "Aura of Silence");

            assertThatThrownBy(() -> service.sacrificePermanent(gameData, player1, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated")
                    .hasMessageContaining("Pithing Needle");
        }

        @Test
        @DisplayName("Invalid permanent index throws")
        void invalidIndexThrows() {
            assertThatThrownBy(() -> service.sacrificePermanent(gameData, player1, 999, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid permanent index");
        }

        @Test
        @DisplayName("Sacrifice logs the action")
        void sacrificeLogsAction() {
            Card aura = createCardWithSacrificeAbility("Aura of Silence");
            Permanent perm = addReadyPermanent(player1Id, aura);

            Card targetCard = createGenericArtifact("Lux Cannon");
            Permanent target = addReadyPermanent(player2Id, targetCard);
            UUID targetId = target.getId();

            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.findPermanentById(gameData, targetId)).thenReturn(target);
            when(gameQueryService.hasProtectionFrom(eq(gameData), eq(target), any())).thenReturn(false);
            when(gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, perm)).thenReturn(false);
            when(gameQueryService.hasProtectionFromSourceSubtypes(gameData, target, perm)).thenReturn(false);

            service.sacrificePermanent(gameData, player1, 0, targetId);

            verify(gameBroadcastService).logAndBroadcast(eq(gameData), eq("Player1 sacrifices Aura of Silence."));
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
            Card card = createArtifactWithTapAbility("Lux Cannon");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.tap();

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already tapped");
        }

        @Test
        @DisplayName("Tap ability: summoning sick creature throws")
        void tapAbilitySummoningSickCreatureThrows() {
            Card creature = createCreatureWithActivatedTapAbility();
            Permanent perm = new Permanent(creature);
            // summoningSick is true by default
            gameData.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isCreature(gameData, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)).thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("summoning sickness");
        }

        @Test
        @DisplayName("Mana cost: insufficient mana throws")
        void insufficientManaThrows() {
            Card artifact = createArtifactWithManaAbility("{2}");
            Permanent perm = addReadyPermanent(player1Id, artifact);
            // Do NOT add any mana

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isArtifact(perm)).thenReturn(true);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        @Test
        @DisplayName("Mana cost: sufficient mana succeeds and is deducted")
        void sufficientManaSucceeds() {
            Card artifact = createArtifactWithManaAbility("{1}");
            Permanent perm = addReadyPermanent(player1Id, artifact);
            gameData.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isArtifact(perm)).thenReturn(true);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(activatedAbilityExecutionService).completeActivationAfterCosts(
                    eq(gameData), eq(player1), eq(perm), any(), any(), eq(0), eq(null), eq(null), eq(true), any(), any());
        }

        @Test
        @DisplayName("No activated abilities on permanent throws")
        void noActivatedAbilitiesThrows() {
            Card card = createCreatureCard("Grizzly Bears", 2, 2);
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no activated ability");
        }

        @Test
        @DisplayName("Invalid ability index throws")
        void invalidAbilityIndexThrows() {
            Card card = createArtifactWithTapAbility("Lux Cannon");
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, 99, null, null, null))
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
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player2Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sorcery speed");
        }

        @Test
        @DisplayName("SORCERY_SPEED: cannot activate outside main phase")
        void sorcerySpeedCannotActivateOutsideMainPhase() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.UPKEEP;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("main phase");
        }

        @Test
        @DisplayName("SORCERY_SPEED: cannot activate with non-empty stack")
        void sorcerySpeedCannotActivateWithStack() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;
            // Put something on the stack
            gameData.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                    StackEntryType.CREATURE_SPELL, new Card(), player1Id, "Test spell", List.of()));

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stack is empty");
        }

        @Test
        @DisplayName("SORCERY_SPEED: succeeds during own main phase with empty stack")
        void sorcerySpeedSucceedsDuringMainPhase() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.SORCERY_SPEED);
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(activatedAbilityExecutionService).completeActivationAfterCosts(
                    eq(gameData), eq(player1), eq(perm), any(), any(), eq(0), eq(null), eq(null), eq(true), any(), any());
        }

        @Test
        @DisplayName("ONLY_DURING_YOUR_UPKEEP: wrong step throws")
        void upkeepOnlyWrongStepThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP);
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("ONLY_DURING_YOUR_UPKEEP: opponent's upkeep throws")
        void upkeepOnlyOpponentTurnThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP);
            Permanent perm = addReadyPermanent(player1Id, card);

            gameData.activePlayerId = player2Id;
            gameData.currentStep = TurnStep.UPKEEP;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("upkeep");
        }

        @Test
        @DisplayName("METALCRAFT: fewer than 3 artifacts throws")
        void metalcraftInsufficientArtifactsThrows() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.METALCRAFT);
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isMetalcraftMet(gameData, player1Id)).thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("three or more artifacts");
        }

        @Test
        @DisplayName("METALCRAFT: exactly 3 artifacts succeeds")
        void metalcraftThreeArtifactsSucceeds() {
            Card card = createArtifactWithTimingRestriction(ActivationTimingRestriction.METALCRAFT);
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isMetalcraftMet(gameData, player1Id)).thenReturn(true);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(activatedAbilityExecutionService).completeActivationAfterCosts(
                    eq(gameData), eq(player1), eq(perm), any(), any(), eq(0), eq(null), eq(null), eq(true), any(), any());
        }

        @Test
        @DisplayName("POWER_4_OR_GREATER: low power creature throws")
        void power4OrGreaterLowPowerThrows() {
            Card card = createCreatureWithTimingRestriction(ActivationTimingRestriction.POWER_4_OR_GREATER, 2, 2);
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.getEffectivePower(gameData, perm)).thenReturn(2);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("power is 4 or greater");
        }

        @Test
        @DisplayName("POWER_4_OR_GREATER: high power creature succeeds")
        void power4OrGreaterHighPowerSucceeds() {
            Card card = createCreatureWithTimingRestriction(ActivationTimingRestriction.POWER_4_OR_GREATER, 5, 5);
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.getEffectivePower(gameData, perm)).thenReturn(5);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(activatedAbilityExecutionService).completeActivationAfterCosts(
                    eq(gameData), eq(player1), eq(perm), any(), any(), eq(0), eq(null), eq(null), eq(true), any(), any());
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
            Card card = createPlaneswalkerCard("Koth of the Hammer");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(3);

            gameData.activePlayerId = player2Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Loyalty abilities");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability outside main phase")
        void cannotActivateLoyaltyOutsideMainPhase() {
            Card card = createPlaneswalkerCard("Koth of the Hammer");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(3);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.UPKEEP;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("main phase");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability with non-empty stack")
        void cannotActivateLoyaltyWithStack() {
            Card card = createPlaneswalkerCard("Koth of the Hammer");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(3);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;
            gameData.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                    StackEntryType.CREATURE_SPELL, new Card(), player1Id, "Test spell", List.of()));

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stack is empty");
        }

        @Test
        @DisplayName("Cannot activate loyalty ability twice per turn")
        void cannotActivateLoyaltyTwicePerTurn() {
            Card card = createPlaneswalkerCard("Koth of the Hammer");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(5);
            perm.setLoyaltyActivationsThisTurn(1);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("one loyalty ability");
        }

        @Test
        @DisplayName("Cannot activate negative loyalty ability with insufficient counters")
        void cannotActivateNegativeLoyaltyWithInsufficientCounters() {
            Card card = createPlaneswalkerWithNegativeLoyalty("Koth of the Hammer", -2);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(1); // Need at least 2

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty counters");
        }

        @Test
        @DisplayName("Positive loyalty cost adds counters")
        void positiveLoyaltyCostAddsCounters() {
            Card card = createPlaneswalkerCard("Koth of the Hammer");
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setLoyaltyCounters(3);

            gameData.activePlayerId = player1Id;
            gameData.currentStep = TurnStep.PRECOMBAT_MAIN;

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            // +1 loyalty cost
            assertThat(perm.getLoyaltyCounters()).isEqualTo(4);
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
            Card card = createArtifactWithTapAbility("Lux Cannon");
            Permanent perm = addReadyPermanent(player1Id, card);

            addPithingNeedle(player2Id, "Lux Cannon");

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be activated")
                    .hasMessageContaining("Pithing Needle");
        }

        @Test
        @DisplayName("Pithing Needle does not block mana abilities (tested via tapPermanent)")
        void pithingNeedleDoesNotBlockManaAbilities() {
            Card island = createLandWithManaAbility("Island", ManaColor.BLUE);
            Permanent perm = addReadyPermanent(player1Id, island);

            addPithingNeedle(player2Id, "Island");

            when(gameQueryService.isCreature(gameData, perm)).thenReturn(false);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            int blueBefore = pool.get(ManaColor.BLUE);

            // tapPermanent does not check Pithing Needle for mana abilities
            service.tapPermanent(gameData, player1, 0);

            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        }

        @Test
        @DisplayName("Pithing Needle naming a different card does not block")
        void pithingNeedleDifferentNameDoesNotBlock() {
            Card card = createArtifactWithTapAbility("Lux Cannon");
            Permanent perm = addReadyPermanent(player1Id, card);

            addPithingNeedle(player2Id, "Grizzly Bears");

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(activatedAbilityExecutionService).completeActivationAfterCosts(
                    eq(gameData), eq(player1), eq(perm), any(), any(), eq(0), eq(null), eq(null), eq(true), any(), any());
        }

        @Test
        @DisplayName("Arrest blocks all activated abilities of enchanted creature")
        void arrestBlocksAllAbilities() {
            Card card = createCreatureCard("Nantuko Husk", 2, 2);
            card.addActivatedAbility(new ActivatedAbility(
                    false, null, List.of(new SacrificeCreatureCost(false, false), new BoostSelfEffect(2, 2)), "Sacrifice a creature: +2/+2"
            ));
            Permanent perm = addReadyPermanent(player1Id, card);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
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
            Card card = createArtifactWithChargeCounterAbility(3);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChargeCounters(2);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough charge counters");
        }

        @Test
        @DisplayName("RemoveChargeCountersFromSourceCost: removes exactly the required count")
        void removeChargeCountersExactCount() {
            Card card = createArtifactWithChargeCounterAbility(3);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChargeCounters(5);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            assertThat(perm.getChargeCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: no counters throws")
        void removeCounterNoCountersThrows() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1Id, card);
            // No counters on the permanent

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough counters to remove");
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: prefers removing -1/-1 counter first")
        void removeCounterPrefersMinusOneMinusOne() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setPlusOnePlusOneCounters(2);
            perm.setMinusOneMinusOneCounters(1);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            assertThat(perm.getMinusOneMinusOneCounters()).isEqualTo(0);
            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("RemoveCounterFromSourceCost: removes +1/+1 if no -1/-1 counters")
        void removeCounterFallsToPlusOnePlusOne() {
            Card card = createArtifactWithRemoveCounterAbility();
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setPlusOnePlusOneCounters(3);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(2);
        }
    }

    // =========================================================================
    // activateAbility — sacrifice creature cost (permanent-choice handler flow)
    // =========================================================================

    @Nested
    @DisplayName("activateAbility — sacrifice creature cost")
    class ActivateAbilitySacrificeCreature {

        @Test
        @DisplayName("Sacrifice creature cost: auto-pays when only one creature is available")
        void sacrificeCreatureCostAutoPaysSingleCreature() {
            Card huskCard = createCreatureCard("Nantuko Husk", 2, 2);
            huskCard.addActivatedAbility(new ActivatedAbility(
                    false, null, List.of(new SacrificeCreatureCost(false, false), new BoostSelfEffect(2, 2)),
                    "Sacrifice a creature: +2/+2"
            ));
            Permanent husk = addReadyPermanent(player1Id, huskCard);

            // Only husk on the battlefield — 1 creature = auto-pay
            when(gameQueryService.computeStaticBonus(gameData, husk)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(husk), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isCreature(gameData, husk)).thenReturn(true);
            when(gameQueryService.findPermanentById(gameData, husk.getId())).thenReturn(husk);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, husk);
            verify(triggerCollectionService).checkAllyPermanentSacrificedTriggers(gameData, player1Id, husk.getCard());
            verify(gameBroadcastService).logAndBroadcast(eq(gameData), eq("Player1 sacrifices Nantuko Husk."));
        }

        @Test
        @DisplayName("Sacrifice creature cost: prompts when multiple creatures are available")
        void sacrificeCreatureCostPromptsMultipleCreatures() {
            Card huskCard = createCreatureCard("Nantuko Husk", 2, 2);
            huskCard.addActivatedAbility(new ActivatedAbility(
                    false, null, List.of(new SacrificeCreatureCost(false, false), new BoostSelfEffect(2, 2)),
                    "Sacrifice a creature: +2/+2"
            ));
            Permanent husk = addReadyPermanent(player1Id, huskCard);

            Card bearsCard = createCreatureCard("Grizzly Bears", 2, 2);
            Permanent bears = addReadyPermanent(player1Id, bearsCard);

            // 2 creatures on battlefield — prompts for choice
            when(gameQueryService.computeStaticBonus(gameData, husk)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(husk), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isCreature(gameData, husk)).thenReturn(true);
            when(gameQueryService.isCreature(gameData, bears)).thenReturn(true);

            service.activateAbility(gameData, player1, 0, null, null, null, null);

            verify(playerInputService).beginPermanentChoice(eq(gameData), eq(player1Id), any(), eq("Choose a creature to sacrifice."));
            verify(gameBroadcastService).broadcastGameState(gameData);
        }

        @Test
        @DisplayName("Sacrifice creature cost: validateCanPay throws when no creatures available")
        void validateCanPayThrowsWhenNoCreatures() {
            Card artifactCard = createGenericArtifact("Phyrexian Altar");
            artifactCard.addActivatedAbility(new ActivatedAbility(
                    false, null, List.of(new SacrificeCreatureCost(false, false), new BoostSelfEffect(2, 2)),
                    "Sacrifice a creature: +2/+2"
            ));
            Permanent artifact = addReadyPermanent(player1Id, artifactCard);

            // No creatures on P1's battlefield — only a non-creature artifact
            when(gameQueryService.computeStaticBonus(gameData, artifact)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(artifact), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isCreature(gameData, artifact)).thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must choose a creature to sacrifice");
        }

        @Test
        @DisplayName("Sacrifice creature cost: only considers controller's creatures")
        void onlyConsidersControllerCreatures() {
            Card artifactCard = createGenericArtifact("Phyrexian Altar");
            artifactCard.addActivatedAbility(new ActivatedAbility(
                    false, null, List.of(new SacrificeCreatureCost(false, false), new BoostSelfEffect(2, 2)),
                    "Sacrifice a creature: +2/+2"
            ));
            Permanent artifact = addReadyPermanent(player1Id, artifactCard);

            // Opponent has a creature, but player 1 has none
            Card bearsCard = createCreatureCard("Grizzly Bears", 2, 2);
            addReadyPermanent(player2Id, bearsCard);

            when(gameQueryService.computeStaticBonus(gameData, artifact)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(artifact), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);
            when(gameQueryService.isCreature(gameData, artifact)).thenReturn(false);

            // getValidChoiceIds only checks player1's battlefield, so opponent's creature is invisible
            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must choose a creature to sacrifice");
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
            Permanent perm = addReadyPermanent(player1Id, card);

            // Simulate one activation already recorded this turn
            gameData.activatedAbilityUsesThisTurn
                    .computeIfAbsent(perm.getId(), k -> new java.util.concurrent.ConcurrentHashMap<>())
                    .put(0, 1);

            when(gameQueryService.computeStaticBonus(gameData, perm)).thenReturn(EMPTY_BONUS);
            when(gameQueryService.hasAuraWithEffect(eq(gameData), eq(perm), eq(EnchantedCreatureCantActivateAbilitiesEffect.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.activateAbility(gameData, player1, 0, null, null, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no more than 1 times each turn");
        }
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Permanent addReadyPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gameData.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    private void addPithingNeedle(UUID playerId, String cardName) {
        Card needleCard = new Card();
        needleCard.setName("Pithing Needle");
        needleCard.setType(CardType.ARTIFACT);
        needleCard.setManaCost("{1}");
        needleCard.addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(false));
        Permanent needle = new Permanent(needleCard);
        needle.setSummoningSick(false);
        needle.setChosenName(cardName);
        gameData.playerBattlefields.get(playerId).add(needle);
    }

    private Card createLandWithManaAbility(String name, ManaColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(color, 1));
        return card;
    }

    private Card createCreatureWithTapAbility(String name, ManaColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(color, 1));
        return card;
    }

    private Card createCreatureCard(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
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

    private Card createCardWithSacrificeAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}{W}{W}");
        card.setColor(CardColor.WHITE);
        card.addEffect(EffectSlot.ON_SACRIFICE, new DestroyTargetPermanentEffect());
        return card;
    }

    private Card createArtifactWithTapAbility(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{4}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new PutChargeCounterOnSelfEffect()), "Tap to add counter"
        ));
        return card;
    }

    private Card createCreatureWithActivatedTapAbility() {
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

    private Card createPlaneswalkerCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{2}{R}{R}");
        card.setColor(CardColor.RED);
        // +1 loyalty ability with a simple effect
        card.addActivatedAbility(new ActivatedAbility(
                1, List.of(new BoostSelfEffect(1, 1)), "+1: Test"
        ));
        return card;
    }

    private Card createPlaneswalkerWithNegativeLoyalty(String name, int loyaltyCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{2}{R}{R}");
        card.setColor(CardColor.RED);
        card.addActivatedAbility(new ActivatedAbility(
                loyaltyCost, List.of(new BoostSelfEffect(1, 1)), "Negative ability"
        ));
        return card;
    }

    private Card createArtifactWithChargeCounterAbility(int requiredCount) {
        Card card = new Card();
        card.setName("Test Charge Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new RemoveChargeCountersFromSourceCost(requiredCount), new PutChargeCounterOnSelfEffect()),
                "Remove charge counters"
        ));
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
