package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardMyrOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivatedAbilityExecutionServiceTest {

    @Mock private DamagePreventionService damagePreventionService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private SessionManager sessionManager;
    @Mock private LifeResolutionService lifeResolutionService;

    @InjectMocks
    private ActivatedAbilityExecutionService service;

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
        gameData.playerLifeTotals.put(player1Id, 20);
        gameData.playerLifeTotals.put(player2Id, 20);
        gameData.playerIdToName.put(player1Id, "Player1");
        gameData.playerIdToName.put(player2Id, "Player2");
        gameData.activePlayerId = player1Id;
    }

    // =========================================================================
    // Mana ability — immediate resolution (no stack)
    // =========================================================================

    @Nested
    @DisplayName("mana ability — immediate resolution")
    class ManaAbilityImmediateResolution {

        @Test
        @DisplayName("Pain land: adds mana and deals damage without using the stack")
        void painLandAddsManaAndDealsDamage() {
            Card card = createCard("Test Pain Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.WHITE, 1), new DealDamageToControllerEffect(1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {W}. Deals 1 damage.");

            stubIsCreature(perm, false);
            stubDamagePathForNormalDamage(perm, 1);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
            assertThat(gameData.playerLifeTotals.get(player1Id)).isEqualTo(19);
            assertThat(gameData.stack).isEmpty();
            verify(stateBasedActionService).performStateBasedActions(gameData);
            verify(gameBroadcastService).broadcastGameState(gameData);
        }

        @Test
        @DisplayName("Pain land: colorless ability adds mana without damage")
        void painLandColorlessNoDamage() {
            Card card = createCard("Test Pain Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.COLORLESS, 1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {C}.");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(gameData.playerLifeTotals.get(player1Id)).isEqualTo(20);
            assertThat(gameData.stack).isEmpty();
        }

        @Test
        @DisplayName("Pain land: blue ability adds blue mana and deals damage without stack")
        void painLandBlueAbility() {
            Card card = createCard("Test Pain Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.BLUE, 1), new DealDamageToControllerEffect(1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {U}. Deals 1 damage.");

            stubIsCreature(perm, false);
            stubDamagePathForNormalDamage(perm, 1);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
            assertThat(gameData.playerLifeTotals.get(player1Id)).isEqualTo(19);
            assertThat(gameData.stack).isEmpty();
        }

        @Test
        @DisplayName("Doubling Cube: doubles all mana in pool immediately")
        void doublingCubeDoublesAllMana() {
            Card card = createCard("Doubling Cube", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new DoubleManaPoolEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{3}, {T}: Double mana.");

            // Pre-populate pool (costs already paid by caller)
            gameData.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 2);
            gameData.playerManaPools.get(player1Id).add(ManaColor.RED, 2);

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(4);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(4);
            assertThat(gameData.stack).isEmpty();
        }

        @Test
        @DisplayName("Myr Reservoir: adds Myr-only colorless mana immediately")
        void myrReservoirAddsMyrOnlyMana() {
            Card card = createCard("Myr Reservoir", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardMyrOnlyColorlessManaEffect(2));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {C}{C} (Myr only).");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            ManaPool pool = gameData.playerManaPools.get(player1Id);
            assertThat(pool.getMyrOnlyColorless()).isEqualTo(2);
            assertThat(gameData.stack).isEmpty();
        }

        @Test
        @DisplayName("Mana ability taps the permanent when tap cost is required")
        void manaAbilityTapsPermanent() {
            Card card = createCard("Test Mana Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.COLORLESS, 1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {C}.");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(perm.isTapped()).isTrue();
            verify(triggerCollectionService).checkEnchantedPermanentTapTriggers(gameData, perm);
        }

        @Test
        @DisplayName("Mana ability logs the activation")
        void manaAbilityLogs() {
            Card card = createCard("Test Mana Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.COLORLESS, 1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {C}.");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gameData),
                    argThat(msg -> msg.contains("activates") && msg.contains("Test Mana Land")));
        }

        @Test
        @DisplayName("GainLifeEffect in mana ability delegates to lifeResolutionService")
        void gainLifeEffectDelegatesToLifeResolutionService() {
            Card card = createCard("Test Mana Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.WHITE, 1), new GainLifeEffect(1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {W}, gain 1 life.");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(lifeResolutionService).applyGainLife(gameData, player1Id, 1);
        }

        @Test
        @DisplayName("AwardAnyColorManaEffect sends color choice message via sessionManager")
        void awardAnyColorManaSendsChoiceMessage() {
            Card card = createCard("Test Any-Color Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(
                    new com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add one mana of any color.");

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(sessionManager).sendToPlayer(eq(player1Id), any(ChooseFromListMessage.class));
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
            Card card = createCreature("Test Boost Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(1, 1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Boost self");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("RegenerateEffect (non-targeting) auto-targets source permanent")
        void regenerateAutoTargets() {
            Card card = createCreature("Test Regenerate Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new RegenerateEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Regenerate");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("AnimateSelfWithStatsEffect auto-targets source permanent")
        void animateSelfAutoTargets() {
            Card card = createCard("Test Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AnimateSelfWithStatsEffect(2, 2, List.of(), Set.of()));
            ActivatedAbility ability = new ActivatedAbility(false, "{W}", effects, "Animate");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("AnimateLandEffect auto-targets source permanent")
        void animateLandAutoTargets() {
            Card card = createCard("Test Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AnimateLandEffect(3, 3, List.of(), Set.of(), CardColor.GREEN));
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Animate land");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("PutChargeCounterOnSelfEffect auto-targets source permanent")
        void putChargeCounterAutoTargets() {
            Card card = createCard("Test Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new PutChargeCounterOnSelfEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Put a charge counter.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetPermanentId()).isEqualTo(perm.getId());
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
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(1, 1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Boost self");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            verify(triggerCollectionService).checkBecomesTargetOfAbilityTriggers(gameData);
            verify(stateBasedActionService).performStateBasedActions(gameData);
            verify(gameBroadcastService).broadcastGameState(gameData);
        }

        @Test
        @DisplayName("Stack entry has correct controller")
        void stackEntryHasCorrectController() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(1, 1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Boost self");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack.getFirst().getControllerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Stack entry has correct source permanent ID")
        void stackEntryHasCorrectSourcePermanent() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(1, 1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Boost self");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack.getFirst().getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("Stack entry description includes card name")
        void stackEntryDescriptionIncludesCardName() {
            Card card = createCreature("Blight Mamba");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new RegenerateEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Regenerate");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack.getFirst().getDescription()).contains("Blight Mamba");
            assertThat(gameData.stack.getFirst().getDescription()).contains("ability");
        }

        @Test
        @DisplayName("Tap ability taps the permanent when pushed to stack")
        void tapAbilityTapsPermanent() {
            Card card = createCard("Test Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new PutChargeCounterOnSelfEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Put a charge counter.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(perm.isTapped()).isTrue();
            verify(triggerCollectionService).checkEnchantedPermanentTapTriggers(gameData, perm);
        }

        @Test
        @DisplayName("Non-tap ability does not tap the permanent")
        void nonTapAbilityDoesNotTap() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new RegenerateEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Regenerate");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(perm.isTapped()).isFalse();
            verify(triggerCollectionService, never()).checkEnchantedPermanentTapTriggers(any(), any());
        }

        @Test
        @DisplayName("Activation logs the action")
        void activationLogs() {
            Card card = createCreature("Blight Mamba");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new RegenerateEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Regenerate");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gameData),
                    argThat(msg -> msg.contains("activates") && msg.contains("Blight Mamba")));
        }

        @Test
        @DisplayName("Pending may abilities are processed after non-mana ability activation")
        void pendingMayAbilitiesProcessed() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(1, 1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Boost self");

            // Set up a pending may ability so the code path is hit
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    card, player1Id, List.of(new DrawCardEffect(1)), "Draw a card"));

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(playerInputService).processNextMayAbility(gameData);
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
            Card card = createCard("Test Replica", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ReturnTargetPermanentToHandEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{U}", effects, "Bounce target");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .noneMatch(e -> e instanceof SacrificeSelfCost);
            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .anyMatch(e -> e instanceof ReturnTargetPermanentToHandEffect);
        }

        @Test
        @DisplayName("CantBlockSourceEffect gets source permanent ID baked in")
        void cantBlockSourceGetsIdBakedIn() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new CantBlockSourceEffect(null));
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{R}", effects, "Can't block");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            assertThat(gameData.stack).hasSize(1);
            CantBlockSourceEffect snapshot = gameData.stack.getFirst().getEffectsToResolve().stream()
                    .filter(e -> e instanceof CantBlockSourceEffect)
                    .map(e -> (CantBlockSourceEffect) e)
                    .findFirst().orElseThrow();
            assertThat(snapshot.sourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("MustBlockSourceEffect gets source permanent ID baked in")
        void mustBlockSourceGetsIdBakedIn() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new MustBlockSourceEffect(null));
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{R}", effects, "Must block");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            assertThat(gameData.stack).hasSize(1);
            MustBlockSourceEffect snapshot = gameData.stack.getFirst().getEffectsToResolve().stream()
                    .filter(e -> e instanceof MustBlockSourceEffect)
                    .map(e -> (MustBlockSourceEffect) e)
                    .findFirst().orElseThrow();
            assertThat(snapshot.sourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("PreventNextColorDamageToControllerEffect gets chosen color baked in")
        void preventColorDamageGetsChosenColorBakedIn() {
            Card card = createCard("Test Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChosenColor(CardColor.RED);
            List<CardEffect> effects = List.of(new PreventNextColorDamageToControllerEffect());
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Prevent color damage");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            PreventNextColorDamageToControllerEffect snapshot = gameData.stack.getFirst().getEffectsToResolve().stream()
                    .filter(e -> e instanceof PreventNextColorDamageToControllerEffect)
                    .map(e -> (PreventNextColorDamageToControllerEffect) e)
                    .findFirst().orElseThrow();
            assertThat(snapshot.chosenColor()).isEqualTo(CardColor.RED);
        }
    }

    // =========================================================================
    // Self-sacrifice flow (SacrificeSelfCost)
    // =========================================================================

    @Nested
    @DisplayName("self-sacrifice flow")
    class SelfSacrificeFlow {

        @Test
        @DisplayName("SacrificeSelfCost removes permanent via removal service")
        void sacrificeRemovesPermanent() {
            Card card = createCard("Test Replica", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ReturnTargetPermanentToHandEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{U}", effects, "Bounce target");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, perm);
        }

        @Test
        @DisplayName("SacrificeSelfCost passes the correct permanent to removal service")
        void sacrificePassesCorrectPermanent() {
            Card card = createCard("Neurok Replica", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ReturnTargetPermanentToHandEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{U}", effects, "Bounce target");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            verify(permanentRemovalService).removePermanentToGraveyard(eq(gameData),
                    argThat(p -> p.getCard().getName().equals("Neurok Replica")));
        }

        @Test
        @DisplayName("SacrificeSelfCost still pushes ability on stack")
        void sacrificeStillPushesAbilityOnStack() {
            Card card = createCard("Test Replica", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ReturnTargetPermanentToHandEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{U}", effects, "Bounce target");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        }
    }

    // =========================================================================
    // Exile self cost
    // =========================================================================

    @Nested
    @DisplayName("exile self cost")
    class ExileSelfCostFlow {

        @Test
        @DisplayName("ExileSelfCost exiles permanent via removal service")
        void exileSelfCostExilesPermanent() {
            Card card = createCard("Test Exile Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new ExileSelfCost(), new DrawCardEffect(1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Exile self: draw a card");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            verify(permanentRemovalService).removePermanentToExile(gameData, perm);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("ExileSelfCost still pushes ability on stack")
        void exileSelfCostStillPushesAbilityOnStack() {
            Card card = createCard("Test Exile Artifact", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new ExileSelfCost(), new DrawCardEffect(1));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Exile self: draw a card");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            // ExileSelfCost should be filtered from the snapshot effects
            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .noneMatch(e -> e instanceof ExileSelfCost);
            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .anyMatch(e -> e instanceof DrawCardEffect);
        }
    }

    // =========================================================================
    // Death trigger ordering after sacrifice-as-cost (CR 603.3)
    // =========================================================================

    @Nested
    @DisplayName("death trigger ordering after sacrifice-as-cost (CR 603.3)")
    class SacrificeDeathTriggerOrdering {

        @Test
        @DisplayName("Death trigger from sacrifice is on top of activated ability on the stack")
        void deathTriggerOnTopOfActivatedAbility() {
            Card card = createCard("Test Spellbomb", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ExileTargetPlayerGraveyardEffect());
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Exile graveyard");

            // Simulate death trigger being added during sacrifice
            doAnswer(inv -> {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Death trigger", List.of(new DrawCardEffect(1)));
                gameData.stack.add(trigger);
                return null;
            }).when(permanentRemovalService).removePermanentToGraveyard(gameData, perm);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, player2Id, null, false);

            assertThat(gameData.stack).hasSize(2);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gameData.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("Activated ability is bottom of stack with correct effect")
        void activatedAbilityIsBottomWithCorrectEffect() {
            Card card = createCard("Test Spellbomb", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ExileTargetPlayerGraveyardEffect());
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Exile graveyard");

            doAnswer(inv -> {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Death trigger", List.of(new DrawCardEffect(1)));
                gameData.stack.add(trigger);
                return null;
            }).when(permanentRemovalService).removePermanentToGraveyard(gameData, perm);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, player2Id, null, false);

            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .anyMatch(e -> e instanceof ExileTargetPlayerGraveyardEffect);
        }

        @Test
        @DisplayName("Death trigger is top of stack with DrawCardEffect")
        void deathTriggerIsTopWithDrawCardEffect() {
            Card card = createCard("Test Spellbomb", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ExileTargetPlayerGraveyardEffect());
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Exile graveyard");

            doAnswer(inv -> {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Death trigger", List.of(new DrawCardEffect(1)));
                gameData.stack.add(trigger);
                return null;
            }).when(permanentRemovalService).removePermanentToGraveyard(gameData, perm);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, player2Id, null, false);

            assertThat(gameData.stack.getLast().getEffectsToResolve())
                    .anyMatch(e -> e instanceof DrawCardEffect);
        }

        @Test
        @DisplayName("Mana ability with sacrifice: death trigger goes on stack, mana resolves immediately")
        void manaAbilityDeathTriggerOnStack() {
            Card card = createCard("Test Chromatic Star", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.WHITE, 1));
            ActivatedAbility ability = new ActivatedAbility(false, "{1}", effects, "{1}, Sacrifice: Add one mana.");

            stubIsCreature(perm, false);

            doAnswer(inv -> {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Death trigger", List.of(new DrawCardEffect(1)));
                gameData.stack.add(trigger);
                return null;
            }).when(permanentRemovalService).removePermanentToGraveyard(gameData, perm);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            // Mana ability resolves immediately (no stack entry for mana)
            // Only the death trigger should be on the stack
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gameData.stack.getFirst().getEffectsToResolve())
                    .anyMatch(e -> e instanceof DrawCardEffect);
        }

        @Test
        @DisplayName("Sacrifice without death trigger: only activated ability on stack")
        void sacrificeWithoutDeathTriggerOnlyAbilityOnStack() {
            Card card = createCard("Test Replica", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            Permanent target = addReadyPermanent(player2Id, createCreature("Target Creature"));
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new ReturnTargetPermanentToHandEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{U}", effects, "Bounce target");

            // No death trigger added during sacrifice (mock does nothing by default)

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, target.getId(), null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
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
            Card card = createCard("Culling Dais", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChargeCounters(3);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new DrawCardsEqualToChargeCountersOnSourceEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}", effects, "{1}, Sacrifice: Draw cards.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(3);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, perm);
        }

        @Test
        @DisplayName("GainLifeEqualToChargeCountersOnSourceEffect snapshots counters as xValue")
        void gainLifeSnapshotsChargeCounters() {
            Card card = createCard("Golden Urn", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChargeCounters(5);
            List<CardEffect> effects = List.of(new SacrificeSelfCost(), new GainLifeEqualToChargeCountersOnSourceEffect());
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Sacrifice: Gain life.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("MillTargetPlayerByChargeCountersEffect snapshots counters as xValue")
        void millSnapshotsChargeCounters() {
            Card card = createCard("Grindclock", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            perm.setChargeCounters(4);
            List<CardEffect> effects = List.of(new MillTargetPlayerByChargeCountersEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Mill X.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, player2Id, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(4);
        }

        @Test
        @DisplayName("Charge counters snapshot to 0 when permanent has no counters")
        void snapshotZeroCounters() {
            Card card = createCard("Grindclock", CardType.ARTIFACT);
            Permanent perm = addReadyPermanent(player1Id, card);
            // No charge counters set
            List<CardEffect> effects = List.of(new MillTargetPlayerByChargeCountersEffect());
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Mill X.");

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, player2Id, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(0);
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
            Card card = createCard("Test Mana Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.COLORLESS, 1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {C}.");
            gameData.priorityPassedBy.add(player1Id);

            stubIsCreature(perm, false);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Priority is cleared after non-mana ability activation")
        void priorityClearedAfterStackPush() {
            Card card = createCreature("Test Creature");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new RegenerateEffect());
            ActivatedAbility ability = new ActivatedAbility(false, "{1}{G}", effects, "Regenerate");
            gameData.priorityPassedBy.add(player1Id);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.priorityPassedBy).isEmpty();
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
            Card card = createCard("Test Pain Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.WHITE, 1), new DealDamageToControllerEffect(1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {W}. Deals 1 damage.");

            stubIsCreature(perm, false);
            when(gameQueryService.isDamagePreventable(gameData)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gameData), any())).thenReturn(false);
            // Source-specific prevention is active for this permanent
            when(damagePreventionService.isSourceDamagePreventedForPlayer(gameData, player1Id, perm.getId()))
                    .thenReturn(true);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            // Mana should be added
            assertThat(gameData.playerManaPools.get(player1Id).get(ManaColor.WHITE)).isEqualTo(1);
            // Damage should be prevented — applyPlayerPreventionShield should NOT be called
            assertThat(gameData.playerLifeTotals.get(player1Id)).isEqualTo(20);
            verify(damagePreventionService, never()).applyPlayerPreventionShield(any(), any(), any(int.class));
        }

        @Test
        @DisplayName("Pain land damage is reduced by player damage prevention shield")
        void painLandDamageReducedByShield() {
            Card card = createCard("Test Pain Land", CardType.LAND);
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new AwardManaEffect(ManaColor.WHITE, 1), new DealDamageToControllerEffect(1));
            ActivatedAbility ability = new ActivatedAbility(true, null, effects, "{T}: Add {W}. Deals 1 damage.");

            stubIsCreature(perm, false);
            when(gameQueryService.isDamagePreventable(gameData)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gameData), any())).thenReturn(false);
            when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gameData), eq(player1Id), eq(perm.getId())))
                    .thenReturn(false);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gameData), eq(player1Id), any()))
                    .thenReturn(false);
            // Shield absorbs all 1 damage
            when(damagePreventionService.applyPlayerPreventionShield(gameData, player1Id, 1)).thenReturn(0);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gameData), eq(player1Id), eq(0), anyString()))
                    .thenReturn(0);

            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            // Mana should be added
            assertThat(gameData.playerManaPools.get(player1Id).get(ManaColor.WHITE)).isEqualTo(1);
            // 1 damage fully absorbed by shield
            assertThat(gameData.playerLifeTotals.get(player1Id)).isEqualTo(20);
            verify(damagePreventionService).applyPlayerPreventionShield(gameData, player1Id, 1);
        }
    }

    // =========================================================================
    // Sacrifice creature cost does NOT set nonTargeting flag
    // =========================================================================

    @Nested
    @DisplayName("sacrifice creature cost targeting flag")
    class SacrificeCreatureCostTargetingFlag {

        @Test
        @DisplayName("Sacrifice creature cost ability does not set nonTargeting on stack entry")
        void sacrificeCreatureCostDoesNotSetNonTargeting() {
            Card card = createCreature("Test Hopper");
            Permanent perm = addReadyPermanent(player1Id, card);
            List<CardEffect> effects = List.of(new BoostSelfEffect(2, 2));
            ActivatedAbility ability = new ActivatedAbility(false, null, effects, "Sacrifice a creature: +2/+2");

            // With the permanent-choice handler flow, completeActivationAfterCosts
            // is called with markAsNonTargetingForSacCreatureCost=false
            service.completeActivationAfterCosts(gameData, player1, perm, ability, effects, 0, null, null, false);

            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().isNonTargeting()).isFalse();
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

    private Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private void stubIsCreature(Permanent perm, boolean value) {
        when(gameQueryService.isCreature(gameData, perm)).thenReturn(value);
    }

    private void stubDamagePathForNormalDamage(Permanent perm, int damage) {
        when(gameQueryService.isDamagePreventable(gameData)).thenReturn(true);
        when(gameQueryService.isDamageFromSourcePrevented(eq(gameData), any())).thenReturn(false);
        when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gameData), eq(player1Id), eq(perm.getId())))
                .thenReturn(false);
        when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gameData), eq(player1Id), any()))
                .thenReturn(false);
        when(damagePreventionService.applyPlayerPreventionShield(gameData, player1Id, damage)).thenReturn(damage);
        when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gameData), eq(player1Id), eq(damage), anyString()))
                .thenReturn(damage);
        when(gameQueryService.shouldDamageBeDealtAsInfect(gameData, player1Id)).thenReturn(false);
        when(gameQueryService.canPlayerLifeChange(gameData, player1Id)).thenReturn(true);
    }
}
