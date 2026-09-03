package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveTimeCounterFromExiledCardEffectHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CreatureSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentReturnToHandCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentExileCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.DistinctNamePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.SequencePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentUntapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.AllMatchingPermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentBounceAction;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentChoiceCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentExileAction;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentSacrificeAction;
import com.github.laxika.magicalvibes.service.ability.cost.SacrificeXPermanentsCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCreatureCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCostSupport;
import com.github.laxika.magicalvibes.service.ability.cost.TapTwoSharingCreatureTypeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CrewCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.RemoveCounterFromPermanentCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.RemoveCounterFromCreatureCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PutCounterOnCreatureCostHandler;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaActivation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.PendingAbilityCounterCostActivation;
import com.github.laxika.magicalvibes.model.PendingGraveyardAbilityActivation;
import com.github.laxika.magicalvibes.model.PendingManaActivation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeSacrificedLandCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLandwalkOfSacrificedLandToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardXCostEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentAbilityLockEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCardGroupEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.TargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.FreeCyclingEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.HandCardCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.RevealTwoCardsSharingColorCost;
import com.github.laxika.magicalvibes.model.effect.RevealHandCost;
import com.github.laxika.magicalvibes.model.effect.HandRevealCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileInstantOrSorcerySpellCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ExileSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactsWithTotalManaValueCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromSingleGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.NinjutsuEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.CardDrawingEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ActivationCostModifierEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromPermanentOrSuspendedCardCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromGrantingPermanentCost;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledCreaturesCost;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllMatchingPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeDistinctNamePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesForManaCost;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedPermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.effect.TapTwoCreaturesSharingTypeCost;
import com.github.laxika.magicalvibes.model.effect.PowerBasedTapCost;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Handles activation and cost payment for activated abilities and tap/sacrifice abilities on permanents.
 *
 * <p>This service implements the MTG activated ability activation sequence (CR 602.2): declaring the ability,
 * choosing targets, paying costs (mana, tap, sacrifice, discard, counter removal), and placing the ability
 * on the stack. It also enforces activation restrictions such as Pithing Needle, timing restrictions,
 * per-turn activation limits, summoning sickness, and loyalty ability rules.
 *
 * <p>When a sacrifice cost requires player choice (e.g. multiple valid creatures to sacrifice), the service
 * enters an interactive flow: it stores a {@link PermanentChoiceContext}, prompts the player, and resumes
 * via the corresponding {@code complete*Choice} callback once the player responds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityActivationService {

    private final CardRevealService cardRevealService;

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final CastingCostService castingCostService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;
    private final ActivatedAbilityExecutionService activatedAbilityExecutionService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileService exileService;
    private final LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameMutationCoordinator mutationCoordinator;
    private final TapCostSupport tapCostSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterFromExiledCardEffectHandler;

    /**
     * Taps a permanent for its mana ability (ON_TAP effects), adding the produced mana to the player's pool.
     *
     * @param gameData       the current game state
     * @param player         the player tapping the permanent
     * @param permanentIndex index of the permanent on the player's battlefield
     * @throws IllegalStateException if the permanent is already tapped, has no tap effects,
     *                               has summoning sickness (creatures without haste), or is blocked by Arrest
     */
    /**
     * The mana quantity of a single-color {@code ON_TAP} producer modeled by
     * {@link ManaProducingEffect}. Tap-for-mana is always a flat ({@link Fixed}) amount — basic
     * lands and mana creatures — so there is no evaluation context to build here; a non-fixed
     * amount (which never occurs in an {@code ON_TAP} slot) contributes 0.
     */
    private static int onTapManaAmount(ManaProducingEffect effect) {
        return effect.estimatedManaAmount() instanceof Fixed fixed ? fixed.value() : 0;
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        if (permanent.isTapped()) {
            throw new IllegalStateException("Permanent is already tapped");
        }
        // Printed ON_TAP mana is an ability: a continuous "loses all abilities" strips it
        // (Imprisoned in the Moon / Deep Freeze). Granted mana abilities use activateAbility.
        if (gameQueryService.computeStaticBonus(gameData, permanent).losesAllAbilities()
                || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            throw new IllegalStateException("Permanent has lost its abilities");
        }
        // Check for land type override (e.g. Evil Presence / Lush Growth)
        List<ManaColor> overriddenManaColors = gameQueryService.getOverriddenLandManaColors(gameData, permanent);
        
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty() && overriddenManaColors.isEmpty()) {
            throw new IllegalStateException("Permanent has no tap effects");
        }
        if (gameQueryService.isSummoningSickForTapCost(gameData, permanent, playerId)) {
            throw new IllegalStateException("Creature has summoning sickness");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        // Serra Bestiary / Katabatic Winds: tapping for mana is a {T} ability, so it is locked too.
        if (gameQueryService.isLockedFromActivatingTapAbilities(gameData, permanent)) {
            throw new IllegalStateException("Tap abilities of " + permanent.getCard().getName() + " can't be activated");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent, true);
        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);
        validateNotBlockedByOpponentsTurnRestriction(gameData, playerId, permanent);

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        EnumMap<ManaColor, Integer> poolBefore = snapshotPoolColors(manaPool);
        EnumMap<ManaColor, Integer> creatureManaBefore = snapshotCreatureManaColors(manaPool);
        int totalManaBefore = manaPool.getTotalAllMana();
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);
        boolean snowSource = gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.SNOW);
        boolean caveSource = isCaveSource(gameData, permanent);
        boolean basicLandSource = permanent.getCard().hasType(CardType.LAND)
                && gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC);
        // Mana-production replacement effects are applied to the tapped permanent.
        int manaMultiplier = gameQueryService.manaProductionMultiplier(gameData, playerId, permanent);
        boolean playerControlsLand = permanent.getCard().hasType(CardType.LAND)
                && playerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()));
        ManaColor controllerLandFixedColor = playerControlsLand
                ? gameData.landManaFixedColorThisTurn.get(playerId)
                : null;
        boolean chosenLandManaReplacement = playerControlsLand
                && controllerLandFixedColor == null
                && gameData.playersWithLandManaChoiceReplacementThisTurn.contains(playerId);
        ManaColor fixedLandColor = controllerLandFixedColor != null
                ? controllerLandFixedColor
                : permanent.getCard().hasType(CardType.LAND)
                ? gameQueryService.fixedLandManaColor(gameData, permanent)
                : null;
        boolean anyColorReplacement = permanent.getCard().hasType(CardType.LAND)
                && fixedLandColor == null
                && gameQueryService.basicLandManaProducesAnyColor(gameData, permanent);
        Set<ManaColor> twistedColors = permanent.getCard().hasType(CardType.LAND)
                && fixedLandColor == null && !anyColorReplacement
                ? gameQueryService.twistedLandManaColors(gameData, permanent)
                : Set.of();
        if (chosenLandManaReplacement) {
            ChoiceContext.ManaColorChoice choiceContext =
                    new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, manaMultiplier)
                            .withCaveSource(caveSource)
                            .withBasicLandSource(basicLandSource);
            List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null, choiceContext, colors,
                    "Choose a color of mana to add (Harvest Mage)."));
        } else if (fixedLandColor != null) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = manaMultiplier;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect) * manaMultiplier;
                    }
                }
            }
            if (totalMana > 0) {
                if (snowSource) {
                    manaPool.addSnowMana(fixedLandColor, totalMana);
                } else {
                    manaPool.add(fixedLandColor, totalMana);
                }
                if (caveSource) {
                    manaPool.addCaveManaTag(fixedLandColor, totalMana);
                }
                if (basicLandSource) {
                    manaPool.addBasicLandManaTag(fixedLandColor, totalMana);
                }
                if (isCreatureSource) {
                    manaPool.addCreatureMana(fixedLandColor, totalMana);
                }
            }
        } else if (anyColorReplacement) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = manaMultiplier;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect) * manaMultiplier;
                    }
                }
            }
            if (totalMana > 0) {
                ChoiceContext.ManaColorChoice choiceContext =
                        new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, totalMana)
                                .withCaveSource(caveSource)
                                .withBasicLandSource(basicLandSource);
                List<String> colors = ManaColor.COLORS.stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors,
                        "Choose a color of mana to add."));
            }
        } else if (!twistedColors.isEmpty()) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = manaMultiplier;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect) * manaMultiplier;
                    }
                }
            }
            if (totalMana > 0) {
                if (twistedColors.size() == 1) {
                    ManaColor color = twistedColors.iterator().next();
                    if (snowSource) {
                        manaPool.addSnowMana(color, totalMana);
                    } else {
                        manaPool.add(color, totalMana);
                    }
                    if (caveSource) {
                        manaPool.addCaveManaTag(color, totalMana);
                    }
                    if (basicLandSource) {
                        manaPool.addBasicLandManaTag(color, totalMana);
                    }
                    if (isCreatureSource) {
                        manaPool.addCreatureMana(color, totalMana);
                    }
                } else {
                    ChoiceContext.ManaColorChoice choiceContext =
                            new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, totalMana)
                                    .withCaveSource(caveSource)
                                    .withBasicLandSource(basicLandSource);
                    List<String> colors = twistedColors.stream().map(Enum::name).toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors,
                            "Choose a color of mana to add (Reality Twist)."));
                }
            }
        } else if (!overriddenManaColors.isEmpty()) {
            // Land type is overridden — produce a new basic land type's mana instead of original
            if (overriddenManaColors.size() == 1) {
                if (snowSource) {
                    manaPool.addSnowMana(overriddenManaColors.getFirst(), manaMultiplier);
                } else {
                    manaPool.add(overriddenManaColors.getFirst(), manaMultiplier);
                }
                if (caveSource) {
                    manaPool.addCaveManaTag(overriddenManaColors.getFirst(), manaMultiplier);
                }
                if (basicLandSource) {
                    manaPool.addBasicLandManaTag(overriddenManaColors.getFirst(), manaMultiplier);
                }
                if (isCreatureSource) {
                    manaPool.addCreatureMana(overriddenManaColors.getFirst(), manaMultiplier);
                }
            } else {
                ChoiceContext.ManaColorChoice choiceContext =
                        new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, manaMultiplier)
                                .withCaveSource(caveSource)
                                .withBasicLandSource(basicLandSource);
                List<String> colors = overriddenManaColors.stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors,
                        "Choose a color of mana to add."));
            }
        } else {
            // Damping Sphere replacement: if a land is tapped for two or more mana, it produces {C} instead.
            boolean dampingReplacement = false;
            if (permanent.getCard().hasType(CardType.LAND) && isDampingManaReplacementActiveOnTap(gameData)) {
                int totalMana = 0;
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect);
                    }
                }
                if (totalMana >= 2) {
                    dampingReplacement = true;
                    manaPool.add(ManaColor.COLORLESS, manaMultiplier);
                    if (caveSource) {
                        manaPool.addCaveManaTag(ManaColor.COLORLESS, manaMultiplier);
                    }
                    if (basicLandSource) {
                        manaPool.addBasicLandManaTag(ManaColor.COLORLESS, manaMultiplier);
                    }
                }
            }
            if (!dampingReplacement) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        int amount = onTapManaAmount(awardMana) * manaMultiplier;
                        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId,
                                permanent, awardMana.color());
                        if (snowSource) {
                            manaPool.addSnowMana(effectiveColor, amount);
                        } else {
                            manaPool.add(effectiveColor, amount);
                        }
                        if (caveSource) {
                            manaPool.addCaveManaTag(effectiveColor, amount);
                        }
                        if (basicLandSource) {
                            manaPool.addBasicLandManaTag(effectiveColor, amount);
                        }
                        if (isCreatureSource) {
                            manaPool.addCreatureMana(effectiveColor, amount);
                        }
                    }
                }
            }
        }

        gameLogService.append(gameData, GameLog.playerTaps(player.getUsername(), permanent.getCard()));

        log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // CR 603.2 + 603.3: triggers from a mana ability (a land tapping for mana or
        // the enchanted permanent becoming tapped) wait until a player next would
        // receive priority before going on the stack. Defer them into
        // pendingManaAbilityTriggers so they don't block sorcery-speed spell casting
        // when mana is being tapped to pay a cost.
        int stackBeforeTriggers = gameData.stack.size();
        if (permanent.getCard().hasType(CardType.LAND)) {
            triggerCollectionService.checkLandTapTriggers(gameData, playerId, permanent.getId());
        }
        if (isCreatureSource) {
            triggerCollectionService.checkCreatureTapForManaTriggers(gameData, playerId, permanent.getId());
        }
        triggerCollectionService.checkSelfTappedForManaTriggers(gameData, permanent, playerId);
        if (!isAwaitingOwnManaColorChoice(gameData, playerId)) {
            triggerCollectionService.checkManaAbilityResolutionTriggers(
                    gameData, permanent, playerId, manaPool.getTotalAllMana() - totalManaBefore);
        }
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
        List<StackEntry> deferred = List.of();
        if (gameData.stack.size() > stackBeforeTriggers) {
            deferred = new ArrayList<>(
                    gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
            gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
            gameData.pendingManaAbilityTriggers.addAll(deferred);
        }

        if (isAwaitingOwnManaColorChoice(gameData, playerId)) {
            // A land whose type was overridden into several basic types stops to ask which colour to
            // add, so its mana does not exist yet. Recording now would log an activation with no
            // mana, and reverting it would untap the land while leaving the colour it went on to
            // produce floating in the pool.
            gameData.pendingRevertableManaActivation = new PendingManaActivation(
                    playerId, permanent.getId(), poolBefore, creatureManaBefore, List.copyOf(deferred));
        } else {
            recordRevertableManaActivation(gameData, playerId, permanent, poolBefore, creatureManaBefore, deferred);
        }

        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * True when the mana activation just performed stopped to ask this player which colour of
     * ordinary mana to add, so its mana is still owed and the pool diff would measure nothing.
     *
     * <p>Restricted variants (flashback-only, creature-spells-only, subtype, fixed-colour menus) are
     * excluded: they pay into buckets {@link #recordRevertableManaActivation} does not read, so a
     * revert would untap the source without draining what it produced.
     */
    static boolean isAwaitingOwnManaColorChoice(GameData gameData, UUID playerId) {
        PendingInteraction.PermanentChoice playerChoice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (playerChoice != null && playerId.equals(playerChoice.playerId())
                && playerChoice.context() instanceof PermanentChoiceContext.ManaAbilityAddToChosenPlayer chosen
                && chosen.anyColor()) {
            return true;
        }

        PendingInteraction.ColorChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (choice == null || !playerId.equals(choice.playerId())) {
            return false;
        }
        if (choice.context() instanceof ChoiceContext.DevotionManaColorChoice) {
            return true;
        }
        if (!(choice.context() instanceof ChoiceContext.ManaColorChoice manaChoice)) {
            return false;
        }
        return manaChoice.restrictedToCreatureSubtype() == null
                && !manaChoice.flashbackOnly()
                && !manaChoice.instantSorceryOnly()
                && !manaChoice.spellOrAbilitySubtype()
                && !manaChoice.creatureSpellOnly()
                && !manaChoice.creatureSpellOrAbilityOnly()
                && !manaChoice.abilityOnly()
                && !manaChoice.artifactSpellOrAbilityOnly()
                && manaChoice.fixedColorOptions() == null;
    }

    /** Per-color snapshot of the plain pool, for computing what a mana activation added. */
    static EnumMap<ManaColor, Integer> snapshotPoolColors(ManaPool pool) {
        EnumMap<ManaColor, Integer> snapshot = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            snapshot.put(color, pool.get(color));
        }
        return snapshot;
    }

    static EnumMap<ManaColor, Integer> snapshotCreatureManaColors(ManaPool pool) {
        EnumMap<ManaColor, Integer> snapshot = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            snapshot.put(color, pool.getCreatureMana(color));
        }
        return snapshot;
    }

    /**
     * Logs a completed pure mana activation (source tapped, mana added, nothing else) into
     * {@link GameData#revertableManaActivations} so the MTGO-style cancel-casting UI can undo it.
     */
    static void recordRevertableManaActivation(GameData gameData, UUID playerId, Permanent permanent,
                                               EnumMap<ManaColor, Integer> poolBefore,
                                               EnumMap<ManaColor, Integer> creatureManaBefore,
                                               List<StackEntry> deferredTriggers) {
        recordRevertableManaActivation(gameData, playerId, permanent.getId(), poolBefore,
                creatureManaBefore, deferredTriggers);
    }

    /**
     * Completes an activation that tapped its source and then stopped for a colour choice: the
     * parked snapshot is diffed against the pool now that the chosen mana has landed. Called by the
     * colour-choice answer handler, which is the first moment the produced mana exists.
     */
    public static void completeParkedManaActivation(GameData gameData, PendingManaActivation parked) {
        if (parked.poolBefore() != null) {
            recordRevertableManaActivation(gameData, parked.playerId(), parked.permanentId(),
                    parked.poolBefore(), parked.creatureManaBefore(), parked.deferredTriggers());
        }
    }

    private static void recordRevertableManaActivation(GameData gameData, UUID playerId, UUID permanentId,
                                                       EnumMap<ManaColor, Integer> poolBefore,
                                                       EnumMap<ManaColor, Integer> creatureManaBefore,
                                                       List<StackEntry> deferredTriggers) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        EnumMap<ManaColor, Integer> manaAdded = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> creatureManaAdded = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int added = pool.get(color) - poolBefore.getOrDefault(color, 0);
            if (added > 0) {
                manaAdded.put(color, added);
            }
            int creatureAdded = pool.getCreatureMana(color) - creatureManaBefore.getOrDefault(color, 0);
            if (creatureAdded > 0) {
                creatureManaAdded.put(color, creatureAdded);
            }
        }
        if (manaAdded.isEmpty() && deferredTriggers.isEmpty()) {
            return;
        }
        gameData.revertableManaActivations.add(new ManaActivation(
                playerId, permanentId, manaAdded, creatureManaAdded, List.copyOf(deferredTriggers)));
    }

    /**
     * Undoes this player's still-revertable mana-ability activations: untaps each recorded
     * source, drains the mana it produced, and removes its deferred triggers. Entries are
     * processed newest-first and skipped (dropped without reverting) when the produced mana
     * is no longer in the pool or the source is no longer tapped — a safety net in case the
     * mana was spent through a path that didn't clear the log.
     */
    public void revertManaActivations(GameData gameData, Player player) {
        UUID playerId = player.getId();
        ManaPool pool = gameData.playerManaPools.get(playerId);
        boolean revertedAny = false;

        // An activation still waiting on its colour choice owes mana that does not exist yet, so it
        // can neither be reverted nor left behind to attach itself to a later prompt.
        if (gameData.pendingRevertableManaActivation != null
                && playerId.equals(gameData.pendingRevertableManaActivation.playerId())) {
            gameData.pendingRevertableManaActivation = null;
        }

        List<ManaActivation> activations = gameData.revertableManaActivations;
        for (int i = activations.size() - 1; i >= 0; i--) {
            ManaActivation activation = activations.get(i);
            if (!playerId.equals(activation.playerId())) {
                continue;
            }
            activations.remove(i);

            Permanent source = gameQueryService.findPermanentById(gameData, activation.permanentId());
            boolean poolStillHasMana = pool != null && activation.manaAdded().entrySet().stream()
                    .allMatch(e -> pool.get(e.getKey()) >= e.getValue());
            if (source == null || !source.isTapped() || !poolStillHasMana) {
                continue;
            }

            source.untap();
            for (Map.Entry<ManaColor, Integer> e : activation.manaAdded().entrySet()) {
                for (int n = 0; n < e.getValue(); n++) {
                    pool.remove(e.getKey());
                }
            }
            for (Map.Entry<ManaColor, Integer> e : activation.creatureManaAdded().entrySet()) {
                pool.removeCreatureMana(e.getKey(), e.getValue());
            }
            gameData.pendingManaAbilityTriggers.removeAll(activation.deferredTriggers());
            revertedAny = true;
        }

        if (revertedAny) {
            String logEntry = player.getUsername() + " cancels — mana abilities reverted.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} reverts their mana ability activations", gameData.id, player.getUsername());
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Taps a land the player does not control for mana (Piracy). Legal only while the player is marked in
     * {@code gameData.mayTapLandsForSpellsUntilEndOfTurn}. The produced mana is routed into the player's
     * spell-only bucket so it can be spent only to cast spells.
     *
     * @param gameData     the current game state
     * @param player       the player tapping the foreign land
     * @param permanentId  the id of the land to tap (on any battlefield)
     * @throws IllegalStateException if the player may not tap foreign lands, the permanent is not a land
     *                               the player fails to control, it is already tapped, or has no mana ability
     */
    public void tapForeignLandForMana(GameData gameData, Player player, UUID permanentId) {
        UUID playerId = player.getId();
        if (!gameData.mayTapLandsForSpellsUntilEndOfTurn.contains(playerId)) {
            throw new IllegalStateException("You may not tap lands you don't control");
        }

        Permanent permanent = null;
        UUID controllerId = null;
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            for (Permanent candidate : entry.getValue()) {
                if (candidate.getId().equals(permanentId)) {
                    permanent = candidate;
                    controllerId = entry.getKey();
                }
            }
        }
        if (permanent == null) {
            throw new IllegalStateException("Land not found");
        }
        if (playerId.equals(controllerId)) {
            throw new IllegalStateException("You control that land; tap it normally");
        }
        if (!permanent.getCard().hasType(CardType.LAND)) {
            throw new IllegalStateException("Permanent is not a land");
        }
        if (permanent.isTapped()) {
            throw new IllegalStateException("Land is already tapped");
        }
        List<ManaColor> overriddenManaColors = gameQueryService.getOverriddenLandManaColors(gameData, permanent);
        ManaColor overriddenManaColor = overriddenManaColors.size() == 1 ? overriddenManaColors.getFirst() : null;
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty() && overriddenManaColors.isEmpty()) {
            throw new IllegalStateException("Land has no mana ability");
        }

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        boolean caveSource = isCaveSource(gameData, permanent);
        boolean basicLandSource = gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC);
        ManaColor fixedLandColor = gameQueryService.fixedLandManaColor(gameData, permanent);
        boolean anyColorReplacement = fixedLandColor == null
                && gameQueryService.basicLandManaProducesAnyColor(gameData, permanent);
        Set<ManaColor> twistedColors = fixedLandColor == null && !anyColorReplacement
                ? gameQueryService.twistedLandManaColors(gameData, permanent)
                : Set.of();
        if (fixedLandColor != null) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = 1;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect);
                    }
                }
            }
            if (totalMana > 0) {
                manaPool.add(fixedLandColor, totalMana);
                manaPool.addSpellOnlyMana(fixedLandColor, totalMana);
                if (basicLandSource) {
                    manaPool.addBasicLandManaTag(fixedLandColor, totalMana);
                }
                if (caveSource) {
                    manaPool.addCaveManaTag(fixedLandColor, totalMana);
                }
            }
        } else if (anyColorReplacement) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = 1;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof ManaProducingEffect manaEffect
                            && manaEffect.estimatedManaColor() != null) {
                        totalMana += onTapManaAmount(manaEffect);
                    }
                }
            }
            if (totalMana > 0) {
                ChoiceContext.ManaColorChoice choiceContext =
                        new ChoiceContext.ManaColorChoice(playerId, false, totalMana)
                                .withCaveSource(caveSource)
                                .withBasicLandSource(basicLandSource);
                List<String> colors = ManaColor.COLORS.stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors,
                        "Choose a color of mana to add."));
            }
        } else if (!twistedColors.isEmpty()) {
            int totalMana = 0;
            if (!overriddenManaColors.isEmpty()) {
                totalMana = 1;
            } else {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        totalMana += onTapManaAmount(awardMana);
                    }
                }
            }
            if (totalMana > 0) {
                if (twistedColors.size() == 1) {
                    ManaColor color = twistedColors.iterator().next();
                    manaPool.add(color, totalMana);
                    manaPool.addSpellOnlyMana(color, totalMana);
                    if (basicLandSource) {
                        manaPool.addBasicLandManaTag(color, totalMana);
                    }
                    if (caveSource) {
                        manaPool.addCaveManaTag(color, totalMana);
                    }
                } else {
                    // Piracy + multi-type under Reality Twist: pick one color for all mana.
                    ChoiceContext.ManaColorChoice choiceContext =
                            new ChoiceContext.ManaColorChoice(playerId, false, totalMana)
                                    .withCaveSource(caveSource)
                                    .withBasicLandSource(basicLandSource);
                    List<String> colors = twistedColors.stream().map(Enum::name).toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors,
                            "Choose a color of mana to add (Reality Twist)."));
                }
            }
        } else if (!overriddenManaColors.isEmpty()) {
            if (overriddenManaColors.size() == 1) {
                ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId,
                        permanent, overriddenManaColor);
                manaPool.add(effectiveColor, 1);
                manaPool.addSpellOnlyMana(effectiveColor, 1);
                if (basicLandSource) {
                    manaPool.addBasicLandManaTag(effectiveColor, 1);
                }
                if (caveSource) {
                    manaPool.addCaveManaTag(effectiveColor, 1);
                }
            } else {
                ChoiceContext.ManaColorChoice choiceContext =
                        new ChoiceContext.ManaColorChoice(playerId, false, 1)
                                .withCaveSource(caveSource)
                                .withBasicLandSource(basicLandSource);
                List<String> colors = overriddenManaColors.stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors,
                        "Choose a color of mana to add."));
            }
        } else {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof AwardManaEffect awardMana) {
                    int amount = onTapManaAmount(awardMana);
                    ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId,
                            permanent, awardMana.color());
                    manaPool.add(effectiveColor, amount);
                    manaPool.addSpellOnlyMana(effectiveColor, amount);
                    if (basicLandSource) {
                        manaPool.addBasicLandManaTag(effectiveColor, amount);
                    }
                    if (caveSource) {
                        manaPool.addCaveManaTag(effectiveColor, amount);
                    }
                }
            }
        }

        gameLogService.append(gameData,
                GameLog.playerTaps(player.getUsername(), permanent.getCard(), " for mana (Piracy)."));
        log.info("Game {} - {} taps foreign land {} for mana", gameData.id, player.getUsername(), permanent.getCard().getName());

        int stackBeforeTriggers = gameData.stack.size();
        triggerCollectionService.checkLandTapTriggers(gameData, playerId, permanent.getId());
        if (gameData.stack.size() > stackBeforeTriggers) {
            List<StackEntry> deferred = new ArrayList<>(
                    gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
            gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
            gameData.pendingManaAbilityTriggers.addAll(deferred);
        }

        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Pays 1 life to add {@code {C}} to the player's mana pool (Channel). This is a mana ability special
     * action, legal only while the player is marked in
     * {@code gameData.mayPayLifeForColorlessManaUntilEndOfTurn} and only if they have at least 1 life.
     *
     * @param gameData the current game state
     * @param player   the player paying life for mana
     * @throws IllegalStateException if the player may not pay life for mana this turn or has less than 1 life
     */
    public void payLifeForColorlessMana(GameData gameData, Player player) {
        UUID playerId = player.getId();
        if (!gameData.mayPayLifeForColorlessManaUntilEndOfTurn.contains(playerId)) {
            throw new IllegalStateException("You may not pay life for mana");
        }

        int life = gameData.getLife(playerId);
        if (life < 1) {
            throw new IllegalStateException("Not enough life to pay (need 1, have " + life + ")");
        }

        lifeSupport.applyLifePayment(gameData, playerId, 1, "Channel");
        gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, 1);

        String logEntry = player.getUsername() + " pays 1 life to add {C} (Channel).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} pays 1 life for colorless mana", gameData.id, player.getUsername());

        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Pays {@code {1}} for a Guardian Angel target's next-damage prevention shield. This is a
     * repeatable special action available to the Guardian Angel spell's controller until cleanup.
     */
    public void payGuardianAngel(GameData gameData, Player player, UUID targetId) {
        UUID playerId = player.getId();
        Set<UUID> targetIds = gameData.guardianAngelTargetsUntilEndOfTurn.get(playerId);
        if (targetIds == null || !targetIds.contains(targetId)) {
            throw new IllegalStateException("You may not pay for Guardian Angel prevention to that target");
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && !gameData.playerIds.contains(targetId)) {
            throw new IllegalStateException("Guardian Angel's target is no longer available");
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);
        ManaCost cost = new ManaCost("{1}");
        if (pool == null || !cost.canPay(pool)) {
            throw new IllegalStateException("Not enough mana to pay for Guardian Angel prevention");
        }
        cost.pay(pool);

        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + 1);
        } else if (gameData.playerIds.contains(targetId)) {
            gameData.playerDamagePreventionShields.merge(targetId, 1, Integer::sum);
        }

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " pays {1} for Guardian Angel prevention."));
        log.info("Game {} - {} pays {1} for Guardian Angel prevention to target {}",
                gameData.id, player.getUsername(), targetId);
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Activates an ON_SACRIFICE ability by sacrificing the source permanent and placing the ability on the stack.
     *
     * @param gameData          the current game state
     * @param player            the player sacrificing the permanent
     * @param permanentIndex    index of the permanent on the player's battlefield
     * @param targetId target for the sacrifice effect (e.g. for destroy-target abilities), or {@code null}
     * @throws IllegalStateException if the permanent has no sacrifice abilities, is blocked by Pithing Needle
     *                               or Arrest, or the target is invalid/protected
     */
    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetId) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        if (permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE).isEmpty()) {
            throw new IllegalStateException("Permanent has no sacrifice abilities");
        }

        // Pithing Needle / Phyrexian Revoker check: sacrifice abilities are activated abilities (never mana abilities)
        validateNotBlockedByNameLock(gameData, permanent.getCard().getName(), false);
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent, false);
        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);
        validateNotBlockedByOpponentsTurnRestriction(gameData, playerId, permanent);
        // Overwhelming Splendor: sacrifice abilities are never mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, null);
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, null);
        validateNotBlockedByCombatActionLock(gameData, null);

        // Validate target for effects that need one
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)) {
            if (effect instanceof DestroyTargetPermanentEffect) {
                if (targetId == null) {
                    throw new IllegalStateException("Sacrifice ability requires a target");
                }
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    throw new IllegalStateException("Invalid target permanent");
                }
                if (permanent.getCard().getTargetFilter() != null) {
                    predicateEvaluationService.validateTargetFilter(permanent.getCard().getTargetFilter(), target);
                }
                for (var sourceColor : gameQueryService.getEffectiveColors(gameData, permanent)) {
                    if (gameQueryService.hasProtectionFrom(gameData, target, sourceColor)) {
                        throw new IllegalStateException(target.getCard().getName() + " has protection from " + sourceColor.name().toLowerCase());
                    }
                }
                if (gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, permanent)) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getType().getDisplayName().toLowerCase() + "s");
                }
                if (gameQueryService.hasProtectionFromSourceSubtypes(gameData, target, permanent)) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from source's subtype");
                }
            }
        }

        // Sacrifice: remove from battlefield, add to graveyard
        permanentRemovalService.removePermanentToGraveyard(gameData, permanent);
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, permanent.getCard());
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " sacrifices " , permanent.getCard(), "."));
        log.info("Game {} - {} sacrifices {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Put activated ability on stack
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                permanent.getCard(),
                playerId,
                permanent.getCard().getName() + "'s ability",
                new ArrayList<>(permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)),
                0,
                targetId,
                Map.of()
        );
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);
        gameData.priorityPassedBy.clear();

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Activates an activated ability on a permanent, validating all costs and restrictions before placing
     * the ability on the stack. If a sacrifice cost requires player choice, the method enters an interactive
     * prompt flow and returns without completing activation.
     *
     * @param gameData          the current game state
     * @param player            the player activating the ability
     * @param permanentIndex    index of the source permanent on the player's battlefield
     * @param abilityIndex      index of the ability to activate (defaults to 0 if {@code null})
     * @param xValue            value for X in the mana cost (defaults to 0 if {@code null})
     * @param targetId target permanent for the ability, or creature to sacrifice as cost, or {@code null}
     * @param targetZone        target zone for zone-targeted effects, or {@code null}
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, null, null, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, null, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, damageAssignments, null, null);
    }

    /** Activates an ability printed on a spell while that spell is on the stack. */
    public void activateStackAbility(GameData gameData, Player player, UUID stackCardId,
                                     Integer abilityIndex, Integer discardHandCardIndex) {
        StackEntry sourceEntry = gameData.stack.stream()
                .filter(entry -> entry.getCard() != null)
                .filter(entry -> entry.getCard().getId().equals(stackCardId))
                .filter(entry -> entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("That spell is no longer on the stack"));

        List<ActivatedAbility> abilities = sourceEntry.getCard().getStackActivatedAbilities();
        int idx = effectiveAbilityIndex(abilityIndex);
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid stack ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        if (!ability.isActivatableByAnyPlayer()
                && !player.getId().equals(sourceEntry.getControllerId())) {
            throw new IllegalStateException("You cannot activate that ability");
        }

        validateNotBlockedByNonManaAbilityLock(gameData, player.getId(), ability);

        HandCardCost discardCost = ability.getEffects().stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCost != null) {
            if (discardCost.requiredCount(0) != 1) {
                throw new IllegalStateException("This stack ability requires more than one discard choice");
            }
            payDiscardCost(gameData, player, discardCost, discardHandCardIndex, 0, null, sourceEntry.getCard());
        }

        activatedAbilityExecutionService.completeStackActivationAfterCosts(gameData, player, sourceEntry, ability);
        flushActivatedAbilityCostTriggers(gameData);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " activates ", sourceEntry.getCard(), "'s ability from the stack."));
        log.info("Game {} - {} activates {}'s stack ability", gameData.id, player.getUsername(),
                sourceEntry.getCard().getName());
    }

    /** Activates an exile-only ability printed on a card in its owner's exile zone. */
    public void activateExiledAbility(GameData gameData, Player player, UUID exiledCardId,
                                      Integer abilityIndex, Integer xValue, UUID targetId) {
        ExiledCardEntry exileEntry = gameData.findExiledCard(exiledCardId);
        if (exileEntry == null || !player.getId().equals(exileEntry.ownerId())) {
            throw new IllegalStateException("That card is not in your exile zone");
        }
        Card card = exileEntry.card();
        List<ActivatedAbility> exileAbilities = card.getActivatedAbilities().stream()
                .filter(ActivatedAbility::isExileOnly)
                .toList();
        int exileIndex = effectiveAbilityIndex(abilityIndex);
        if (exileIndex < 0 || exileIndex >= exileAbilities.size()) {
            throw new IllegalStateException("Card has no exiled activated ability at that index");
        }
        int sourceAbilityIndex = card.getActivatedAbilities().indexOf(exileAbilities.get(exileIndex));
        activateAbilityInternal(gameData, player, -1, sourceAbilityIndex, xValue, targetId, null,
                null, null, null, null, new Permanent(card), null);
    }

    /**
     * Activates an activated ability on a card in the player's graveyard (e.g. Magma Phoenix's
     * "{3}{R}{R}: Return Magma Phoenix from your graveyard to your hand.").
     *
     * <p>Validates the card exists in the graveyard, has a graveyard activated ability, and that
     * the player can pay the mana cost. Pays the cost and pushes the ability onto the stack.</p>
     */
    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, null, null);
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex, Integer xValue) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue, null);
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex,
                                         Integer xValue, UUID targetId) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue, targetId, null);
    }

    /**
     * Graveyard ability activation whose targets are cards in graveyards (Soul of Innistrad). The ids
     * are validated before any cost is paid (CR 601.2c) — so a self-exiling ability may legally target
     * its own source card, which then fizzles once the exile cost is paid — and ride on the stack entry
     * as {@code targetCardIds} for the graveyard handlers to resolve.
     */
    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex,
                                         Integer xValue, UUID targetId, List<UUID> graveyardTargetIds) {
        activateGraveyardAbilityWithExileSelection(gameData, player, graveyardCardIndex, abilityIndex,
                xValue, targetId, graveyardTargetIds, null);
    }

    private void activateGraveyardAbilityWithExileSelection(
            GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex,
            Integer xValue, UUID targetId, List<UUID> graveyardTargetIds,
            List<UUID> exileGraveyardCardIds) {
        // Spell-only mana (Piracy) can't pay ability costs — hide it for the duration of this activation.
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (pool != null) {
            pool.setWhiteSpendableAsRed(gameQueryService.canSpendWhiteManaAsRed(gameData, player.getId()));
            pool.setWhiteSpendableAsAnyColor(gameQueryService.canSpendWhiteManaAsAnyColor(gameData, player.getId()));
            pool.setWhiteSpendableAsAnyColorWithoutRestriction(
                    gameQueryService.canSpendWhiteManaAsAnyColorUntilEndOfTurn(gameData, player.getId()));
            pool.setAllManaSpendableAsAnyColor(gameQueryService.canSpendManaAsAnyColor(gameData, player.getId()));
        }
        Map<ManaColor, Integer> withheldSpellOnlyMana = pool != null ? pool.withdrawSpellOnlyMana() : Map.of();
        boolean promotedAbilityOnlyMana = pool != null && pool.promoteAbilityOnlyMana() > 0;
        try {
            activateGraveyardAbilityImpl(gameData, player, graveyardCardIndex, abilityIndex,
                    xValue != null ? xValue : 0, targetId, graveyardTargetIds, exileGraveyardCardIds);
        } finally {
            if (pool != null && !withheldSpellOnlyMana.isEmpty()) {
                pool.restoreSpellOnlyMana(withheldSpellOnlyMana);
            }
            if (pool != null && promotedAbilityOnlyMana) {
                pool.restorePromotedAbilityOnlyMana();
            }
        }
    }

    private void activateGraveyardAbilityImpl(GameData gameData, Player player, int graveyardCardIndex,
                                              Integer abilityIndex, int xValue, UUID targetId,
                                              List<UUID> graveyardTargetIds,
                                              List<UUID> exileGraveyardCardIds) {
        // Ashes of the Abhorrent etc.: players can't activate abilities of cards in graveyards
        if (!gameQueryService.canPlayersActivateGraveyardAbilities(gameData)) {
            throw new IllegalStateException("Abilities of cards in graveyards can't be activated");
        }

        UUID playerId = player.getId();
        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyardCardIndex < 0 || graveyardCardIndex >= graveyard.size()) {
            throw new IllegalStateException("Invalid graveyard card index");
        }

        Card card = graveyard.get(graveyardCardIndex);
        List<ActivatedAbility> abilities = effectiveGraveyardAbilities(gameData, card, playerId);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no graveyard activated ability");
        }

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);

        // Validate targeting before any cost is paid (CR 601.2c) — same contract as hand abilities.
        List<CardEffect> abilityEffects = ability.getEffects();
        if (isMultiTargetGraveyardAbility(ability)) {
            // The ability announces a target group on the battlefield/players (Soul of Shandalar),
            // so the id list carries those targets rather than graveyard cards.
            targetLegalityService.validateMultiTargetAbility(gameData, playerId, ability,
                    graveyardTargetIds != null ? graveyardTargetIds : List.of(), card, xValue, abilityEffects);
        } else if (graveyardTargetIds != null) {
            targetLegalityService.validateMultiTargetGraveyardAbility(gameData, playerId, abilityEffects,
                    graveyardTargetIds, card.getId());
        } else {
            if (ability.targetsSpellOnStack(null)) {
                targetLegalityService.validateSpellTargetOnStack(
                        gameData, targetId, ability.getTargetFilter(), playerId);
            }
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetId, null, card, xValue);
        }

        // Validate timing restrictions applicable to graveyard abilities (e.g. Raid, activation conditions)
        validateGraveyardTimingRestrictions(gameData, playerId, ability, card);

        // Pithing Needle check: block non-mana activated abilities of the chosen name
        for (UUID opponentId : gameData.playerBattlefields.keySet()) {
            for (Permanent perm : gameData.playerBattlefields.get(opponentId)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect
                            && perm.getChosenName() != null
                            && perm.getChosenName().equals(card.getName())) {
                        throw new IllegalStateException("Activated abilities of " + card.getName() + " can't be activated (Pithing Needle)");
                    }
                }
            }
        }

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, ability);
        validateNotBlockedByCombatActionLock(gameData, ability);

        // Identify permanent-choice costs (e.g. return lands to hand)
        List<PermanentChoiceCostHandler> permanentChoiceCosts = ability.getEffects().stream()
                .map(e -> toPermanentChoiceCostHandler(gameData, e, null, 0))
                .filter(Objects::nonNull)
                .toList();

        // Validate permanent-choice costs can be paid before paying mana
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }

        // Exile-N-cards-from-graveyard cost (Salvage Titan: "Exile three artifact cards from your
        // graveyard"). Validate up front that enough matching cards other than the source exist —
        // the source card must remain in the graveyard for the ability's own return effect.
        ExileNCardsFromGraveyardCost exileNGraveyardCost = ability.getEffects().stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNGraveyardCost != null) {
            List<Card> candidates = matchingGraveyardExileCandidates(graveyard, exileNGraveyardCost, card);
            if (candidates.size() < exileNGraveyardCost.count()) {
                String typeName = graveyardExileFilterLabel(exileNGraveyardCost.requiredType(), null);
                throw new IllegalStateException("Not enough " + typeName + "cards in graveyard to exile (need "
                        + exileNGraveyardCost.count() + ")");
            }
            if (exileGraveyardCardIds == null && candidates.size() > exileNGraveyardCost.count()) {
                gameData.pendingGraveyardAbilityActivation = new PendingGraveyardAbilityActivation(
                        playerId, card, ability, xValue, targetId, 0, null, graveyardTargetIds);
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                                playerId,
                                null,
                                idx,
                                targetId,
                                null,
                                candidates,
                                "Choose " + exileNGraveyardCost.count()
                                        + " cards from your graveyard to exile as an activation cost.",
                                exileNGraveyardCost.count(),
                                exileNGraveyardCost.count(),
                                false));
                return;
            }
            if (exileGraveyardCardIds != null) {
                validateControllerGraveyardExileSelection(
                        gameData, playerId, exileNGraveyardCost, exileGraveyardCardIds, card);
            }
        }

        ExileXCardsFromGraveyardCost exileXGraveyardCost = ability.getEffects().stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileXGraveyardCost != null
                && matchingGraveyardExileCandidates(graveyard, exileXGraveyardCost, card).size() < xValue) {
            String typeName = graveyardExileFilterLabel(exileXGraveyardCost.requiredType(), null);
            throw new IllegalStateException("Not enough " + typeName + "cards in graveyard to exile (need "
                    + xValue + ")");
        }

        // Mill-controller activation cost (Rot Farm Skeleton: "Mill four cards"). CR 701.17b — a
        // player can't pay a cost that mills more cards than their library holds.
        MillControllerCost graveyardMillCost = ability.getEffects().stream()
                .filter(MillControllerCost.class::isInstance)
                .map(MillControllerCost.class::cast)
                .findFirst()
                .orElse(null);
        if (graveyardMillCost != null) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null || deck.size() < graveyardMillCost.count()) {
                throw new IllegalStateException("Not enough cards in library to mill (need "
                        + graveyardMillCost.count() + ")");
            }
        }

        // Discard-card(s) activation cost (Eternalize—{cost}, Discard a card / Haunted Dead Discard two):
        // validate up front that enough legal cards exist before paying any cost, so an unpayable
        // discard makes activation illegal without side effects (CR 602.2a).
        HandCardCost discardCardTypeCost = ability.getEffects().stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null
                && collectDiscardIndices(gameData, playerId, gameData.playerHands.get(playerId), discardCardTypeCost, xValue).size()
                < discardCardTypeCost.requiredCount(xValue)) {
            throw new IllegalStateException("No valid card to discard for the activation cost");
        }

        // Pay mana cost. Static effects (Embalmer's Tools) can make a matching graveyard card's
        // ability cost {N} less to activate; the reduction is floored to the generic portion so the
        // cost never drops below its colored requirements, then threaded through as a negative
        // additional generic cost.
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            ManaCost manaCost = new ManaCost(abilityCost);
            int genericCost = manaCost.getGenericCost();
            int additionalGenericCost = -Math.min(
                    castingCostService.getGraveyardActivatedAbilityCostReduction(gameData, playerId, card),
                    genericCost);
            AmountContext activationCostContext = new AmountContext(
                    playerId, null, null, xValue, 0, false, null, List.of(), card);
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof ActivationCostModifierEffect modifier) {
                    int amount = amountEvaluationService.evaluate(gameData, modifier.amount(), activationCostContext);
                    if (modifier.reducesGenericCost()) {
                        int reduction = Math.min(
                                amount, Math.max(0, genericCost + additionalGenericCost));
                        additionalGenericCost -= reduction;
                    } else {
                        additionalGenericCost += amount;
                    }
                }
            }
            payManaCostForSourceCard(gameData, playerId, card, abilityCost, xValue, false, false,
                    additionalGenericCost);
        }

        // Pay the mill-controller cost. Milled cards land on top of the graveyard, leaving the
        // source card (and the ability's own self-return) untouched.
        if (graveyardMillCost != null) {
            graveyardService.resolveMillPlayer(gameData, playerId, graveyardMillCost.count());
        }

        // Pay the exile-N-cards-from-graveyard cost
        if (exileNGraveyardCost != null) {
            if (exileGraveyardCardIds == null) {
                payGraveyardExileNCost(gameData, player, exileNGraveyardCost, card);
            } else {
                payChosenGraveyardExileNCost(
                        gameData, player, exileNGraveyardCost, exileGraveyardCardIds, card);
            }
        }

        if (exileXGraveyardCost != null) {
            payGraveyardExileXCost(gameData, player, exileXGraveyardCost, xValue, card);
        }

        // Pay the exile-this-card cost (Embalm / Eternalize). Exiling the source now — before the
        // ability is put on the stack — prevents the same graveyard card from being activated twice.
        if (ability.getEffects().stream().anyMatch(ExileSelfFromGraveyardCost.class::isInstance)) {
            graveyard.remove(card);
            graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, card);
            exileService.exileCard(gameData, playerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", card, " from the graveyard as an activation cost."));
        }

        // Pay permanent-choice costs (auto-pay or enter interactive mode)
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            if (handleGraveyardPermanentChoiceCost(gameData, player, card, graveyardCardIndex, idx, handler)) {
                return; // Entering interactive choice mode; activation will complete later
            }
        }

        // Discard-card(s) cost: enter interactive discard-choice mode. The source card may already have
        // been exiled above, so the suspended activation is resumed via handleActivatedAbilityDiscardCostChosen.
        if (discardCardTypeCost != null && discardCardTypeCost.requiredCount(xValue) > 0) {
            List<Integer> validDiscardIndices = collectDiscardIndices(gameData, playerId,
                    gameData.playerHands.get(playerId), discardCardTypeCost, xValue);
            gameData.pendingGraveyardAbilityActivation = new PendingGraveyardAbilityActivation(
                    playerId, card, ability, xValue, targetId,
                    discardCardTypeCost.requiredCount(xValue), null, graveyardTargetIds);
            String labelText = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
            String prompt = discardCardTypeCost.requiredCount(xValue) > 1
                    ? "Choose a " + labelText + "card to " + discardCardTypeCost.payVerb() + " as an activation cost ("
                    + discardCardTypeCost.requiredCount(xValue) + " remaining)."
                    : "Choose a " + labelText + "card to " + discardCardTypeCost.payVerb() + " as an activation cost.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    playerId, validDiscardIndices, prompt));
            return;
        }

        completeGraveyardAbilityActivation(gameData, player, card, ability, xValue, targetId, graveyardTargetIds);
    }

    /**
     * Whether a graveyard-activated ability announces a target group of permanents/players rather
     * than the single target or the graveyard-card list. Mirrors the battlefield activation
     * dispatch, so such an ability's ids go through {@code validateMultiTargetAbility} and land in
     * the stack entry's flat target list.
     */
    private boolean isMultiTargetGraveyardAbility(ActivatedAbility ability) {
        return !targetsGraveyardCards(ability)
                && (ability.isMultiTarget() || ability.getMaxTargets() > 1);
    }

    private boolean targetsGraveyardCards(ActivatedAbility ability) {
        return ability.getEffects().stream().anyMatch(effect ->
                effect instanceof TargetedGraveyardCardsEffect
                        || effect instanceof ReturnTargetCardsFromGraveyardToHandEffect
                        || effect instanceof ExileCardsFromGraveyardEffect
                        || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
    }

    /**
     * The graveyard-activated abilities a card offers from the given owner's graveyard: its own
     * printed graveyard abilities plus any granted by static effects on the battlefield. Granted
     * abilities are appended after the card's own so indices stay aligned with the client's card view.
     */
    public List<ActivatedAbility> effectiveGraveyardAbilities(GameData gameData, Card card, UUID ownerId) {
        if (gameQueryService.graveyardCardsHaveLostAllAbilities(gameData)) {
            return List.of();
        }
        List<ActivatedAbility> abilities = new ArrayList<>(card.getGraveyardActivatedAbilities());
        abilities.addAll(gameQueryService.computeGrantedGraveyardAbilitiesForOwnedCard(gameData, ownerId, card));
        return abilities;
    }

    /**
     * The hand-activated abilities a card currently offers: its own printed abilities followed by
     * abilities granted by static effects on the battlefield.
     */
    public List<ActivatedAbility> effectiveHandAbilities(GameData gameData, Card card, UUID ownerId) {
        List<ActivatedAbility> abilities = new ArrayList<>(card.getHandActivatedAbilities());
        abilities.addAll(gameQueryService.computeGrantedHandAbilitiesForOwnedCard(gameData, ownerId, card));
        return abilities;
    }

    private boolean handleGraveyardPermanentChoiceCost(GameData gameData, Player player, Card card,
                                                        int graveyardCardIndex, int abilityIndex,
                                                        PermanentChoiceCostHandler handler) {
        int required = handler.requiredCount();
        if (required <= 0) return false;
        UUID playerId = player.getId();
        // Mirror battlefield handlePermanentChoiceCost: sequence costs expose only the current
        // slot's candidates via getValidChoiceIds, so size<=required is not a safe auto-pay gate.
        if (handler.shouldAutoPayAll(gameData, playerId, required)) {
            List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
            for (UUID id : validIds) {
                Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                if (chosen != null) {
                    handler.validateAndPay(gameData, player, chosen);
                }
            }
            return false;
        }
        List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.GraveyardAbilityCostChoice(
                playerId, card, graveyardCardIndex, abilityIndex, handler.costEffect(), required));
        playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                handler.getPromptMessage(required));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        return true;
    }

    private boolean handleHandPermanentChoiceCost(GameData gameData, Player player, Card card,
                                                   ActivatedAbility ability, int abilityIndex, int xValue,
                                                   UUID targetId, PermanentChoiceCostHandler handler,
                                                   int stackSizeBeforeCosts) {
        int required = handler.requiredCount();
        if (required <= 0) {
            return false;
        }
        UUID playerId = player.getId();
        boolean tapBatch = handler.costEffect() instanceof TapMultiplePermanentsCost;
        if (tapBatch) {
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        }
        if (handler.shouldAutoPayAll(gameData, playerId, required)) {
            try {
                for (UUID id : handler.getValidChoiceIds(gameData, playerId)) {
                    Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                    if (chosen != null) {
                        handler.validateAndPay(gameData, player, chosen);
                    }
                }
            } finally {
                if (tapBatch) {
                    triggerCollectionService.endPermanentTapTriggerBatch(gameData);
                }
            }
            deferActivatedAbilityCostTriggers(gameData, stackSizeBeforeCosts);
            return false;
        }

        Zone targetZone = ability.isNeedsSpellTarget() ? Zone.STACK : null;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.HandAbilityCostChoice(
                playerId, card, ability, abilityIndex, xValue, targetId, targetZone,
                handler.costEffect(), required, stackSizeBeforeCosts));
        playerInputService.beginPermanentChoice(gameData, playerId, handler.getValidChoiceIds(gameData, playerId),
                handler.getPromptMessage(required));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        return true;
    }

    private Integer trackedSacrificedManaValue(CardEffect costEffect, Permanent chosen) {
        if (costEffect instanceof SacrificeCreatureCost cost && cost.trackSacrificedManaValue()) {
            return chosen.getCard().getManaValue();
        }
        if (costEffect instanceof SacrificePermanentCost cost && cost.trackSacrificedManaValue()) {
            return chosen.getCard().getManaValue();
        }
        return null;
    }

    /**
     * Callback for when a player has chosen a permanent for a graveyard ability's permanent-choice cost.
     * Validates the choice, pays the cost, and either re-prompts or completes the ability activation.
     */
    public void completeGraveyardAbilityCostChoice(GameData gameData, Player player,
                                                    PermanentChoiceContext.GraveyardAbilityCostChoice context,
                                                    UUID chosenPermanentId) {
        UUID playerId = player.getId();
        Card card = context.graveyardCard();
        int idx = context.abilityIndex() != null ? context.abilityIndex() : 0;
        ActivatedAbility ability = effectiveGraveyardAbilities(gameData, card, playerId).get(idx);

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(
                gameData, context.costEffect(), null, 0, context.chosenSoFar());
        if (handler == null) {
            throw new IllegalStateException("Unknown cost effect type");
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || !battlefield.contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent you control");
        }

        handler.validateAndPay(gameData, player, chosen);

        List<UUID> chosenSoFar = new ArrayList<>(context.chosenSoFar());
        chosenSoFar.add(chosenPermanentId);
        handler = toPermanentChoiceCostHandler(gameData, context.costEffect(), null, 0, chosenSoFar);

        int remaining = context.remaining() - handler.lastPaymentWeight();
        if (remaining > 0) {
            if (!handler.canPayRemaining(gameData, playerId, remaining)) {
                throw new IllegalStateException("Not enough permanents remaining");
            }
            if (handler.shouldAutoPayAll(gameData, playerId, remaining)) {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                for (UUID id : validIds) {
                    Permanent autoPay = gameQueryService.findPermanentById(gameData, id);
                    if (autoPay != null) {
                        handler.validateAndPay(gameData, player, autoPay);
                    }
                }
            } else {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.GraveyardAbilityCostChoice(
                        playerId, card, context.graveyardCardIndex(), context.abilityIndex(),
                        context.costEffect(), remaining, chosenSoFar));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
        }

        completeGraveyardAbilityActivation(gameData, player, card, ability, 0, null);
    }

    public void completeHandAbilityCostChoice(GameData gameData, Player player,
                                               PermanentChoiceContext.HandAbilityCostChoice context,
                                               UUID chosenPermanentId) {
        UUID playerId = player.getId();
        boolean tapBatch = context.costEffect() instanceof TapMultiplePermanentsCost;
        if (tapBatch && gameData.permanentTapTriggerBatchDepth == 0) {
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.stream().noneMatch(card -> card.getId().equals(context.handCard().getId()))) {
            throw new IllegalStateException("Source card is no longer in hand");
        }

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(
                gameData, context.costEffect(), null, context.xValue() != null ? context.xValue() : 0,
                context.chosenSoFar());
        if (handler == null) {
            throw new IllegalStateException("Unknown cost effect type");
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || !battlefield.contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent you control");
        }

        handler.validateAndPay(gameData, player, chosen);
        deferActivatedAbilityCostTriggers(gameData, context.stackSizeBeforeCosts());
        List<UUID> chosenSoFar = new ArrayList<>(context.chosenSoFar());
        chosenSoFar.add(chosenPermanentId);
        handler = toPermanentChoiceCostHandler(gameData, context.costEffect(), null,
                context.xValue() != null ? context.xValue() : 0, chosenSoFar);
        int remaining = context.remaining() - handler.lastPaymentWeight();
        if (remaining > 0) {
            if (!handler.canPayRemaining(gameData, playerId, remaining)) {
                throw new IllegalStateException("Not enough permanents remaining");
            }
            if (handler.shouldAutoPayAll(gameData, playerId, remaining)) {
                for (UUID id : handler.getValidChoiceIds(gameData, playerId)) {
                    Permanent autoPay = gameQueryService.findPermanentById(gameData, id);
                    if (autoPay != null) {
                        handler.validateAndPay(gameData, player, autoPay);
                    }
                }
                deferActivatedAbilityCostTriggers(gameData, context.stackSizeBeforeCosts());
            } else {
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.HandAbilityCostChoice(
                        playerId, context.handCard(), context.ability(), context.abilityIndex(), context.xValue(),
                        context.targetId(), context.targetZone(), context.costEffect(), remaining, chosenSoFar,
                        context.stackSizeBeforeCosts()));
                playerInputService.beginPermanentChoice(gameData, playerId,
                        handler.getValidChoiceIds(gameData, playerId), handler.getPromptMessage(remaining));
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
        }

        if (tapBatch) {
            triggerCollectionService.endPermanentTapTriggerBatch(gameData);
        }

        if (context.ability().isSourceStaysInHand()) {
            completeHandAbilityActivation(gameData, player, context.handCard(), context.ability(),
                    context.abilityIndex(), context.xValue() != null ? context.xValue() : 0,
                    context.targetId(), context.stackSizeBeforeCosts());
        } else {
            completeHandAbilityActivationAfterCosts(gameData, player, context.handCard(), context.ability(),
                    context.abilityIndex(), context.xValue() != null ? context.xValue() : 0,
                    context.targetId(), context.stackSizeBeforeCosts());
        }
    }

    private void completeGraveyardAbilityActivation(GameData gameData, Player player, Card card,
                                                    ActivatedAbility ability, int xValue, UUID targetId) {
        completeGraveyardAbilityActivation(gameData, player, card, ability, xValue, targetId, null);
    }

    private void completeGraveyardAbilityActivation(GameData gameData, Player player, Card card,
                                                    ActivatedAbility ability, int xValue, UUID targetId,
                                                    List<UUID> graveyardTargetIds) {
        UUID playerId = player.getId();

        // Filter out cost effects for the snapshot
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (!(effect instanceof CostEffect)) {
                snapshotEffects.add(effect);
            }
        }

        // Push ability onto the stack. Graveyard-card targets ride in targetCardIds (Zone.GRAVEYARD)
        // so the graveyard handlers resolve them and removed targets fizzle individually. A
        // battlefield/player target group instead rides in the flat targetIds list, exactly as it
        // does for a battlefield activation.
        boolean multiTarget = isMultiTargetGraveyardAbility(ability);
        boolean hasGraveyardTargets = !multiTarget && graveyardTargetIds != null && !graveyardTargetIds.isEmpty();
        boolean targetsSpellOnStack = !multiTarget && !hasGraveyardTargets && ability.targetsSpellOnStack(null);
        StackEntry stackEntry = multiTarget
                ? new StackEntry(
                        StackEntryType.ACTIVATED_ABILITY,
                        card,
                        playerId,
                        card.getName() + "'s ability",
                        snapshotEffects,
                        xValue,
                        targetId,
                        null,
                        Map.of(),
                        null,
                        List.of(),
                        graveyardTargetIds != null ? new ArrayList<>(graveyardTargetIds) : List.of())
                : hasGraveyardTargets
                ? new StackEntry(
                        StackEntryType.ACTIVATED_ABILITY,
                        card,
                        playerId,
                        card.getName() + "'s ability",
                        snapshotEffects,
                        xValue,
                        targetId,
                        null,
                        Map.of(),
                        Zone.GRAVEYARD,
                        new ArrayList<>(graveyardTargetIds),
                        List.of())
                : targetsSpellOnStack
                ? new StackEntry(
                        StackEntryType.ACTIVATED_ABILITY,
                        card,
                        playerId,
                        card.getName() + "'s ability",
                        snapshotEffects,
                        xValue,
                        targetId,
                        null,
                        Map.of(),
                        Zone.STACK,
                        List.of(),
                        List.of())
                : new StackEntry(
                        StackEntryType.ACTIVATED_ABILITY,
                        card,
                        playerId,
                        card.getName() + "'s ability",
                        snapshotEffects,
                        xValue,
                        targetId,
                        Map.of()
                );
        stackEntry.setTargetFilter(ability.getTargetFilter());
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);
        flushActivatedAbilityCostTriggers(gameData);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " activates " , card, "'s ability from the graveyard."));
        log.info("Game {} - {} activates {}'s graveyard ability", gameData.id, player.getUsername(), card.getName());

        // "Whenever you activate an eternalize or embalm ability, draw a card" (Vizier of the
        // Anointed). Fired after the ability is on the stack so the draw trigger lands above it.
        if (ability.isEmbalmOrEternalize()) {
            triggerCollectionService.checkControllerActivatesEternalizeOrEmbalmTriggers(gameData, playerId);
        }

        gameData.priorityPassedBy.clear();
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Activates an ability of a card in the player's hand (e.g. Reinforce N—{cost}:
     * "{cost}, Discard this card: Put N +1/+1 counters on target creature."). Discarding
     * the source card is an intrinsic part of the activation cost.
     *
     * <p>Validates targeting before any cost is paid (CR 601.2c), pays the mana cost, discards
     * the source card to the graveyard, then pushes the ability onto the stack.</p>
     */
    /** Activates an ability printed on a card while that card is in exile. */
    public void activateExileAbility(GameData gameData, Player player, UUID cardId, Integer abilityIndex,
                                     Integer xValue, UUID targetId) {
        UUID playerId = player.getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null || !playerId.equals(exiledEntry.ownerId())) {
            throw new IllegalStateException("Invalid exiled card");
        }
        if (exiledEntry.faceDown()) {
            throw new IllegalStateException("That card has no accessible abilities in exile");
        }

        Card card = exiledEntry.card();
        List<ActivatedAbility> abilities = card.getActivatedAbilities().stream()
                .filter(ActivatedAbility::isExileOnly)
                .toList();
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no exile-activated ability");
        }

        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);
        if (gameData.playersCantActivateAbilitiesThisTurn.contains(playerId)) {
            throw new IllegalStateException("You can't activate abilities this turn");
        }

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        int effectiveXValue = xValue != null ? xValue : 0;

        targetLegalityService.validateActivatedAbilityTargeting(
                gameData, playerId, ability, ability.getEffects(), targetId, null, card, effectiveXValue);
        validateGraveyardTimingRestrictions(gameData, playerId, ability, card);

        for (UUID opponentId : gameData.playerBattlefields.keySet()) {
            for (Permanent perm : gameData.playerBattlefields.get(opponentId)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect
                            && perm.getChosenName() != null
                            && perm.getChosenName().equals(card.getName())) {
                        throw new IllegalStateException("Activated abilities of " + card.getName()
                                + " can't be activated (Pithing Needle)");
                    }
                }
            }
        }

        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);
        validateNotBlockedByNameLock(gameData, card.getName(), isManaAbility(ability));
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, ability);
        validateNotBlockedByCombatActionLock(gameData, ability);

        if (ability.getManaCost() != null) {
            payManaCost(gameData, playerId, ability.getManaCost(), effectiveXValue, false, false);
        }

        List<CardEffect> snapshotEffects = ability.getEffects().stream()
                .filter(effect -> !(effect instanceof CostEffect))
                .toList();
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects,
                effectiveXValue,
                targetId,
                Map.of());
        stackEntry.setSourceZone(Zone.EXILE);
        stackEntry.setTargetFilter(ability.getTargetFilter());
        gameData.stack.add(stackEntry);
        flushActivatedAbilityCostTriggers(gameData);

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " activates ", card, "'s ability from exile."));
        log.info("Game {} - {} activates {}'s exile ability", gameData.id, player.getUsername(), card.getName());
        gameData.priorityPassedBy.clear();
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    public void activateHandAbility(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, UUID targetId) {
        activateHandAbility(gameData, player, handCardIndex, abilityIndex, targetId, null);
    }

    public void activateHandAbility(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, UUID targetId, Integer xValue) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || handCardIndex < 0 || handCardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid hand card index");
        }

        Card card = hand.get(handCardIndex);
        List<ActivatedAbility> abilities = effectiveHandAbilities(gameData, card, playerId);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no hand-activated ability");
        }

        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        List<CardEffect> abilityEffects = ability.getEffects();
        int effectiveXValue = xValue != null ? xValue : 0;
        if (ability.isSuspendsSourceFromHand() && ability.isSuspendTimeCountersFromX()
                && effectiveXValue < 1) {
            throw new IllegalStateException("Suspend X requires X to be at least 1");
        }

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, ability);
        validateNotBlockedByCombatActionLock(gameData, ability);
        validateHandTimingRestriction(gameData, playerId, ability);
        validateHandActivationLimitPerTurn(gameData, card, ability, idx);

        // Ninjutsu takes its own path: the source card is not discarded (it stays revealed in hand
        // until the ability resolves) and targetId names the attacker returned as a cost, not a
        // target, so the generic targeting validation below does not apply (CR 702.49a/b).
        if (ability.isNinjutsuAbility()) {
            activateNinjutsuAbility(gameData, player, card, ability, targetId);
            return;
        }

        // Validate targeting before any cost is paid — an illegal activation rewinds cleanly (CR 601.2c)
        targetLegalityService.validateActivatedAbilityTargeting(
                gameData, playerId, ability, abilityEffects, targetId, null, card, effectiveXValue);

        // A hand ability whose (reflexive) effect counters a spell or ability on the stack — e.g.
        // Nimble Obstructionist's cycling trigger — validates the chosen stack target against the
        // ability's target filter, mirroring the battlefield activated-ability path (which
        // validateActivatedAbilityTargeting only applies to permanent/player targets). A minTargets==0
        // ability with no target chosen (targetId == null) is the legal "decline" case, so cycling
        // still resolves and draws even with nothing to counter (CR 603.3c / 115.1d).
        if (ability.isNeedsSpellTarget() && targetId != null) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, ability.getTargetFilter(), playerId);
        }

        List<PermanentChoiceCostHandler> permanentChoiceCosts = abilityEffects.stream()
                .map(e -> toPermanentChoiceCostHandler(gameData, e, null, effectiveXValue))
                .filter(Objects::nonNull)
                .toList();
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }

        PayLifeCost payLifeCost = abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (payLifeCost != null) {
            if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)
                    || !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                throw new IllegalStateException("Players can't pay life to activate abilities");
            }
            int life = gameData.getLife(playerId);
            int needed = payLifeCost.effectiveAmount(life);
            if (life < needed) {
                throw new IllegalStateException("Not enough life to pay (need " + needed + ", have " + life + ")");
            }
        }

        int stackSizeBeforeCosts = gameData.stack.size();

        // Pay mana cost (throws before mutating the pool if it can't be afforded). A static effect
        // may replace a cycling ability's mana cost with {0} (New Perspectives, CR 118.9): the card
        // being cycled still counts toward the hand-size condition, so check the current hand size
        // before it is discarded below as part of the cost.
        String abilityCost = ability.getManaCost();
        if (abilityCost != null && cyclingCostReplacedWithZero(gameData, playerId, ability, hand.size())) {
            abilityCost = null;
        }
        if (abilityCost != null) {
            int cyclingReduction = ability.isCyclingAbility()
                    ? Math.min(castingCostService.getCyclingAbilityCostReduction(gameData, playerId),
                    new ManaCost(abilityCost).getGenericCost())
                    : 0;
            payManaCostForSourceCard(gameData, playerId, card, abilityCost, effectiveXValue,
                    false, false, -cyclingReduction);
        }

        if (payLifeCost != null) {
            int amount = payLifeCost.effectiveAmount(gameData.getLife(playerId));
            if (amount > 0) {
                lifeSupport.applyLifePayment(gameData, playerId, amount, card.getName());
            }
            deferActivatedAbilityCostTriggers(gameData, stackSizeBeforeCosts);
        }

        if (ability.isSourceStaysInHand() && ability.isRevealsSourceFromHand()) {
            cardRevealService.revealToAllPlayers(gameData, playerId,
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND, List.of(card));
        }

        if (ability.isSourceStaysInHand()) {
            for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
                if (handleHandPermanentChoiceCost(gameData, player, card, ability, idx, effectiveXValue,
                        targetId, handler, stackSizeBeforeCosts)) {
                    return;
                }
            }
            completeHandAbilityActivation(gameData, player, card, ability, idx, effectiveXValue, targetId,
                    stackSizeBeforeCosts);
            return;
        }

        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            if (handleHandPermanentChoiceCost(gameData, player, card, ability, idx, effectiveXValue,
                    targetId, handler, stackSizeBeforeCosts)) {
                return;
            }
        }
        if (ability.isSuspendsSourceFromHand()) {
            recordHandAbilityActivationUse(gameData, card, idx);
            hand.remove(handCardIndex);
            exileService.exileCard(gameData, playerId, card);
            int suspendTimeCounters = ability.isSuspendTimeCountersFromX()
                    ? effectiveXValue
                    : ability.getSuspendTimeCounters();
            gameData.exiledCardTimeCounters.put(card.getId(), suspendTimeCounters);
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " suspends ", card, "."));
            log.info("Game {} - {} suspends {} from hand", gameData.id,
                    player.getUsername(), card.getName());
            gameData.priorityPassedBy.clear();
            mutationCoordinator.invalidateAllPlayerViews(gameData);
            return;
        }
        if (ability.isExilesSourceFromHand()) {
            recordHandAbilityActivationUse(gameData, card, idx);
            hand.remove(handCardIndex);
            exileService.exileCard(gameData, playerId, card);
            if (isManaAbility(ability, abilityEffects)) {
                resolveHandManaAbility(gameData, player, card, abilityEffects, effectiveXValue);
                gameData.priorityPassedBy.clear();
                mutationCoordinator.invalidateAllPlayerViews(gameData);
            } else {
                completeHandAbilityActivation(gameData, player, card, ability, idx, effectiveXValue,
                        targetId, stackSizeBeforeCosts);
            }
            return;
        }
        completeHandAbilityActivationAfterCosts(gameData, player, card, ability, idx, effectiveXValue,
                targetId, stackSizeBeforeCosts);
    }

    private void completeHandAbilityActivation(GameData gameData, Player player, Card card,
                                               ActivatedAbility ability, int abilityIndex, int xValue, UUID targetId,
                                               int stackSizeBeforeCosts) {
        UUID playerId = player.getId();
        List<CardEffect> snapshotEffects = new ArrayList<>(ability.getEffects().stream()
                .filter(effect -> !(effect instanceof CostEffect))
                .toList());
        Zone targetZone = ability.isNeedsSpellTarget() ? Zone.STACK : null;
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects,
                xValue,
                targetId,
                null,
                Map.of(),
                targetZone,
                List.of(),
                List.of()
        );
        stackEntry.setTargetFilter(ability.getTargetFilter());
        flushActivatedAbilityCostTriggers(gameData);
        recordHandAbilityActivationUse(gameData, card, abilityIndex);
        int insertionIndex = Math.min(Math.max(stackSizeBeforeCosts, 0), gameData.stack.size());
        gameData.stack.add(insertionIndex, stackEntry);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " activates ", card, "'s ability from their hand."));
        log.info("Game {} - {} activates {}'s hand ability", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    private void completeHandAbilityActivationAfterCosts(GameData gameData, Player player, Card card,
                                                         ActivatedAbility ability, int abilityIndex, int xValue,
                                                         UUID targetId, int stackSizeBeforeCosts) {
        UUID playerId = player.getId();
        boolean discarded = false;
        if (ability.isRevealsSourceFromHand()) {
            cardRevealService.revealToAllPlayers(gameData, playerId,
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND, List.of(card));
        } else {
            List<Card> hand = gameData.playerHands.get(playerId);
            int handCardIndex = -1;
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (hand.get(i).getId().equals(card.getId())) {
                        handCardIndex = i;
                        break;
                    }
                }
            }
            if (handCardIndex < 0) {
                throw new IllegalStateException("Source card is no longer in hand");
            }
            hand.remove(handCardIndex);
            UUID previousCyclingCard = gameData.cardEnteringGraveyardByCycling;
            if (ability.isCyclingAbility()) {
                gameData.cardEnteringGraveyardByCycling = card.getId();
            }
            try {
                graveyardService.addCardToGraveyard(gameData, playerId, card);
                discarded = true;
            } finally {
                gameData.cardEnteringGraveyardByCycling = previousCyclingCard;
            }
            gameData.discardCausedByOpponent = false;
        }

        List<CardEffect> snapshotEffects = new ArrayList<>(ability.getEffects().stream()
                .filter(effect -> !(effect instanceof CostEffect))
                .toList());
        Zone targetZone = ability.isNeedsSpellTarget() ? Zone.STACK : null;
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects,
                xValue,
                targetId,
                null,
                Map.of(),
                targetZone,
                List.of(),
                List.of()
        );
        flushActivatedAbilityCostTriggers(gameData);
        recordHandAbilityActivationUse(gameData, card, abilityIndex);
        int insertionIndex = Math.min(Math.max(stackSizeBeforeCosts, 0), gameData.stack.size());
        gameData.stack.add(insertionIndex, stackEntry);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);
        if (discarded) {
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
            if (ability.isCyclingAbility()) {
                triggerCollectionService.checkCycleTriggers(gameData, playerId, card);
            }
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " activates ", card, "'s ability from their hand."));
        log.info("Game {} - {} activates {}'s hand ability", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Activates a ninjutsu ability (CR 702.49a): pays the mana cost and returns the chosen unblocked
     * attacking creature the activating player controls to its owner's hand, then puts the ability on
     * the stack.
     *
     * <p>The source card stays in hand — revealed until the ability leaves the stack (CR 702.49b) —
     * because the resolution effect is what moves it to the battlefield. The returned attacker's
     * attack target is captured here and baked into the {@link NinjutsuEffect} snapshot so the ninja
     * enters attacking the same player or planeswalker (CR 702.49c); the attacker itself is gone from
     * the battlefield by the time the ability resolves.
     *
     * @param ninjaTargetId the unblocked attacking creature returned to hand as part of the cost
     */
    private void activateNinjutsuAbility(GameData gameData, Player player, Card card,
                                         ActivatedAbility ability, UUID ninjaTargetId) {
        UUID playerId = player.getId();
        if (ninjaTargetId == null) {
            throw new IllegalStateException("Ninjutsu requires an unblocked attacking creature to return");
        }

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        Permanent attacker = battlefield.stream()
                .filter(p -> p.getId().equals(ninjaTargetId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ninjutsu requires an unblocked attacker you control"));
        if (!predicateEvaluationService.matchesPermanentPredicate(
                gameData, attacker, new PermanentIsUnblockedAttackingPredicate())) {
            throw new IllegalStateException("Ninjutsu requires an unblocked attacker you control");
        }

        UUID attackTargetId = attacker.getAttackTarget();

        // Mana first: payManaCost throws before mutating the pool if it can't be afforded, so an
        // unaffordable activation never bounces the attacker.
        if (ability.getManaCost() != null) {
            payManaCostForSourceCard(gameData, playerId, card, ability.getManaCost(), 0, false, false, 0);
        }
        permanentRemovalService.removePermanentToHand(gameData, attacker);
        gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(), " is returned to its owner's hand."));

        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ninjutsu ability",
                List.<CardEffect>of(new NinjutsuEffect(attackTargetId)),
                0,
                null,
                null,
                Map.of(),
                null,
                List.of(),
                List.of()
        );
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " activates ninjutsu on ", card, "."));
        log.info("Game {} - {} activates ninjutsu on {}", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Resolves the mana produced by a hand-activated mana ability whose source card has just been
     * exiled to pay its cost (Elvish Spirit Guide). A mana ability never uses the stack (CR 605.1a),
     * and the source is no longer a permanent, so the mana goes straight into the controller's pool.
     * Mana-doubling effects keyed on tapping a permanent do not apply here.
     */
    private void resolveHandManaAbility(GameData gameData, Player player, Card card,
                                        List<CardEffect> abilityEffects, int xValue) {
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        for (CardEffect effect : abilityEffects) {
            if (effect instanceof AwardManaEffect awardMana) {
                int amount = awardMana.amount() instanceof Fixed fixed ? fixed.value() : xValue;
                if (amount > 0) {
                    ManaProductionSupport.add(gameData, player.getId(), pool, awardMana.color(), amount);
                }
            }
        }
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", card, " from their hand for mana."));
        log.info("Game {} - {} exiles {} from hand for mana", gameData.id, player.getUsername(), card.getName());
    }

    /**
     * True if {@code ability}'s mana cost is currently replaced with {0} for {@code playerId}: the
     * ability is a cycling ability and the player controls a permanent granting free cycling while
     * they hold enough cards in hand (New Perspectives, CR 118.9). Cycling is identified by the
     * ability's reminder-text description ending in "cycling", the engine's convention for cycling.
     * {@code handSize} is the hand size at activation, which still includes the card being cycled.
     */
    private boolean cyclingCostReplacedWithZero(GameData gameData, UUID playerId, ActivatedAbility ability, int handSize) {
        if (!ability.isCyclingAbility()) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof FreeCyclingEffect freeCycling && handSize >= freeCycling.minCardsInHand()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Activates a hand ability whose targets are cards in graveyards (e.g. Faerie Macabre:
     * "Discard this card: Exile up to two target cards from graveyards."). Mirrors
     * {@link #activateHandAbility} but carries a list of graveyard target card IDs instead of a
     * single battlefield/player target.
     *
     * <p>Targets are chosen and validated first (CR 601.2c), so the source card being discarded as a
     * cost is never itself a legal target; the discard cost is then paid before the ability is put on
     * the stack.</p>
     */
    public void activateHandAbilityWithGraveyardTargets(GameData gameData, Player player, int handCardIndex,
                                                        Integer abilityIndex, List<UUID> graveyardCardIds) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || handCardIndex < 0 || handCardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid hand card index");
        }

        Card card = hand.get(handCardIndex);
        List<ActivatedAbility> abilities = effectiveHandAbilities(gameData, card, playerId);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no hand-activated ability");
        }

        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        List<CardEffect> abilityEffects = ability.getEffects();

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, ability);
        validateNotBlockedByCombatActionLock(gameData, ability);
        validateHandTimingRestriction(gameData, playerId, ability);
        validateHandActivationLimitPerTurn(gameData, card, ability, idx);

        // Validate targeting before any cost is paid — an illegal activation rewinds cleanly (CR 601.2c)
        targetLegalityService.validateMultiTargetGraveyardAbility(gameData, playerId, abilityEffects,
                graveyardCardIds, card.getId(), null, ability.getMultiTargetConstraint());

        // Pay mana cost (throws before mutating the pool if it can't be afforded)
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            payManaCostForSourceCard(gameData, playerId, card, abilityCost, 0, false, false, 0);
        }

        // Pay the intrinsic source-card cost. Forecast reveals the source instead of discarding it.
        if (ability.isRevealsSourceFromHand()) {
            cardRevealService.revealToAllPlayers(gameData, playerId,
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND, List.of(card));
        } else {
            hand.remove(handCardIndex);
            UUID previousCyclingCard = gameData.cardEnteringGraveyardByCycling;
            if (ability.isCyclingAbility()) {
                gameData.cardEnteringGraveyardByCycling = card.getId();
            }
            try {
                graveyardService.addCardToGraveyard(gameData, playerId, card);
            } finally {
                gameData.cardEnteringGraveyardByCycling = previousCyclingCard;
            }
            gameData.discardCausedByOpponent = false;
            collectDiscardTriggersAsAbilityCost(gameData, playerId, card, ability.isCyclingAbility());
        }

        // Push the ability onto the stack with its graveyard targets (cost effects are not resolved)
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (!(effect instanceof CostEffect)) {
                snapshotEffects.add(effect);
            }
        }
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects,
                0,
                null,
                null,
                Map.of(),
                Zone.GRAVEYARD,
                graveyardCardIds,
                List.of()
        );
        gameData.stack.add(stackEntry);
        recordHandAbilityActivationUse(gameData, card, idx);
        triggerCollectionService.checkCrimeTriggers(gameData, stackEntry);
        flushActivatedAbilityCostTriggers(gameData);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " activates " , card, "'s ability from their hand."));
        log.info("Game {} - {} activates {}'s hand ability targeting graveyards", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Callback for when a player has chosen which card to discard as an activated ability's discard cost
     * (e.g. {@link DiscardCardTypeCost}). Resumes the pending ability activation with the chosen card.
     *
     * @param gameData  the current game state
     * @param player    the player who chose the card
     * @param cardIndex index of the chosen card in the player's hand
     * @throws IllegalStateException if there is no pending ability activation, the player is not the one
     *                               who should be choosing, or the card index is invalid
     */
    public void handleOpponentChosenTarget(GameData gameData, Player player, UUID chosenTargetId,
                                            PermanentChoiceContext.ActivatedAbilityOpponentTarget context) {
        if (!player.getId().equals(context.choosingPlayerId())) {
            throw new IllegalStateException("Not the player who chooses this target");
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        Player activatingPlayer = new Player(context.activatingPlayerId(),
                gameData.playerIdToName.get(context.activatingPlayerId()));
        List<UUID> targetIds = List.of(context.firstTargetId(), chosenTargetId);
        activateAbilityInternal(gameData, activatingPlayer, -1, context.abilityIndex(), context.xValue(),
                context.firstTargetId(), context.targetZone(), null, null, targetIds, null,
                sourcePermanent, null, true);
    }

    public void handleSoleOpponentChosenTarget(GameData gameData, Player player, UUID chosenTargetId,
                                                PermanentChoiceContext.ActivatedAbilitySoleOpponentTarget context) {
        if (!player.getId().equals(context.choosingPlayerId())) {
            throw new IllegalStateException("Not the player who chooses this target");
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        Player activatingPlayer = new Player(context.activatingPlayerId(),
                gameData.playerIdToName.get(context.activatingPlayerId()));
        activateAbilityInternal(gameData, activatingPlayer, -1, context.abilityIndex(), context.xValue(),
                chosenTargetId, context.targetZone(), null, null, null, null,
                sourcePermanent, null, true);
    }

    public void handleActivatedAbilityDiscardCostChosen(GameData gameData, Player player, int cardIndex) {
        com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice cardChoice =
                gameData.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice.class);
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        // A graveyard-activated ability (Eternalize) suspended on its discard cost resumes down its own
        // completion path rather than the battlefield re-entry below.
        if (gameData.pendingGraveyardAbilityActivation != null) {
            handleGraveyardAbilityDiscardCostChosen(gameData, player, cardChoice, cardIndex);
            return;
        }
        if (gameData.pendingAbilityActivation == null) {
            throw new IllegalStateException("No pending ability activation");
        }
        if (cardChoice.validIndices() == null || !cardChoice.validIndices().contains(cardIndex)) {
            // Invalid index — re-prompt the discard cost choice
            log.warn("Game {} - {} sent invalid discard cost card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            interactionHandlerRegistry.requestActiveDecision(gameData);
            return;
        }

        PendingAbilityActivation pending = gameData.pendingAbilityActivation;
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        ActivatedAbility ability = resolveAbility(gameData, source, pending.abilityIndex());
        HandCardCost discardCost = ability.getEffects().stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .findFirst()
                .orElseThrow();

        gameData.interaction.clearAwaitingInput();
        PaidHandCard paid = payDiscardCost(gameData, player, discardCost, cardIndex, pending.xValue(),
                pending.discardCostRequiredName(), source.getCard());
        String requiredName = discardCost.sameName()
                ? (pending.discardCostRequiredName() != null ? pending.discardCostRequiredName() : paid.name())
                : null;
        int xForActivation = discardCost.trackManaValue() ? paid.manaValue() : pending.xValue();

        int remaining = pending.remainingDiscards() - 1;
        if (remaining > 0) {
            List<Integer> validDiscardIndices = collectDiscardIndices(
                    gameData, player.getId(), gameData.playerHands.get(player.getId()), discardCost,
                    pending.xValue(), requiredName);
            if (validDiscardIndices.size() < remaining) {
                clearPendingAbilityActivation(gameData);
                throw new IllegalStateException("No valid card to discard for the activation cost");
            }
            gameData.pendingAbilityActivation = new PendingAbilityActivation(
                    pending.sourcePermanentId(),
                    pending.abilityIndex(),
                    pending.xValue(),
                    pending.targetId(),
                    pending.targetZone(),
                    pending.discardCostLabel(),
                    remaining,
                    requiredName,
                    pending.targetIds(),
                    pending.damageAssignments()
            );
            String labelText = pending.discardCostLabel() != null ? pending.discardCostLabel() + " " : "";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    player.getId(), validDiscardIndices,
                    "Choose a " + labelText + "card to " + discardCost.payVerb() + " as an activation cost ("
                            + remaining + " remaining)."));
            return;
        }

        // The source may be controlled by another player (an "any player may activate" ability such as
        // Oona's Prowler); re-enter with the already-resolved source rather than an own-battlefield index.
        // discardCardIndex = -1 signals that all required discards were already paid above.
        clearPendingAbilityActivation(gameData);
        activateAbilityInternal(
                gameData,
                player,
                -1,
                pending.abilityIndex(),
                xForActivation,
                pending.targetId(),
                pending.targetZone(),
                -1,
                null,
                pending.targetIds(),
                pending.damageAssignments(),
                source,
                null
        );
    }

    /**
     * Resumes an activated ability after its controller chooses how many source counters to remove
     * as an X-valued activation cost.
     */
    public void handleActivatedAbilityCounterCostChosen(GameData gameData, Player player, int chosenValue) {
        PendingAbilityCounterCostActivation pending = gameData.pendingAbilityCounterCostActivation;
        if (pending == null) {
            throw new IllegalStateException("No pending counter-cost activation");
        }

        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            gameData.pendingAbilityCounterCostActivation = null;
            gameData.interaction.clearAwaitingInput();
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        int available = source.getCounterCount(pending.counterType());
        if (chosenValue < 0 || chosenValue > available) {
            throw new IllegalArgumentException("Counter removal must be between 0 and " + available);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.pendingAbilityCounterCostActivation = null;
        activateAbilityInternal(
                gameData,
                player,
                -1,
                pending.abilityIndex(),
                chosenValue,
                pending.targetId(),
                pending.targetZone(),
                null,
                null,
                null,
                null,
                source,
                null
        );
    }

    /**
     * Resumes a graveyard-activated ability suspended on its discard cost after the player picks a card.
     * Pays one discard; if more remain (e.g. Haunted Dead's "Discard two cards"), re-prompts; otherwise
     * pushes the ability onto the stack.
     */
    private void handleGraveyardAbilityDiscardCostChosen(GameData gameData, Player player,
                                                         PendingInteraction.DiscardCostChoice cardChoice, int cardIndex) {
        PendingGraveyardAbilityActivation pending = gameData.pendingGraveyardAbilityActivation;
        Card card = pending.card();
        ActivatedAbility ability = pending.ability();
        HandCardCost discardCost = ability.getEffects().stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .findFirst()
                .orElseThrow();

        if (cardChoice.validIndices() == null || !cardChoice.validIndices().contains(cardIndex)) {
            // Invalid index — re-prompt the discard cost choice.
            interactionHandlerRegistry.requestActiveDecision(gameData);
            return;
        }

        gameData.interaction.clearAwaitingInput();
        PaidHandCard paid = payDiscardCost(gameData, player, discardCost, cardIndex, pending.xValue(),
                pending.discardCostRequiredName(), card);
        String requiredName = discardCost.sameName()
                ? (pending.discardCostRequiredName() != null ? pending.discardCostRequiredName() : paid.name())
                : null;
        int xForActivation = discardCost.trackManaValue() ? paid.manaValue() : pending.xValue();

        int remaining = pending.remainingDiscards() - 1;
        if (remaining > 0) {
            List<Integer> validDiscardIndices = collectDiscardIndices(
                    gameData, player.getId(), gameData.playerHands.get(player.getId()), discardCost,
                    pending.xValue(), requiredName);
            if (validDiscardIndices.size() < remaining) {
                gameData.pendingGraveyardAbilityActivation = null;
                throw new IllegalStateException("No valid card to discard for the activation cost");
            }
            gameData.pendingGraveyardAbilityActivation = new PendingGraveyardAbilityActivation(
                    pending.playerId(), card, ability, pending.xValue(), pending.targetId(), remaining,
                    requiredName, pending.graveyardTargetIds());
            String labelText = discardCost.label() != null ? discardCost.label() + " " : "";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    pending.playerId(), validDiscardIndices,
                    "Choose a " + labelText + "card to " + discardCost.payVerb() + " as an activation cost ("
                            + remaining + " remaining)."));
            return;
        }

        gameData.pendingGraveyardAbilityActivation = null;
        completeGraveyardAbilityActivation(gameData, player, card, ability, xForActivation,
                pending.targetId(), pending.graveyardTargetIds());
    }

    public void handleActivatedAbilityGraveyardExileCostChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.interaction.activeInteraction(PendingInteraction.GraveyardExileCostChoice.class) == null) {
            throw new IllegalStateException("Not awaiting graveyard exile cost choice");
        }
        if (gameData.pendingAbilityActivation == null) {
            throw new IllegalStateException("No pending ability activation");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || cardIndex < 0 || cardIndex >= graveyard.size()) {
            throw new IllegalStateException("Invalid graveyard card index");
        }

        PendingAbilityActivation pending = gameData.pendingAbilityActivation;
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        clearPendingAbilityActivation(gameData);
        activateAbilityInternal(
                gameData,
                player,
                -1,
                pending.abilityIndex(),
                pending.xValue(),
                pending.targetId(),
                pending.targetZone(),
                null,
                cardIndex,
                pending.targetIds(),
                pending.damageAssignments(),
                source,
                null
        );
    }

    public void handleActivatedAbilityRevealCardsChosen(
            GameData gameData, Player player,
            PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice,
            List<UUID> cardIds) {
        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice activeChoice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)
                || choice.activatedAbilityContext() == null) {
            throw new IllegalStateException("Not awaiting activated ability hand reveal cost choice");
        }
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingInteraction.ActivatedAbilityRevealContext context = choice.activatedAbilityContext();
        List<UUID> selectedIds = cardIds == null ? List.of() : List.copyOf(cardIds);
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null) {
            gameData.interaction.clearAwaitingInput();
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }
        ActivatedAbility ability = resolveAbility(gameData, source, context.abilityIndex());
        HandRevealCost handRevealCost = ability.getEffects().stream()
                .filter(HandRevealCost.class::isInstance)
                .map(HandRevealCost.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Activated ability no longer has the hand reveal cost"));
        validateHandRevealSelection(gameData, player.getId(), handRevealCost, context.xValue(), selectedIds);

        gameData.interaction.clearAwaitingInput();
        activateAbilityInternal(
                gameData, player, -1, context.abilityIndex(), context.xValue(), context.targetId(),
                context.targetZone(), null, null, context.targetIds(), context.damageAssignments(), source,
                null, null, null, false, selectedIds);
    }

    public void handleActivatedAbilityGraveyardExileCostChosen(
            GameData gameData, Player player,
            PendingInteraction.ActivatedAbilityGraveyardExileCostChoice choice,
            List<UUID> cardIds) {
        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice activeChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)) {
            throw new IllegalStateException("Not awaiting activated ability graveyard exile cost choice");
        }
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> selectedIds = cardIds == null ? List.of() : List.copyOf(cardIds);
        if (selectedIds.size() > choice.cards().size()) {
            throw new IllegalStateException("Too many cards selected");
        }
        if (new HashSet<>(selectedIds).size() != selectedIds.size()
                || !choice.validCardIds().containsAll(selectedIds)) {
            throw new IllegalStateException("Invalid graveyard card selection");
        }
        if (selectedIds.size() < choice.minimumCards() || selectedIds.size() > choice.maximumCards()) {
            throw new IllegalStateException("Invalid number of graveyard cards selected");
        }
        if (choice.singleGraveyard() && !cardsAreFromSingleGraveyard(gameData, selectedIds)) {
            throw new IllegalStateException("Selected cards must be from a single graveyard");
        }

        if (choice.sourcePermanentId() == null) {
            PendingGraveyardAbilityActivation pending = gameData.pendingGraveyardAbilityActivation;
            if (pending == null || !player.getId().equals(pending.playerId())) {
                throw new IllegalStateException("No pending graveyard ability activation");
            }
            ExileNCardsFromGraveyardCost cost = pending.ability().getEffects().stream()
                    .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                    .map(ExileNCardsFromGraveyardCost.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Graveyard ability no longer has the graveyard exile cost"));
            validateControllerGraveyardExileSelection(
                    gameData, player.getId(), cost, selectedIds, pending.card());
            List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
            int cardIndex = indexOfCard(graveyard, pending.card().getId());
            if (cardIndex < 0) {
                gameData.pendingGraveyardAbilityActivation = null;
                gameData.interaction.clearAwaitingInput();
                throw new IllegalStateException("Source card is no longer in the graveyard");
            }
            gameData.pendingGraveyardAbilityActivation = null;
            gameData.interaction.clearAwaitingInput();
            activateGraveyardAbilityWithExileSelection(
                    gameData, player, cardIndex, choice.abilityIndex(), pending.xValue(),
                    pending.targetId(), pending.graveyardTargetIds(), selectedIds);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, choice.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }
        ActivatedAbility ability = resolveAbility(gameData, source, choice.abilityIndex());
        ExileCardFromGraveyardCost anyGraveyardCost = ability.getEffects().stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .filter(ExileCardFromGraveyardCost::anyGraveyard)
                .findFirst()
                .orElse(null);
        if (anyGraveyardCost != null) {
            if (selectedIds.size() != 1
                    || !collectAnyGraveyardExileCandidates(gameData, anyGraveyardCost).stream()
                    .map(Card::getId)
                    .toList()
                    .contains(selectedIds.getFirst())) {
                throw new IllegalStateException("Must choose a matching card from a graveyard");
            }
            gameData.interaction.clearAwaitingInput();
            activateAbilityInternal(
                    gameData,
                    player,
                    -1,
                    choice.abilityIndex(),
                    choice.minimumCards(),
                    choice.targetId(),
                    choice.targetZone(),
                    null,
                    null,
                    null,
                    null,
                    source,
                    null,
                    selectedIds,
                    null,
                    false
            );
            return;
        }
        ExileNCardsFromGraveyardCost controllerGraveyardCost = ability.getEffects().stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (controllerGraveyardCost != null) {
            PendingAbilityActivation pending = gameData.pendingAbilityActivation;
            if (pending == null
                    || !source.getId().equals(pending.sourcePermanentId())
                    || choice.abilityIndex() != pending.abilityIndex()) {
                throw new IllegalStateException("No pending activated ability");
            }
            validateControllerGraveyardExileSelection(
                    gameData, player.getId(), controllerGraveyardCost, selectedIds, null);
            clearPendingAbilityActivation(gameData);
            activateAbilityInternal(
                    gameData,
                    player,
                    -1,
                    pending.abilityIndex(),
                    pending.xValue(),
                    pending.targetId(),
                    pending.targetZone(),
                    null,
                    null,
                    pending.targetIds(),
                    pending.damageAssignments(),
                    source,
                    null,
                    selectedIds,
                    null,
                    false
            );
            return;
        }
        ExileNCardsFromSingleGraveyardCost exactCost = ability.getEffects().stream()
                .filter(ExileNCardsFromSingleGraveyardCost.class::isInstance)
                .map(ExileNCardsFromSingleGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exactCost != null) {
            validateAnySingleGraveyardExileSelection(gameData, exactCost, selectedIds);
            gameData.interaction.clearAwaitingInput();
            activateAbilityInternal(
                    gameData,
                    player,
                    -1,
                    choice.abilityIndex(),
                    choice.minimumCards(),
                    choice.targetId(),
                    choice.targetZone(),
                    null,
                    null,
                    null,
                    null,
                    source,
                    null,
                    selectedIds,
                    null,
                    false
            );
            return;
        }

        ExileXCardsFromGraveyardCost cost = ability.getEffects().stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);

        CollectEvidenceCost collectEvidenceCost = ability.getEffects().stream()
                .filter(CollectEvidenceCost.class::isInstance)
                .map(CollectEvidenceCost.class::cast)
                .findFirst()
                .orElse(null);
        if (collectEvidenceCost != null) {
            UUID playerId = player.getId();
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            validateCollectEvidenceSelection(gameData, playerId, collectEvidenceCost, selectedIds);
            List<Integer> selectedIndices = selectedIds.stream()
                    .map(cardId -> indexOfCard(graveyard, cardId))
                    .toList();
            gameData.interaction.clearAwaitingInput();
            activateAbilityInternal(
                    gameData,
                    player,
                    -1,
                    choice.abilityIndex(),
                    0,
                    choice.targetId(),
                    choice.targetZone(),
                    null,
                    null,
                    null,
                    null,
                    source,
                    selectedIndices,
                    null
            );
            return;
        }

        if (cost == null) {
            throw new IllegalStateException("Activated ability no longer has the graveyard exile cost");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null && !selectedIds.isEmpty()) {
            throw new IllegalStateException("Selected graveyard cards are no longer available");
        }
        List<Integer> validIndices = collectGraveyardIndicesForType(graveyard, cost.requiredType(), null, null);
        Set<Integer> validIndexSet = new HashSet<>(validIndices);
        List<Card> selectedCards = new ArrayList<>();
        List<Integer> selectedIndices = new ArrayList<>();
        for (UUID cardId : selectedIds) {
            int index = -1;
            for (int i = 0; i < graveyard.size(); i++) {
                if (graveyard.get(i).getId().equals(cardId)) {
                    index = i;
                    break;
                }
            }
            if (index < 0 || !validIndexSet.contains(index)) {
                throw new IllegalStateException("Selected card is no longer a valid graveyard exile cost");
            }
            selectedIndices.add(index);
            selectedCards.add(graveyard.get(index));
        }

        gameData.interaction.clearAwaitingInput();

        activateAbilityInternal(
                gameData,
                player,
                -1,
                choice.abilityIndex(),
                selectedCards.size(),
                choice.targetId(),
                choice.targetZone(),
                null,
                null,
                null,
                null,
                source,
                selectedIndices,
                null
        );
    }

    public void handleActivatedAbilityGraveyardLibraryCostChosen(
            GameData gameData, Player player,
            PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice choice,
            List<UUID> cardIds) {
        PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice activeChoice =
                gameData.interaction.activeInteraction(
                        PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)) {
            throw new IllegalStateException("Not awaiting activated ability graveyard library cost choice");
        }
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> selectedIds = cardIds == null ? List.of() : List.copyOf(cardIds);
        if (selectedIds.size() < choice.minimumCards() || selectedIds.size() > choice.maximumCards()
                || new HashSet<>(selectedIds).size() != selectedIds.size()
                || !choice.validCardIds().containsAll(selectedIds)) {
            throw new IllegalStateException("Invalid graveyard card selection");
        }

        Permanent source = gameQueryService.findPermanentById(gameData, choice.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }
        ActivatedAbility ability = resolveAbility(gameData, source, choice.abilityIndex());
        PutCardsFromGraveyardOnBottomOfLibraryCost cost = ability.getEffects().stream()
                .filter(PutCardsFromGraveyardOnBottomOfLibraryCost.class::isInstance)
                .map(PutCardsFromGraveyardOnBottomOfLibraryCost.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Activated ability no longer has the graveyard library cost"));
        validatePutBottomLibraryCostSelection(gameData, player.getId(), cost, selectedIds);

        gameData.interaction.clearAwaitingInput();
        activateAbilityInternal(
                gameData,
                player,
                -1,
                choice.abilityIndex(),
                choice.xValue(),
                choice.targetId(),
                choice.targetZone(),
                null,
                null,
                null,
                null,
                source,
                null,
                null,
                null,
                false,
                null,
                null,
                selectedIds
        );
    }

    public void handleActivatedAbilityExileInstantOrSorcerySpellCostChosen(
            GameData gameData, Player player,
            PendingInteraction.ExileInstantOrSorcerySpellCostChoice choice, UUID cardId) {
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        PendingInteraction.ExileInstantOrSorcerySpellCostChoice activeChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)) {
            throw new IllegalStateException("Not awaiting instant or sorcery spell cost choice");
        }
        if (!choice.validCardIds().contains(cardId)) {
            throw new IllegalStateException("Invalid instant or sorcery spell choice");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, choice.sourcePermanentId());
        if (source == null) {
            gameData.interaction.clearAwaitingInput();
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        gameData.interaction.clearAwaitingInput();
        activateAbilityInternal(
                gameData,
                player,
                -1,
                choice.abilityIndex(),
                choice.xValue(),
                null,
                null,
                null,
                null,
                null,
                null,
                source,
                cardId
        );
    }

    public void handlePutCardExiledWithSourceIntoGraveyardCostChosen(
            GameData gameData, Player player,
            PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice choice, UUID cardId) {
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice activeChoice =
                gameData.interaction.activeInteraction(
                        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)) {
            throw new IllegalStateException("Not awaiting an exiled-card cost choice");
        }
        if (!choice.validCardIds().contains(cardId)) {
            throw new IllegalStateException("Invalid exiled-card cost choice");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, choice.sourcePermanentId());
        if (source == null) {
            gameData.interaction.clearAwaitingInput();
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        gameData.interaction.clearAwaitingInput();
        activateAbilityInternal(
                gameData,
                player,
                -1,
                choice.abilityIndex(),
                choice.xValue(),
                choice.targetId(),
                choice.targetZone(),
                null,
                null,
                choice.targetIds(),
                choice.damageAssignments(),
                source,
                null,
                null,
                null,
                false,
                null,
                cardId
        );
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         UUID exileInstantOrSorcerySpellCardId) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                null, exileInstantOrSorcerySpellCardId, false);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                null, null, exileInstantOrSorcerySpellCardId, opponentTargetAlreadyChosen);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         UUID exileInstantOrSorcerySpellCardId) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                exileXGraveyardCardIndices, null, exileInstantOrSorcerySpellCardId, false);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                exileXGraveyardCardIndices, null, exileInstantOrSorcerySpellCardId, opponentTargetAlreadyChosen);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         List<UUID> exileAnyGraveyardCardIds,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                exileXGraveyardCardIndices, exileAnyGraveyardCardIds, exileInstantOrSorcerySpellCardId,
                opponentTargetAlreadyChosen, null);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         List<UUID> exileAnyGraveyardCardIds,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen,
                                         List<UUID> revealedHandCardIds) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                exileXGraveyardCardIndices, exileAnyGraveyardCardIds, exileInstantOrSorcerySpellCardId,
                opponentTargetAlreadyChosen, revealedHandCardIds, null);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         List<UUID> exileAnyGraveyardCardIds,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen,
                                         List<UUID> revealedHandCardIds, UUID putExiledCardIntoGraveyardCardId) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                exileXGraveyardCardIndices, exileAnyGraveyardCardIds, exileInstantOrSorcerySpellCardId,
                opponentTargetAlreadyChosen, revealedHandCardIds, putExiledCardIntoGraveyardCardId, null);
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         List<UUID> exileAnyGraveyardCardIds,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen,
                                         List<UUID> revealedHandCardIds, UUID putExiledCardIntoGraveyardCardId,
                                         List<UUID> putBottomLibraryCardIds) {
        // Spell-only mana (e.g. tapped via Piracy) can't pay ability costs — hide it for the duration of
        // this activation (including the affordability check) so it is neither counted nor spent, then
        // restore it afterward. Re-entrant callbacks (discard/exile cost) call this method afresh, so each
        // pass withdraws and restores symmetrically.
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        boolean previousBlueSpendPermission = pool != null
                && pool.isBlueSpendableAsAnyColorForActivatedAbilities();
        boolean previousAllManaSpendPermission = pool != null
                && pool.isAllManaSpendableAsAnyColorForActivatedAbilities();
        if (pool != null) {
            // Refresh the "spend white as red" permission (Sunglasses of Urza) so this ability's cost
            // affordability check and payment honor it.
            pool.setWhiteSpendableAsRed(gameQueryService.canSpendWhiteManaAsRed(gameData, player.getId()));
            pool.setWhiteSpendableAsAnyColor(gameQueryService.canSpendWhiteManaAsAnyColor(gameData, player.getId()));
            pool.setWhiteSpendableAsAnyColorWithoutRestriction(
                    gameQueryService.canSpendWhiteManaAsAnyColorUntilEndOfTurn(gameData, player.getId()));
            pool.setAllManaSpendableAsAnyColor(gameQueryService.canSpendManaAsAnyColor(gameData, player.getId()));
        }
        Map<ManaColor, Integer> withheldSpellOnlyMana = pool != null ? pool.withdrawSpellOnlyMana() : Map.of();
        boolean promotedAbilityOnlyMana = pool != null && pool.promoteAbilityOnlyMana() > 0;
        boolean promotedLandAbilityOnlyMana = pool != null
                && isLandAbilitySource(gameData, player.getId(), permanentIndex, preResolvedSource)
                && pool.promoteLandAbilityOnlyMana() > 0;
        try {
            activateAbilityInternalImpl(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                    discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource,
                    exileXGraveyardCardIndices, exileAnyGraveyardCardIds,
                    exileInstantOrSorcerySpellCardId, opponentTargetAlreadyChosen, revealedHandCardIds,
                    putExiledCardIntoGraveyardCardId, putBottomLibraryCardIds);
        } finally {
            if (pool != null) {
                pool.setBlueSpendableAsAnyColorForActivatedAbilities(previousBlueSpendPermission);
                pool.setAllManaSpendableAsAnyColorForActivatedAbilities(previousAllManaSpendPermission);
            }
            if (pool != null && !withheldSpellOnlyMana.isEmpty()) {
                pool.restoreSpellOnlyMana(withheldSpellOnlyMana);
            }
            if (pool != null && promotedAbilityOnlyMana) {
                pool.restorePromotedAbilityOnlyMana();
            }
            if (pool != null && promotedLandAbilityOnlyMana) {
                pool.restorePromotedLandAbilityOnlyMana();
            }
        }
    }

    private boolean isLandAbilitySource(GameData gameData, UUID playerId, int permanentIndex,
                                        Permanent preResolvedSource) {
        if (preResolvedSource != null) {
            return gameQueryService.isLand(gameData, preResolvedSource);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && permanentIndex >= 0 && permanentIndex < battlefield.size()
                && gameQueryService.isLand(gameData, battlefield.get(permanentIndex));
    }

    private void activateAbilityInternalImpl(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource,
                                         List<Integer> exileXGraveyardCardIndices,
                                         List<UUID> exileAnyGraveyardCardIds,
                                         UUID exileInstantOrSorcerySpellCardId, boolean opponentTargetAlreadyChosen,
                                         List<UUID> revealedHandCardIds,
                                         UUID putExiledCardIntoGraveyardCardId,
                                         List<UUID> putBottomLibraryCardIds) {
        int effectiveXValue = xValue != null ? xValue : 0;

        UUID playerId = player.getId();
        Permanent permanent = preResolvedSource != null
                ? preResolvedSource
                : resolveActivationSource(gameData, playerId, permanentIndex, abilityIndex);
        if (permanent == null) {
            throw new IllegalStateException("Invalid permanent index");
        }
        ManaPool activationPool = gameData.playerManaPools.get(playerId);
        if (activationPool != null) {
            activationPool.setBlueSpendableAsAnyColorForActivatedAbilities(
                    gameQueryService.canSpendBlueManaAsAnyColorForActivatedAbilities(gameData, permanent));
            activationPool.setAllManaSpendableAsAnyColorForActivatedAbilities(
                    gameQueryService.canSpendManaAsAnyColorForActivatedAbilities(gameData, permanent));
        }
        List<ActivatedAbility> abilities = getEffectiveActivatedAbilities(gameData, permanent);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Permanent has no activated ability");
        }

        int effectiveIndex = effectiveAbilityIndex(abilityIndex);
        ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
        List<CardEffect> abilityEffects = ability.getEffects();
        String abilityCost = effectiveAbilityManaCost(gameData, permanent, ability);
        if (ability.getSourceCounterScaledTargetsType() != null) {
            effectiveXValue = permanent.getCounterCount(ability.getSourceCounterScaledTargetsType());
        }
        List<CardEffect> activationEffects = abilityEffects;
        if (ability.isModalChoiceAtActivation()) {
            if (xValue == null) {
                throw new IllegalStateException("Choose a mode for this ability");
            }
            activationEffects = EffectResolution.resolveEffects(
                    abilityEffects, null, ability.modalEffectAtActivation().decodeModeIndices(effectiveXValue).getFirst());
        }

        TapCreaturesForManaCost tapCreaturesForManaCost = abilityEffects.stream()
                .filter(TapCreaturesForManaCost.class::isInstance)
                .map(TapCreaturesForManaCost.class::cast)
                .findFirst()
                .orElse(null);
        List<UUID> creatureManaPaymentIds = tapCreaturesForManaCost == null
                ? List.of()
                : targetIds != null ? targetIds : List.of();
        if (tapCreaturesForManaCost != null) {
            targetIds = List.of();
        }

        int additionalGenericCost = getActivatedAbilityAdditionalGenericCost(
                gameData, playerId, permanent, ability, targetId, targetIds, effectiveXValue);

        // All state-based legality checks, shared with the AI's dry-run query. Nothing is mutated
        // until every check (including targeting below) has passed, so an illegal activation
        // rewinds cleanly with no cost paid (CR 602.2b/601.2c).
        // discardCardIndex < 0 means the interactive path already paid all required discards — skip
        // the discard-hand check so the re-entry does not fail after the cards left the hand.
        validateActivationLegality(gameData, playerId, permanent, ability, effectiveIndex, effectiveXValue,
                gameData.playerManaPools.get(playerId), additionalGenericCost,
                discardCardIndex != null && discardCardIndex < 0,
                tapCreaturesForManaCost == null ? null : creatureManaPaymentIds);

        if (ability.getOpponentChosenTargetIndex() >= 0 && !opponentTargetAlreadyChosen) {
            if (ability.getOpponentChosenTargetIndex() == 0) {
                if (!ability.isControllerChoosesOpponentForTarget()) {
                    throw new IllegalStateException("The target chooser must be selected by the ability's controller");
                }
                if (targetId != null || (targetIds != null && !targetIds.isEmpty())) {
                    throw new IllegalStateException("The target is chosen by an opponent");
                }

                var validTargetIds = validTargetService.computeValidTargetsForAbility(
                        gameData, permanent.getCard(), ability, playerId, permanentIndex, List.of(), effectiveXValue);
                List<UUID> validOpponentTargetIds = new ArrayList<>(validTargetIds.validPermanentIds());
                validOpponentTargetIds.addAll(validTargetIds.validPlayerIds());
                if (validOpponentTargetIds.isEmpty()) {
                    throw new IllegalStateException("No legal target");
                }

                UUID choosingPlayerId = gameData.orderedPlayerIds.stream()
                        .filter(opponentId -> !opponentId.equals(playerId))
                        .findFirst()
                        .orElse(null);
                if (choosingPlayerId == null) {
                    throw new IllegalStateException("No opponent can choose the target");
                }

                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ActivatedAbilitySoleOpponentTarget(
                                playerId, choosingPlayerId, permanent.getId(), effectiveIndex,
                                effectiveXValue, targetZone));
                if (!validTargetIds.validPlayerIds().isEmpty()) {
                    playerInputService.beginAnyTargetChoice(gameData, choosingPlayerId,
                            validTargetIds.validPermanentIds(), validTargetIds.validPlayerIds(),
                            permanent.getCard().getName() + " — choose the target.");
                } else {
                    playerInputService.beginPermanentChoice(gameData, choosingPlayerId, validOpponentTargetIds,
                            permanent.getCard().getName() + " — choose the target.");
                }
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            if (targetIds != null && !targetIds.isEmpty()) {
                throw new IllegalStateException("Choose the first target separately");
            }
            if (targetId == null || ability.getOpponentChosenTargetIndex() != 1
                    || ability.getMultiTargetFilters().isEmpty()) {
                throw new IllegalStateException("Invalid first target");
            }

            var validFirstTargets = validTargetService.computeValidTargetsForAbility(
                    gameData, permanent.getCard(), ability, playerId, permanentIndex, List.of(), effectiveXValue);
            if (!validFirstTargets.validPermanentIds().contains(targetId)
                    && !validFirstTargets.validPlayerIds().contains(targetId)) {
                throw new IllegalStateException("Invalid first target");
            }

            UUID choosingPlayerId;
            if (ability.isControllerChoosesOpponentForTarget()) {
                choosingPlayerId = gameData.orderedPlayerIds.stream()
                        .filter(opponentId -> !opponentId.equals(playerId))
                        .findFirst()
                        .orElse(null);
                if (choosingPlayerId == null) {
                    throw new IllegalStateException("No opponent can choose the second target");
                }
            } else {
                choosingPlayerId = gameQueryService.findPermanentController(gameData, targetId);
                if (choosingPlayerId == null || choosingPlayerId.equals(playerId)) {
                    throw new IllegalStateException("The first target must be controlled by an opponent");
                }
            }

            var validSecondTargets = validTargetService.computeValidTargetsForAbility(
                    gameData, permanent.getCard(), ability, playerId, permanentIndex,
                    List.of(targetId), effectiveXValue);
            List<UUID> validOpponentTargetIds = new ArrayList<>(validSecondTargets.validPermanentIds());
            validOpponentTargetIds.addAll(validSecondTargets.validPlayerIds());
            if (validOpponentTargetIds.isEmpty()) {
                throw new IllegalStateException("No legal second target");
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityOpponentTarget(
                    playerId, choosingPlayerId, permanent.getId(), effectiveIndex, effectiveXValue, targetId, targetZone));
            if (!validSecondTargets.validPlayerIds().isEmpty()) {
                playerInputService.beginAnyTargetChoice(gameData, choosingPlayerId,
                        validSecondTargets.validPermanentIds(), validSecondTargets.validPlayerIds(),
                        permanent.getCard().getName() + " — choose the second target.");
            } else {
                playerInputService.beginPermanentChoice(gameData, choosingPlayerId, validOpponentTargetIds,
                        permanent.getCard().getName() + " — choose the second target.");
            }
            mutationCoordinator.invalidateAllPlayerViews(gameData);
            return;
        }

        // Validate spell target for abilities that counter spells
        if (ability.targetsSpellOnStack(targetZone)) {
            targetLegalityService.validateSpellTargetOnStack(
                    gameData, targetId, ability.getTargetFilter(), playerId, permanent, effectiveXValue);
        }

        UUID sourceId = permanent.getId();
        final int xValueForCost = effectiveXValue;
        List<PermanentChoiceCostHandler> permanentChoiceCosts = new ArrayList<>(abilityEffects.stream()
                .map(e -> toPermanentChoiceCostHandler(gameData, e, sourceId, xValueForCost))
                .filter(Objects::nonNull)
                .toList());
        List<UUID> chosenCostPermanentIds = new ArrayList<>();
        CastingCostService.ImposedSacrificeRequirement imposedTax =
                castingCostService.getImposedSacrificeRequirementForAbility(gameData, abilityCost);
        if (!imposedTax.isEmpty()) {
            PermanentChoiceCostHandler imposedHandler = toPermanentChoiceCostHandler(
                    gameData, new SacrificeMultiplePermanentsCost(imposedTax.count(), imposedTax.filter()),
                    sourceId, xValueForCost);
            if (imposedHandler != null) {
                permanentChoiceCosts.add(imposedHandler);
            }
        }
        for (CostEffect additionalCost : castingCostService.getActivatedAbilityAdditionalCosts(gameData, permanent)) {
            PermanentChoiceCostHandler additionalHandler = toPermanentChoiceCostHandler(
                    gameData, additionalCost, sourceId, xValueForCost);
            if (additionalHandler != null) {
                permanentChoiceCosts.add(additionalHandler);
            }
        }

        RemoveTimeCounterFromPermanentOrSuspendedCardCost timeCounterCost = abilityEffects.stream()
                .filter(RemoveTimeCounterFromPermanentOrSuspendedCardCost.class::isInstance)
                .map(RemoveTimeCounterFromPermanentOrSuspendedCardCost.class::cast)
                .findFirst()
                .orElse(null);
        RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler timeCounterCostHandler =
                timeCounterCost == null ? null : new RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler(
                        gameLogService, removeTimeCounterFromExiledCardEffectHandler);

        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }
        if (timeCounterCostHandler != null) {
            timeCounterCostHandler.validateCanPay(gameData, playerId);
        }

        // The variable X of a remove-X-counters cost is announced before targets are checked
        // (CR 601.2b announces X, CR 601.2c announces targets), so this prompt has to run ahead of
        // the target legality pass below: a target filter that reads X (Quillmane Baku's "creature
        // with mana value X or less") would otherwise be evaluated against a placeholder X of 0.
        RemoveXCountersFromSourceCost pendingVariableCounterCost = abilityEffects.stream()
                .filter(RemoveXCountersFromSourceCost.class::isInstance)
                .map(RemoveXCountersFromSourceCost.class::cast)
                .findFirst()
                .orElse(null);
        if (pendingVariableCounterCost != null && xValue == null) {
            int maxCounters = permanent.getCounterCount(pendingVariableCounterCost.counterType());
            gameData.pendingAbilityCounterCostActivation = new PendingAbilityCounterCostActivation(
                    permanent.getId(), effectiveIndex, targetId, targetZone, pendingVariableCounterCost.counterType());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                    playerId, maxCounters,
                    "Choose how many " + pendingVariableCounterCost.counterType().name().toLowerCase().replace('_', ' ')
                            + " counters to remove as an activation cost.",
                    permanent.getCard().getName()));
            return;
        }

        validatePreventDividedDamageAssignments(gameData, playerId, permanent, ability, activationEffects,
                effectiveXValue, damageAssignments);
        validateDividedDamageAssignments(gameData, playerId, permanent, ability, activationEffects,
                effectiveXValue, damageAssignments);

        if (permanentChoiceCosts.isEmpty()
                && ability.getTargetFilter() != null && ability.getEffectiveMinTargets(effectiveXValue) > 0
                && targetId == null && (targetIds == null || targetIds.isEmpty())) {
            throw new IllegalStateException("Ability requires a target");
        }

        boolean selectedModeNeedsTarget = ability.isModalChoiceAtActivation()
                && EffectResolution.needsTarget(activationEffects, List.of(), false, false);
        if (selectedModeNeedsTarget && targetId == null) {
            throw new IllegalStateException("Ability requires a target");
        }

        // For regular targeting abilities, validate legality before costs are paid (CR 602.2b/601.2c).
        boolean targetsGraveyard = targetZone == Zone.GRAVEYARD && targetsGraveyardCards(ability);
        ExileArtifactsWithTotalManaValueCost exileArtifactsCost = abilityEffects.stream()
                .filter(ExileArtifactsWithTotalManaValueCost.class::isInstance)
                .map(ExileArtifactsWithTotalManaValueCost.class::cast)
                .findFirst()
                .orElse(null);
        int targetValidationXValue = effectiveXValue;
        if (exileArtifactsCost != null) {
            targetValidationXValue = Math.max(targetValidationXValue,
                    totalManaValueOfPermanents(gameData,
                            collectExileArtifactsCostCandidates(gameData, playerId, permanent)));
        }
        boolean usesTargetCardGroups = activationEffects.stream()
                .anyMatch(effect -> effect instanceof TargetCardGroupEffect groupedEffect
                        && !groupedEffect.targetGroups().isEmpty());
        if (targetsGraveyard && !usesTargetCardGroups
                && (ability.isMultiTarget() || (targetIds != null && !targetIds.isEmpty()))) {
            targetLegalityService.validateMultiTargetGraveyardAbility(
                    gameData, playerId, activationEffects,
                    targetIds != null ? targetIds : List.of(), permanent.getCard().getId(), targetValidationXValue,
                    ability.getMultiTargetConstraint());
        } else if (ability.isMultiTarget() && ability.getMaxTargets() == 1
                && (targetIds == null || targetIds.isEmpty())) {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, activationEffects, targetId, targetZone,
                    permanent.getCard(), targetValidationXValue);
        } else if (ability.isMultiTarget() || (ability.getMaxTargets() > 1 && targetIds != null)) {
                targetLegalityService.validateMultiTargetAbility(gameData, playerId, ability,
                    targetIds != null ? targetIds : List.of(), permanent.getCard(), targetValidationXValue, activationEffects);
        } else if (targetZone == Zone.GRAVEYARD && targetIds != null && !targetIds.isEmpty()) {
            targetLegalityService.validateMultiTargetGraveyardAbility(
                    gameData, playerId, activationEffects, targetIds, permanent.getCard().getId(), targetValidationXValue);
        } else {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, activationEffects, targetId, targetZone, permanent.getCard(), targetValidationXValue);
        }

        ExileNCardsFromGraveyardCost exileNFromControllerGraveyardCost = abilityEffects.stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNFromControllerGraveyardCost != null) {
            List<Card> validCards = matchingGraveyardExileCandidates(
                    gameData.playerGraveyards.get(playerId), exileNFromControllerGraveyardCost, null);
            if (validCards.size() < exileNFromControllerGraveyardCost.count()) {
                throw new IllegalStateException("Not enough matching cards in graveyard to exile");
            }
            if (exileAnyGraveyardCardIds == null
                    && validCards.size() > exileNFromControllerGraveyardCost.count()) {
                gameData.pendingAbilityActivation = new PendingAbilityActivation(
                        permanent.getId(), effectiveIndex, effectiveXValue, targetId, targetZone,
                        null, 0, null, targetIds, damageAssignments);
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                                playerId,
                                permanent.getId(),
                                effectiveIndex,
                                targetId,
                                targetZone,
                                validCards,
                                "Choose " + exileNFromControllerGraveyardCost.count()
                                        + " cards from your graveyard to exile as an activation cost.",
                                exileNFromControllerGraveyardCost.count(),
                                exileNFromControllerGraveyardCost.count(),
                                false));
                return;
            }
            if (exileAnyGraveyardCardIds != null) {
                validateControllerGraveyardExileSelection(
                        gameData, playerId, exileNFromControllerGraveyardCost,
                        exileAnyGraveyardCardIds, null);
            }
        }

        ExileNCardsFromSingleGraveyardCost exileNFromSingleGraveyardCost = abilityEffects.stream()
                .filter(ExileNCardsFromSingleGraveyardCost.class::isInstance)
                .map(ExileNCardsFromSingleGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNFromSingleGraveyardCost != null) {
            if (exileAnyGraveyardCardIds == null) {
                List<Card> validCards = collectAnySingleGraveyardExileCandidates(
                        gameData, exileNFromSingleGraveyardCost);
                if (validCards.isEmpty()) {
                    throw new IllegalStateException("Not enough matching cards in a single graveyard to exile");
                }
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                                playerId,
                                permanent.getId(),
                                effectiveIndex,
                                targetId,
                                targetZone,
                                validCards,
                                "Choose " + exileNFromSingleGraveyardCost.count()
                                        + " creature cards from a single graveyard to exile as an activation cost.",
                                exileNFromSingleGraveyardCost.count(),
                                exileNFromSingleGraveyardCost.count(),
                                true));
                return;
            }
            validateAnySingleGraveyardExileSelection(
                    gameData, exileNFromSingleGraveyardCost, exileAnyGraveyardCardIds);
        }

        PutCardsFromGraveyardOnBottomOfLibraryCost putBottomLibraryCost = abilityEffects.stream()
                .filter(PutCardsFromGraveyardOnBottomOfLibraryCost.class::isInstance)
                .map(PutCardsFromGraveyardOnBottomOfLibraryCost.class::cast)
                .findFirst()
                .orElse(null);
        if (putBottomLibraryCost != null && putBottomLibraryCardIds == null) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice(
                            playerId,
                            permanent.getId(),
                            effectiveIndex,
                            effectiveXValue,
                            targetId,
                            targetZone,
                            graveyard,
                            "Choose " + putBottomLibraryCost.count()
                                    + " cards from your graveyard to put on the bottom of your library.",
                            putBottomLibraryCost.count(),
                            putBottomLibraryCost.count()));
            return;
        }
        if (putBottomLibraryCost != null) {
            validatePutBottomLibraryCostSelection(gameData, playerId, putBottomLibraryCost,
                    putBottomLibraryCardIds);
        }

        if (ability.isExhaustAbility()) {
            gameData.playersWhoActivatedExhaustAbilityThisTurn.add(playerId);
        }

        ExileXCardsFromGraveyardCost exileXGraveyardCost = abilityEffects.stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileXGraveyardCost != null) {
            if (exileXGraveyardCardIndices == null) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                List<Integer> validIndices = collectGraveyardIndicesForType(
                        graveyard, exileXGraveyardCost.requiredType(), null, null);
                if (!validIndices.isEmpty()) {
                    List<Card> validCards = validIndices.stream().map(graveyard::get).toList();
                    interactionHandlerRegistry.begin(gameData,
                            new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                                    playerId,
                                    permanent.getId(),
                                    effectiveIndex,
                                    targetId,
                                    targetZone,
                                    validCards,
                                    "Choose any number of cards from your graveyard to exile as an activation cost.",
                                    0,
                                    validCards.size(),
                                    false));
                    return;
                }
                exileXGraveyardCardIndices = List.of();
            }
            effectiveXValue = exileXGraveyardCardIndices.size();
            if (exileXGraveyardCost.requireAtLeastOne() && effectiveXValue < 1) {
                throw new IllegalStateException("Must exile at least one card from your graveyard");
            }
        }

        CollectEvidenceCost collectEvidenceCost = abilityEffects.stream()
                .filter(CollectEvidenceCost.class::isInstance)
                .map(CollectEvidenceCost.class::cast)
                .findFirst()
                .orElse(null);
        if (collectEvidenceCost != null && exileXGraveyardCardIndices == null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<Card> validCards = graveyard == null ? List.of() : List.copyOf(graveyard);
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                            playerId,
                            permanent.getId(),
                            effectiveIndex,
                            targetId,
                            targetZone,
                            validCards,
                            "Choose cards with total mana value at least "
                                    + collectEvidenceCost.minimumManaValue()
                                    + " from your graveyard to exile as an activation cost.",
                            0,
                            validCards.size(),
                            false));
            return;
        }

        // Pay the loyalty cost only now that full legality, including targets, is confirmed
        // (CR 601.2: an illegal activation rewinds with no cost paid)
        if (ability.getLoyaltyCost() != null) {
            payLoyaltyCost(gameData, playerId, permanent, ability, effectiveXValue);
        }

        ExileCardFromGraveyardCost exileGraveyardCost = abilityEffects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileGraveyardCost != null) {
            if (exileGraveyardCost.anyGraveyard()) {
                List<Card> validCards = collectAnyGraveyardExileCandidates(gameData, exileGraveyardCost);
                if (exileAnyGraveyardCardIds == null) {
                    interactionHandlerRegistry.begin(gameData,
                            new PendingInteraction.ActivatedAbilityGraveyardExileCostChoice(
                                    playerId,
                                    permanent.getId(),
                                    effectiveIndex,
                                    targetId,
                                    targetZone,
                                    validCards,
                                    "Choose a card from a graveyard to exile as an activation cost.",
                                    1,
                                    1,
                                    false));
                    return;
                }
                if (exileAnyGraveyardCardIds.size() != 1
                        || !validCards.stream().map(Card::getId).toList().contains(exileAnyGraveyardCardIds.getFirst())) {
                    throw new IllegalStateException("Must choose a matching card from a graveyard");
                }
                Card selectedCard = validCards.stream()
                        .filter(card -> card.getId().equals(exileAnyGraveyardCardIds.getFirst()))
                        .findFirst()
                        .orElseThrow();
                if (exileGraveyardCost.payExiledCardManaCost()) {
                    abilityCost = selectedCard.getManaCost();
                }
                if (exileGraveyardCost.imprintOnSource()) {
                    gameData.setImprintedCard(permanent.getCard(), selectedCard);
                }
            } else {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, exileGraveyardCost.requiredType(),
                        exileGraveyardCost.alternateType(), exileGraveyardCost.requiredSubtype());
                if (exileGraveyardCardIndex == null) {
                    beginGraveyardExileCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                            targetIds, damageAssignments, exileGraveyardCost, validExileIndices);
                    return;
                }
                // Handle "exile and pay its mana cost" abilities (e.g. Back from the Brink)
                if (exileGraveyardCost.payExiledCardManaCost()) {
                    abilityCost = graveyard.get(exileGraveyardCardIndex).getManaCost();
                }
                if (exileGraveyardCost.imprintOnSource()) {
                    gameData.setImprintedCard(permanent.getCard(), graveyard.get(exileGraveyardCardIndex));
                }
            }
        }

        ExileInstantOrSorcerySpellCost exileInstantOrSorcerySpellCost = abilityEffects.stream()
                .filter(ExileInstantOrSorcerySpellCost.class::isInstance)
                .map(ExileInstantOrSorcerySpellCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileInstantOrSorcerySpellCost != null) {
            List<UUID> validSpellIds = collectExileInstantOrSorcerySpellIds(gameData, playerId);
            if (exileInstantOrSorcerySpellCardId == null) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileInstantOrSorcerySpellCostChoice(
                        playerId, permanent.getId(), effectiveIndex, effectiveXValue, validSpellIds));
                return;
            }
            if (!validSpellIds.contains(exileInstantOrSorcerySpellCardId)) {
                throw new IllegalStateException("Selected instant or sorcery spell is no longer on the stack");
            }
        }

        HandCardCost discardCardTypeCost = abilityEffects.stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null && discardCardTypeCost.requiredCount(effectiveXValue) > 0) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Integer> validDiscardIndices = collectDiscardIndices(gameData, playerId, hand,
                    discardCardTypeCost, effectiveXValue);
            if (discardCardIndex == null) {
                if (validDiscardIndices.size() < discardCardTypeCost.requiredCount(effectiveXValue)) {
                    String costLabel = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
                    throw new IllegalStateException("Must " + discardCardTypeCost.payVerb() + " a " + costLabel
                            + "card to activate ability");
                }
                beginDiscardCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                        targetIds, damageAssignments, discardCardTypeCost.label(), validDiscardIndices,
                        discardCardTypeCost.requiredCount(effectiveXValue),
                        discardCardTypeCost.payVerb());
                return;
            }
        }

        PutCardExiledWithSourceIntoGraveyardCost putExiledCardIntoGraveyardCost = abilityEffects.stream()
                .filter(PutCardExiledWithSourceIntoGraveyardCost.class::isInstance)
                .map(PutCardExiledWithSourceIntoGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (putExiledCardIntoGraveyardCost != null) {
            List<UUID> validExiledCardIds = gameData.getCardsExiledByPermanent(permanent.getId()).stream()
                    .map(Card::getId)
                    .toList();
            if (validExiledCardIds.isEmpty()) {
                throw new IllegalStateException("No card is exiled with this permanent");
            }
            if (putExiledCardIntoGraveyardCardId == null) {
                if (validExiledCardIds.size() > 1) {
                    interactionHandlerRegistry.begin(gameData,
                            new PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice(
                                    playerId, permanent.getId(), effectiveIndex, effectiveXValue,
                                    targetId, targetZone, targetIds, damageAssignments, validExiledCardIds));
                    return;
                }
                putExiledCardIntoGraveyardCardId = validExiledCardIds.getFirst();
            } else if (!validExiledCardIds.contains(putExiledCardIntoGraveyardCardId)) {
                throw new IllegalStateException("Selected card is no longer exiled with this permanent");
            }
        }

        HandRevealCost handRevealCost = abilityEffects.stream()
                .filter(HandRevealCost.class::isInstance)
                .map(HandRevealCost.class::cast)
                .findFirst()
                .orElse(null);
        if (handRevealCost != null) {
            if (revealedHandCardIds == null && effectiveXValue > 0) {
                List<Card> validCards = matchingHandCards(gameData, playerId, handRevealCost);
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                                playerId,
                                validCards.stream().map(Card::getId).toList(),
                                permanent.getCard().getName(),
                                null,
                                new PendingInteraction.ActivatedAbilityRevealContext(
                                        permanent.getId(), effectiveIndex, effectiveXValue, targetId, targetZone,
                                        targetIds, damageAssignments)));
                return;
            }
            if (revealedHandCardIds != null) {
                validateHandRevealSelection(gameData, playerId, handRevealCost, effectiveXValue,
                        revealedHandCardIds);
            }
        }

        Optional<RemoveCounterFromSourceCost> removeCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveCounterFromSourceCost)
                .map(e -> (RemoveCounterFromSourceCost) e)
                .findFirst();

        Optional<RemoveCounterFromGrantingPermanentCost> removeGrantingCounterCost = abilityEffects.stream()
                .filter(RemoveCounterFromGrantingPermanentCost.class::isInstance)
                .map(RemoveCounterFromGrantingPermanentCost.class::cast)
                .findFirst();

        Optional<RemoveXCountersFromSourceCost> removeXCounterCost = abilityEffects.stream()
                .filter(RemoveXCountersFromSourceCost.class::isInstance)
                .map(RemoveXCountersFromSourceCost.class::cast)
                .findFirst();

        Optional<MillControllerCost> millControllerCost = abilityEffects.stream()
                .filter(e -> e instanceof MillControllerCost)
                .map(e -> (MillControllerCost) e)
                .findFirst();

        Optional<ExileTopCardOfLibraryCost> exileTopLibraryCost = abilityEffects.stream()
                .filter(e -> e instanceof ExileTopCardOfLibraryCost)
                .map(e -> (ExileTopCardOfLibraryCost) e)
                .findFirst();

        boolean discardHandCost = abilityEffects.stream().anyMatch(e -> e instanceof DiscardHandCost);

        // Validate X for Prototype Portal-style abilities here rather than in the shared legality
        // check alone: when the same ability's exile cost just imprinted a card (re-entry after the
        // graveyard choice), the imprint only exists at this point.
        validateImprintedCopyXValue(gameData, permanent, abilityEffects, effectiveXValue);

        int creatureManaPaymentCount = creatureManaPaymentIds.size();
        if (creatureManaPaymentCount > 0) {
            payCreatureManaPayment(gameData, player, creatureManaPaymentIds);
        }

        // Pay mana cost (including targeting tax if applicable)
        WaterbendCost waterbendCost = abilityEffects.stream()
                .filter(WaterbendCost.class::isInstance)
                .map(WaterbendCost.class::cast)
                .findFirst()
                .orElse(null);
        if (waterbendCost != null) {
            payWaterbendCost(gameData, playerId, permanent, ability, waterbendCost, effectiveXValue,
                    ability.isRequiresTap());
        }
        if (abilityCost != null) {
            boolean artifactContext = gameQueryService.isArtifact(permanent);
            boolean myrContext = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            Set<CardSubtype> subtypeSpellOrAbilityContext = effectiveSpellOrAbilitySubtypes(gameData, permanent, ability);
            Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext = gameQueryService.isCreature(gameData, permanent)
                    ? subtypeSpellOrAbilityContext : Set.of();
            ManaPool payingPool = gameData.playerManaPools.get(playerId);
            EnumMap<ManaColor, Integer> manaBefore = payingPool.getAllManaTotals();
            int treasureManaBefore = payingPool.getTreasureManaTotal();
            EnumMap<ManaColor, Integer> regularManaBefore = snapshotPoolColors(payingPool);
            ManaPool.CreatureAbilityManaState promotedCreatureSourceMana = gameQueryService.isCreature(gameData, permanent)
                    ? payingPool.promoteCreatureAbilityMana()
                    : null;
            try {
                payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext,
                        subtypeSpellOrAbilityContext, subtypeCreatureSourceSpellOrAbilityContext,
                        additionalGenericCost - creatureManaPaymentCount, ability.getXColorRestrictions());
            } finally {
                if (promotedCreatureSourceMana != null) {
                    payingPool.restorePromotedCreatureAbilityMana(promotedCreatureSourceMana,
                            regularManaBefore);
                }
            }
            recordActivationManaSpent(gameData, permanent, manaBefore, payingPool.getAllManaTotals(),
                    treasureManaBefore, payingPool.getTreasureManaTotal());
        } else if (additionalGenericCost - creatureManaPaymentCount > 0) {
            // No base mana cost but targeting tax applies — pay generic mana for the tax
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int treasureManaBefore = pool.getTreasureManaTotal();
            ManaCost taxCost = new ManaCost("{" + (additionalGenericCost - creatureManaPaymentCount) + "}");
            if (gameQueryService.isCreature(gameData, permanent)) {
                EnumMap<ManaColor, Integer> regularManaBefore = snapshotPoolColors(pool);
                ManaPool.CreatureAbilityManaState promotedCreatureSourceMana = pool.promoteCreatureAbilityMana();
                try {
                    taxCost.pay(pool);
                } finally {
                    pool.restorePromotedCreatureAbilityMana(promotedCreatureSourceMana, regularManaBefore);
                }
            } else {
                taxCost.pay(pool);
            }
            gameData.abilityActivationUsedTreasureMana.put(permanent.getCard().getId(),
                    treasureManaBefore > pool.getTreasureManaTotal());
        } else {
            gameData.abilityActivationUsedTreasureMana.put(permanent.getCard().getId(), false);
        }

        if (putExiledCardIntoGraveyardCost != null) {
            payPutCardExiledWithSourceIntoGraveyardCost(
                    gameData, player, permanent, putExiledCardIntoGraveyardCardId, Zone.EXILE);
        }

        if (handRevealCost != null && revealedHandCardIds != null) {
            payHandRevealCost(gameData, playerId, handRevealCost, revealedHandCardIds);
        }

        // discardCardIndex < 0 means the interactive path already paid all required discards.
        if (discardCardTypeCost != null && discardCardIndex != null && discardCardIndex >= 0) {
            PaidHandCard paid = payDiscardCost(gameData, player, discardCardTypeCost, discardCardIndex, effectiveXValue,
                    null, permanent.getCard());
            if (discardCardTypeCost.trackManaValue()) {
                effectiveXValue = paid.manaValue();
            }
        }

        Card exiledGraveyardCardSnapshot = null;
        if (exileGraveyardCost != null) {
            if (exileGraveyardCost.anyGraveyard()) {
                exiledGraveyardCardSnapshot = payAnyGraveyardExileCost(
                        gameData, player, exileGraveyardCost, exileAnyGraveyardCardIds);
            } else {
                exiledGraveyardCardSnapshot = payGraveyardExileCost(
                        gameData, player, exileGraveyardCost, exileGraveyardCardIndex);
            }
        }

        if (exileInstantOrSorcerySpellCost != null) {
            payExileInstantOrSorcerySpellCost(gameData, player, exileInstantOrSorcerySpellCardId);
        }

        ExileNCardsFromSingleGraveyardCost exileNFromSingleGraveyardCostToPay = abilityEffects.stream()
                .filter(ExileNCardsFromSingleGraveyardCost.class::isInstance)
                .map(ExileNCardsFromSingleGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNFromSingleGraveyardCostToPay != null) {
            payAnySingleGraveyardExileCost(
                    gameData, player, exileNFromSingleGraveyardCostToPay, exileAnyGraveyardCardIds);
        }

        PutCardsFromGraveyardOnBottomOfLibraryCost putBottomLibraryCostToPay = abilityEffects.stream()
                .filter(PutCardsFromGraveyardOnBottomOfLibraryCost.class::isInstance)
                .map(PutCardsFromGraveyardOnBottomOfLibraryCost.class::cast)
                .findFirst()
                .orElse(null);
        if (putBottomLibraryCostToPay != null) {
            payPutCardsFromGraveyardOnBottomOfLibraryCost(
                    gameData, player, putBottomLibraryCostToPay, putBottomLibraryCardIds);
        }

        // Pay exile-N-cards-from-graveyard cost by exiling the front N matching cards (Immortal Coil,
        // Drudge Spell's "Exile two creature cards from your graveyard").
        ExileNCardsFromGraveyardCost exileNGraveyardCostToPay = abilityEffects.stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNGraveyardCostToPay != null) {
            if (exileAnyGraveyardCardIds == null) {
                payGraveyardExileNCost(gameData, player, exileNGraveyardCostToPay, null);
            } else {
                payChosenGraveyardExileNCost(
                        gameData, player, exileNGraveyardCostToPay, exileAnyGraveyardCardIds, null);
            }
        }

        ExileXCardsFromGraveyardCost exileXGraveyardCostToPay = abilityEffects.stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileXGraveyardCostToPay != null) {
            if (exileXGraveyardCardIndices != null) {
                payChosenGraveyardExileXCost(
                        gameData, player, exileXGraveyardCostToPay, exileXGraveyardCardIndices,
                        permanent.getId());
            } else {
                payGraveyardExileXCost(gameData, player, exileXGraveyardCostToPay, effectiveXValue, null,
                        permanent.getId());
            }
        }

        CollectEvidenceCost collectEvidenceCostToPay = abilityEffects.stream()
                .filter(CollectEvidenceCost.class::isInstance)
                .map(CollectEvidenceCost.class::cast)
                .findFirst()
                .orElse(null);
        if (collectEvidenceCostToPay != null) {
            payCollectEvidenceCost(gameData, player, collectEvidenceCostToPay,
                    exileXGraveyardCardIndices);
        }

        abilityEffects.stream()
                .filter(ExileTopCardOfGraveyardCost.class::isInstance)
                .map(ExileTopCardOfGraveyardCost.class::cast)
                .findFirst()
                .ifPresent(cost -> payTopOfGraveyardExileCost(gameData, player, cost.requiredType()));

        // Pay remove-counter cost: remove counters respecting counter type
        if (removeCounterCost.isPresent()) {
            int count = removeCounterCost.get().count();
            CounterType ct = removeCounterCost.get().counterType();
            int removedMinus = 0;
            int removedPlus = 0;
            switch (ct) {
                case MINUS_ONE_MINUS_ONE -> {
                    removedMinus = count;
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - count);
                }
                case PLUS_ONE_PLUS_ONE -> {
                    removedPlus = count;
                    permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - count);
                }
                case ANY -> {
                    removedMinus = Math.min(count, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - removedMinus);
                    int remaining = count - removedMinus;
                    if (remaining > 0) {
                        removedPlus = Math.min(remaining, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
                        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - removedPlus);
                        removeOtherPermanentCounters(gameData, permanent, remaining - removedPlus);
                    }
                }
                case SILVER -> throw new IllegalStateException("Silver counters are not on permanents");
                default -> permanent.setCounterCount(ct, permanent.getCounterCount(ct) - count);
            }
            if (ct == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, count);
            }
            String counterTypeLabel;
            if (ct == CounterType.ANY) {
                if (removedMinus > 0 && removedPlus == 0) {
                    counterTypeLabel = "-1/-1";
                } else if (removedPlus > 0 && removedMinus == 0) {
                    counterTypeLabel = "+1/+1";
                } else {
                    counterTypeLabel = "";
                }
            } else if (ct == CounterType.MINUS_ONE_MINUS_ONE) {
                counterTypeLabel = "-1/-1";
            } else if (ct == CounterType.PLUS_ONE_PLUS_ONE) {
                counterTypeLabel = "+1/+1";
            } else {
                counterTypeLabel = ct.name().toLowerCase().replace('_', ' ');
            }
            String counterWord = count == 1 ? "a " + counterTypeLabel + " counter" : count + " " + counterTypeLabel + " counters";
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " removes " + counterWord + " from ", permanent.getCard(), "."));
        }

        if (removeGrantingCounterCost.isPresent()) {
            Permanent grantingPermanent = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (grantingPermanent == null) {
                throw new IllegalStateException("The granting permanent is not on the battlefield");
            }
            int count = removeGrantingCounterCost.get().count();
            CounterType ct = removeGrantingCounterCost.get().counterType();
            int removedMinus = 0;
            int removedPlus = 0;
            switch (ct) {
                case MINUS_ONE_MINUS_ONE -> {
                    removedMinus = count;
                    grantingPermanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                            grantingPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - count);
                }
                case PLUS_ONE_PLUS_ONE -> {
                    removedPlus = count;
                    grantingPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            grantingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - count);
                }
                case ANY -> {
                    removedMinus = Math.min(count, grantingPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
                    grantingPermanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                            grantingPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - removedMinus);
                    int remaining = count - removedMinus;
                    if (remaining > 0) {
                        removedPlus = Math.min(remaining, grantingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
                        grantingPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                                grantingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - removedPlus);
                        removeOtherPermanentCounters(gameData, grantingPermanent, remaining - removedPlus);
                    }
                }
                case SILVER -> throw new IllegalStateException("Silver counters are not on permanents");
                default -> grantingPermanent.setCounterCount(ct,
                        grantingPermanent.getCounterCount(ct) - count);
            }
            if (ct == CounterType.OIL) {
                gameData.recordOilCounterRemoved(grantingPermanent, count);
            }
            String counterTypeLabel;
            if (ct == CounterType.ANY) {
                if (removedMinus > 0 && removedPlus == 0) {
                    counterTypeLabel = "-1/-1";
                } else if (removedPlus > 0 && removedMinus == 0) {
                    counterTypeLabel = "+1/+1";
                } else {
                    counterTypeLabel = "";
                }
            } else if (ct == CounterType.MINUS_ONE_MINUS_ONE) {
                counterTypeLabel = "-1/-1";
            } else if (ct == CounterType.PLUS_ONE_PLUS_ONE) {
                counterTypeLabel = "+1/+1";
            } else {
                counterTypeLabel = ct.name().toLowerCase().replace('_', ' ');
            }
            String counterWord = count == 1 ? "a " + counterTypeLabel + " counter" : count + " " + counterTypeLabel + " counters";
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " removes " + counterWord + " from ", grantingPermanent.getCard(), "."));
        }

        // Pay remove-X-counter cost (Night Dealings, Cruel Sadist): X counters of the declared type,
        // X being the activation's chosen X, which the ability's effects read back with an XValue amount.
        if (removeXCounterCost.isPresent()) {
            CounterType counterType = removeXCounterCost.get().counterType();
            int count = effectiveXValue;
            int available = permanent.getCounterCount(counterType);
            if (count < 0 || count > available) {
                throw new IllegalStateException("Not enough counters to remove (need " + count + ", have " + available + ")");
            }
            permanent.setCounterCount(counterType, available - count);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, count);
            }
            String counterLabel = counterType.name().toLowerCase().replace('_', ' ');
            String counterWord = count == 1 ? "a " + counterLabel + " counter" : count + " " + counterLabel + " counters";
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " removes " + counterWord + " from ", permanent.getCard(), "."));
        }

        Optional<RemoveOneOrMoreCountersFromSourceCost> removeOneOrMoreCountersCost = abilityEffects.stream()
                .filter(RemoveOneOrMoreCountersFromSourceCost.class::isInstance)
                .map(RemoveOneOrMoreCountersFromSourceCost.class::cast)
                .findFirst();
        if (removeOneOrMoreCountersCost.isPresent()) {
            CounterType counterType = removeOneOrMoreCountersCost.get().counterType();
            int count = effectiveXValue;
            int remaining = permanent.getCounterCount(counterType) - count;
            if (count < 1 || remaining < 0) {
                throw new IllegalStateException("Not enough counters to remove");
            }
            permanent.setCounterCount(counterType, remaining);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, count);
            }
            String counterLabel = counterType.name().toLowerCase().replace('_', ' ');
            String counterWord = count == 1 ? "a " + counterLabel + " counter" : count + " " + counterLabel + " counters";
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " removes " + counterWord + " from ", permanent.getCard(), "."));
        }

        // Pay put-counter cost: put counters on the source (e.g. "Put a -1/-1 counter on this creature: ...")
        Optional<PutCounterOnSourceCost> putCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof PutCounterOnSourceCost)
                .map(e -> (PutCounterOnSourceCost) e)
                .findFirst();
        if (putCounterCost.isPresent() && !gameQueryService.cantHaveCounters(gameData, permanent)) {
            PutCounterOnSourceCost c = putCounterCost.get();
            int placedCount = c.count();
            boolean placed = false;
            if (c.powerModifier() > 0) {
                placedCount = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, placedCount);
                if (placedCount > 0) {
                    permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placedCount);
                    permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                            gameData, permanent, playerId, placedCount);
                    placed = true;
                }
            } else if (c.powerModifier() == 0 && c.toughnessModifier() < 0) {
                // -0/-1 counters (Wall of Roots) are a different kind from -1/-1, so -1/-1 replacement effects do not apply.
                placedCount = gameQueryService.replaceCounters(
                        gameData, permanent, CounterType.MINUS_ZERO_MINUS_ONE, placedCount);
                permanent.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE) + placedCount);
                placed = placedCount > 0;
            } else if (!gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
                // Vizier of Remedies reduces the -1/-1 counters put on as a cost (e.g. Devoted Druid).
                placedCount = gameQueryService.reduceMinusOneMinusOneCounters(gameData, permanent, placedCount);
                if (placedCount > 0) {
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + placedCount);
                    placed = true;
                }
            }
            if (placed) {
                triggerCollectionService.checkYouPutCountersTriggers(gameData, playerId, placedCount);
                if (gameQueryService.isCreature(gameData, permanent)) {
                    gameData.playersWhoPutCountersOnCreaturesThisTurn.add(playerId);
                }
                String counterLabel = c.powerModifier() == 0 && c.toughnessModifier() < 0
                        ? "-0/" + c.toughnessModifier()
                        : String.format("%+d/%+d", c.powerModifier(), c.toughnessModifier());
                String counterWord = placedCount == 1 ? "a " + counterLabel + " counter" : placedCount + " " + counterLabel + " counters";
                gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " puts " + counterWord + " on ", permanent.getCard(), "."));
            }
        }

        // Pay put-typed-counter cost (e.g. "Put a verse counter on this creature: ...")
        Optional<PutTypedCounterOnSourceCost> typedCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof PutTypedCounterOnSourceCost)
                .map(e -> (PutTypedCounterOnSourceCost) e)
                .findFirst();
        if (typedCounterCost.isPresent() && !gameQueryService.cantHaveCounters(gameData, permanent)) {
            PutTypedCounterOnSourceCost c = typedCounterCost.get();
            int placedCount = gameQueryService.replaceCounters(gameData, permanent, c.counterType(), c.count());
            permanent.setCounterCount(c.counterType(), permanent.getCounterCount(c.counterType()) + placedCount);
            if (placedCount > 0) {
                triggerCollectionService.checkYouPutCountersTriggers(gameData, playerId, placedCount);
            }
            if (gameQueryService.isCreature(gameData, permanent) && placedCount > 0) {
                gameData.playersWhoPutCountersOnCreaturesThisTurn.add(playerId);
            }
            if (c.counterType() == CounterType.PLUS_ONE_PLUS_ONE && c.count() > 0) {
                gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(playerId);
            }
            String counterLabel = c.counterType().name().toLowerCase().replace('_', ' ');
            String counterWord = placedCount == 1 ? "a " + counterLabel + " counter" : placedCount + " " + counterLabel + " counters";
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " puts " + counterWord + " on ", permanent.getCard(), "."));
        }

        // Pay "tap enchanted permanent" cost (Earthlore): the Aura's target, not the Aura, taps.
        if (abilityEffects.stream().anyMatch(e -> e instanceof TapEnchantedPermanentCost)) {
            Permanent enchanted = enchantedPermanentForTapCost(gameData, permanent);
            enchanted.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, enchanted);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " taps ", enchanted.getCard(), " as a cost."));
        }

        // Pay mill-controller cost
        if (millControllerCost.isPresent()) {
            graveyardService.resolveMillPlayer(gameData, playerId, millControllerCost.get().count());
        }

        // Pay exile-top-of-library cost (e.g. Royal Herbalist)
        Card exiledTopCardSnapshot = exileTopLibraryCost
                .map(cost -> payExileTopOfLibraryCost(gameData, playerId, permanent, cost))
                .orElse(null);

        // Pay discard-your-hand cost
        if (discardHandCost) {
            payDiscardHandCost(gameData, player);
        }

        // Pay random-discard cost
        Card discardedCardSnapshot = null;
        int randomDiscardCount = abilityEffects.stream()
                .filter(DiscardRandomCardCost.class::isInstance)
                .mapToInt(effect -> ((DiscardRandomCardCost) effect).count())
                .sum();
        if (randomDiscardCount > 0) {
            discardedCardSnapshot = payRandomDiscardCost(gameData, player, randomDiscardCount);
        }

        if (abilityEffects.stream().anyMatch(e -> e instanceof RevealHandCost)) {
            cardRevealService.revealHandToAllPlayers(gameData, playerId);
        }

        // Pay reveal-two-color-sharing-cards cost: reveal a qualifying pair (cards stay in hand)
        if (abilityEffects.stream().anyMatch(e -> e instanceof RevealTwoCardsSharingColorCost)) {
            List<Card> pair = colorSharingPair(gameData.playerHands.get(playerId));
            if (pair != null) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(player.getUsername() + " reveals ")
                        .card(pair.get(0))
                        .text(" and ")
                        .card(pair.get(1))
                        .text(" as a cost.")
                        .build());
            }
        }

        if (exileArtifactsCost != null) {
            List<UUID> candidates = collectExileArtifactsCostCandidates(gameData, playerId, permanent);
            if (candidates.size() > 1) {
                playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, candidates.size(),
                        new MultiPermanentChoiceContext.ActivatedAbilityExileArtifactsCost(
                                playerId, sourceId, effectiveIndex, effectiveXValue, targetId, targetZone,
                                targetIds, damageAssignments, ability, new Permanent(permanent)),
                        permanent.getCard().getName() + " — choose one or more other artifacts to exile as a cost.");
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            int costDerivedXValue = totalManaValueOfPermanents(gameData, candidates);
            targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                    gameData, playerId, ability, activationEffects, targetId, targetZone,
                    permanent.getCard(), costDerivedXValue);
            payExileArtifactsCost(gameData, player, candidates);
            effectiveXValue = costDerivedXValue;
        }

        if (timeCounterCostHandler != null) {
            List<UUID> validCardIds = timeCounterCostHandler.validCardIds(gameData, playerId);
            if (validCardIds.isEmpty()) {
                throw new IllegalStateException(
                        "No permanent you control or suspended card you own has a time counter to remove");
            }
            if (validCardIds.size() > 1) {
                PermanentChoiceContext.ActivatedAbilityCostChoice context =
                        new PermanentChoiceContext.ActivatedAbilityCostChoice(
                                playerId, sourceId, effectiveIndex, effectiveXValue, targetId, targetZone,
                                targetIds, timeCounterCost, 1, List.of(), ability, new Permanent(permanent),
                                exiledSourceCard(gameData, permanent));
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.RemoveTimeCounterCostChoice(
                                playerId, validCardIds, context,
                                "Choose a permanent you control or suspended card you own to remove a time counter from as a cost."));
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            timeCounterCostHandler.validateAndPay(gameData, player, validCardIds.getFirst());
        }

        SacrificeAnyNumberOfPermanentsCost sacrificeAnyNumberCost = abilityEffects.stream()
                .filter(SacrificeAnyNumberOfPermanentsCost.class::isInstance)
                .map(SacrificeAnyNumberOfPermanentsCost.class::cast)
                .findFirst()
                .orElse(null);
        if (sacrificeAnyNumberCost != null && permanentChoiceCosts.isEmpty()) {
            List<UUID> candidates = collectSacrificeAnyNumberCostCandidates(
                    gameData, playerId, permanent, sacrificeAnyNumberCost);
            if (!candidates.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, candidates.size(),
                        new MultiPermanentChoiceContext.ActivatedAbilitySacrificeAnyNumberCost(
                                playerId, sourceId, effectiveIndex, effectiveXValue, targetId, targetZone,
                                targetIds, damageAssignments, ability, new Permanent(permanent), sacrificeAnyNumberCost),
                        permanent.getCard().getName() + " — choose any number of creatures to sacrifice as a cost.");
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            completeActivatedAbilitySacrificeAnyNumberCostChoice(
                    gameData, player, new MultiPermanentChoiceContext.ActivatedAbilitySacrificeAnyNumberCost(
                            playerId, sourceId, effectiveIndex, effectiveXValue, targetId, targetZone,
                            targetIds, damageAssignments, ability, new Permanent(permanent), sacrificeAnyNumberCost),
                    List.of());
            return;
        }

        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            // Capture sacrificed creature's tracked values before auto-pay (e.g. Birthing Pod, Fling)
            if (handler.costEffect() instanceof SacrificeCreatureCost sacCost
                    && (sacCost.trackSacrificedManaValue() || sacCost.trackSacrificedPower() || sacCost.trackSacrificedToughness() || sacCost.trackSacrificedColorSymbols() != null)) {
                List<UUID> autoPayIds = handler.getValidChoiceIds(gameData, playerId);
                if (autoPayIds.size() <= handler.requiredCount() && !autoPayIds.isEmpty()) {
                    Permanent autoTarget = gameQueryService.findPermanentById(gameData, autoPayIds.getFirst());
                    if (autoTarget != null) {
                        if (sacCost.trackSacrificedManaValue()) effectiveXValue = autoTarget.getCard().getManaValue();
                        if (sacCost.trackSacrificedPower()) effectiveXValue = gameQueryService.getEffectivePower(gameData, autoTarget);
                        if (sacCost.trackSacrificedToughness()) effectiveXValue = gameQueryService.getEffectiveToughness(gameData, autoTarget);
                        if (sacCost.trackSacrificedColorSymbols() != null) {
                            var mc = autoTarget.getCard().getParsedManaCost();
                            effectiveXValue = mc != null ? mc.countColorSymbols(sacCost.trackSacrificedColorSymbols()) : 0;
                        }
                    }
                }
            }
            // Same for a predicate-based sacrifice cost that scales off the sacrificed permanent
            if (handler.costEffect() instanceof SacrificePermanentCost sacPermCost
                    && (sacPermCost.trackSacrificedPower() || sacPermCost.trackSacrificedManaValue()
                            || sacPermCost.trackSacrificedToughness())) {
                List<UUID> autoPayIds = handler.getValidChoiceIds(gameData, playerId);
                if (autoPayIds.size() <= handler.requiredCount() && !autoPayIds.isEmpty()) {
                    Permanent autoTarget = gameQueryService.findPermanentById(gameData, autoPayIds.getFirst());
                    if (autoTarget != null) {
                        if (sacPermCost.trackSacrificedPower()) {
                            effectiveXValue = gameQueryService.getEffectivePower(gameData, autoTarget);
                        }
                        if (sacPermCost.trackSacrificedManaValue()) {
                            effectiveXValue = autoTarget.getCard().getManaValue();
                        }
                        if (sacPermCost.trackSacrificedToughness()) {
                            effectiveXValue = gameQueryService.getEffectiveToughness(gameData, autoTarget);
                        }
                    }
                }
            }
            if (handler.costEffect() instanceof ExilePermanentCost exilePermCost
                    && exilePermCost.trackExiledManaValue()) {
                List<UUID> autoPayIds = handler.getValidChoiceIds(gameData, playerId);
                if (autoPayIds.size() <= handler.requiredCount() && !autoPayIds.isEmpty()) {
                    Permanent autoTarget = gameQueryService.findPermanentById(gameData, autoPayIds.getFirst());
                    if (autoTarget != null) {
                        effectiveXValue = autoTarget.getCard().getManaValue();
                    }
                }
            }
            // Remember the auto-tapped creature so ChosenPermanentPower can read its power at
            // resolution (Impelled Giant). Only the single-valid-choice case auto-pays here;
            // multi-choice payment records the pick in completeActivatedAbilityCostChoice.
            if (handler.costEffect() instanceof TapCreatureCost tapCost && tapCost.trackTappedCreaturePower()) {
                List<UUID> tapChoiceIds = handler.getValidChoiceIds(gameData, playerId);
                if (tapChoiceIds.size() == 1) {
                    permanent.setChosenPermanentId(tapChoiceIds.getFirst());
                }
            }
            if (handlePermanentChoiceCost(gameData, player, permanent, ability, abilityEffects, effectiveIndex,
                    effectiveXValue, targetId, targetZone, targetIds, handler, chosenCostPermanentIds)) {
                return;
            }
        }

        CraftMaterialCost craftMaterialCost = abilityEffects.stream()
                .filter(CraftMaterialCost.class::isInstance)
                .map(CraftMaterialCost.class::cast)
                .findFirst()
                .orElse(null);
        if (craftMaterialCost != null) {
            List<Card> candidates = validateCraftMaterialCost(
                    gameData, playerId, permanent, craftMaterialCost);
            if (candidates.size() > craftMaterialCost.minimumCount()) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.CraftMaterialChoice(
                        playerId, permanent.getId(), effectiveIndex, effectiveXValue, targetId, targetZone,
                        targetIds, damageAssignments, candidates,
                        craftMaterialCost.minimumCount(), craftMaterialCost.allowsAdditionalMaterials()
                                ? candidates.size() : craftMaterialCost.minimumCount(),
                        permanent.getCard().getName() + " - choose " + craftMaterialPrompt(craftMaterialCost) + "."));
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            payCraftMaterialCost(gameData, player, permanent,
                    candidates.stream().map(Card::getId).toList(), craftMaterialCost);
        }

        boolean nonTargeting = !ability.isNeedsSpellTarget()
                && !EffectResolution.needsTarget(activationEffects, List.of(), false, false);
        UUID resolutionTargetId = targetId;
        Zone resolutionTargetZone = targetZone;
        completeActivationAndRecordWithChosenPermanents(gameData, player, permanent, ability, activationEffects,
                effectiveXValue, resolutionTargetId, resolutionTargetZone, nonTargeting, effectiveIndex,
                targetIds, damageAssignments, chosenCostPermanentIds, discardedCardSnapshot,
                exiledTopCardSnapshot != null ? exiledTopCardSnapshot : exiledGraveyardCardSnapshot);
    }

    private void validatePreventDividedDamageAssignments(GameData gameData, UUID playerId,
                                                         Permanent sourcePermanent, ActivatedAbility ability,
                                                         List<CardEffect> abilityEffects, int xValue,
                                                         Map<UUID, Integer> damageAssignments) {
        PreventDividedDamageEffect prevention = abilityEffects.stream()
                .filter(PreventDividedDamageEffect.class::isInstance)
                .map(PreventDividedDamageEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (prevention == null) {
            return;
        }

        Map<UUID, Integer> assignments = damageAssignments == null ? Map.of() : damageAssignments;
        int expectedAmount = amountEvaluationService.evaluate(gameData, prevention.amount(),
                new AmountContext(playerId, sourcePermanent, null, xValue, 0));
        int assignedAmount = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (assignedAmount != expectedAmount) {
            throw new IllegalStateException("Prevention assignments must sum to " + expectedAmount);
        }
        if (assignments.size() > expectedAmount) {
            throw new IllegalStateException("Too many targets");
        }
        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            if (assignment.getValue() == null || assignment.getValue() <= 0) {
                throw new IllegalStateException("Each prevention assignment must be positive");
            }
            targetLegalityService.validateActivatedAbilityTargeting(gameData, playerId, ability, abilityEffects,
                    assignment.getKey(), null, sourcePermanent.getCard(), xValue);
        }
    }

    private void validateDividedDamageAssignments(GameData gameData, UUID playerId,
                                                   Permanent sourcePermanent, ActivatedAbility ability,
                                                   List<CardEffect> abilityEffects, int xValue,
                                                   Map<UUID, Integer> damageAssignments) {
        DealDividedDamageEffect dividedDamage = abilityEffects.stream()
                .filter(DealDividedDamageEffect.class::isInstance)
                .map(DealDividedDamageEffect.class::cast)
                .filter(effect -> effect.mode() == DivisionMode.CHOSEN && !effect.etbAssignments())
                .findFirst()
                .orElse(null);
        if (dividedDamage == null) {
            return;
        }

        Map<UUID, Integer> assignments = damageAssignments == null ? Map.of() : damageAssignments;
        int expectedAmount = amountEvaluationService.evaluate(gameData, dividedDamage.totalDamage(),
                new AmountContext(playerId, sourcePermanent, null, xValue, 0));
        int assignedAmount = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (assignedAmount != expectedAmount) {
            throw new IllegalStateException("Damage assignments must sum to " + expectedAmount);
        }
        if (dividedDamage.maxTargets() > 0 && assignments.size() > dividedDamage.maxTargets()) {
            throw new IllegalStateException("Too many targets");
        }
        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            if (assignment.getValue() == null || assignment.getValue() <= 0) {
                throw new IllegalStateException("Each damage assignment must be positive");
            }
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, List.of(dividedDamage), assignment.getKey(), null,
                    sourcePermanent.getCard(), xValue);
        }
    }

    PermanentChoiceCostHandler toPermanentChoiceCostHandler(GameData gameData, CardEffect effect,
                                                            UUID sourcePermanentId, int xValue) {
        return toPermanentChoiceCostHandler(gameData, effect, sourcePermanentId, xValue, List.of());
    }

    PermanentChoiceCostHandler toPermanentChoiceCostHandler(GameData gameData, CardEffect effect,
                                                            UUID sourcePermanentId, int xValue,
                                                            List<UUID> chosenSoFar) {
        PermanentSacrificeAction sacAction = this::sacrificePermanentAsCost;
        PermanentExileAction exileAction = this::exilePermanentAsCost;
        PermanentBounceAction bounceAction = this::returnPermanentToHandAsCost;
        if (effect instanceof SacrificeCreatureCost c) return new CreatureSacrificeCostHandler(c, gameQueryService, sacAction, sourcePermanentId);
        if (effect instanceof SacrificePermanentCost c) return new MultiplePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction, sourcePermanentId);
        if (effect instanceof ExilePermanentCost c) return new MultiplePermanentExileCostHandler(c, predicateEvaluationService, exileAction, sourcePermanentId);
        if (effect instanceof SacrificeMultiplePermanentsCost c) return new MultiplePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction);
        if (effect instanceof SacrificeDistinctNamePermanentsCost c) return new DistinctNamePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction, chosenSoFar);
        if (effect instanceof SacrificeAllMatchingPermanentsCost c) return new AllMatchingPermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction);
        if (effect instanceof SacrificePermanentsSequenceCost c) return new SequencePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction, chosenSoFar, sourcePermanentId);
        if (effect instanceof ReturnMultiplePermanentsToHandCost c) return new MultiplePermanentReturnToHandCostHandler(c, predicateEvaluationService, bounceAction);
        if (effect instanceof TapCreatureCost c) return new TapCreatureCostHandler(c, gameQueryService, predicateEvaluationService, gameLogService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof TapMultiplePermanentsCost c) return new MultiplePermanentTapCostHandler(c, tapCostSupport.requiredCount(gameData, c, sourcePermanentId, xValue), predicateEvaluationService, gameLogService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof UntapMultiplePermanentsCost c) return new MultiplePermanentUntapCostHandler(
                c, predicateEvaluationService, gameLogService, gameQueryService, sourcePermanentId);
        if (effect instanceof SacrificeXPermanentsCost c) return new SacrificeXPermanentsCostHandler(
                c, xValue, predicateEvaluationService, sacAction, sourcePermanentId);
        if (effect instanceof TapTwoCreaturesSharingTypeCost c) return new TapTwoSharingCreatureTypeCostHandler(c, gameQueryService, gameLogService, triggerCollectionService, chosenSoFar);
        if (effect instanceof PowerBasedTapCost c) return new CrewCostHandler(c, gameQueryService, gameLogService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof RemoveCounterFromControlledPermanentCost c) return new RemoveCounterFromPermanentCostHandler(
                c, gameLogService, predicateEvaluationService, sourcePermanentId);
        if (effect instanceof RemoveCounterFromControlledCreatureCost c) return new RemoveCounterFromCreatureCostHandler(c, gameQueryService, gameLogService);
        if (effect instanceof RemoveOneOrMoreCountersFromControlledCreaturesCost c) return new RemoveCounterFromCreatureCostHandler(c, xValue, gameQueryService, gameLogService);
        if (effect instanceof PutCounterOnControlledCreatureCost c) return new PutCounterOnCreatureCostHandler(c, gameQueryService, gameLogService);
        return null;
    }

    /**
     * Resolves the permanent a {@link TapEnchantedPermanentCost} taps — the one the source Aura is
     * attached to — and asserts it can still be tapped. Throws when the Aura is unattached, the
     * enchanted permanent has left the battlefield, or it is already tapped.
     */
    private Permanent enchantedPermanentForTapCost(GameData gameData, Permanent aura) {
        Permanent enchanted = aura.isAttached() ? gameQueryService.findPermanentById(gameData, aura.getAttachedTo()) : null;
        if (enchanted == null) {
            throw new IllegalStateException(aura.getCard().getName() + " is not attached to a permanent");
        }
        if (enchanted.isTapped()) {
            throw new IllegalStateException(enchanted.getCard().getName() + " is already tapped");
        }
        return enchanted;
    }

    /**
     * Remembers the permanent untapped to pay a single-permanent {@link UntapMultiplePermanentsCost}
     * so {@code AwardManaOfTypeUntappedLandCouldProduceEffect} can read which land was untapped
     * (Benthic Explorers).
     */
    private void recordUntappedCostPermanent(CardEffect costEffect, Permanent source, UUID chosenPermanentId) {
        if (costEffect instanceof UntapMultiplePermanentsCost untapCost && untapCost.count() == 1) {
            source.setChosenPermanentId(chosenPermanentId);
        }
    }

    private void recordTappedCostPermanent(CardEffect costEffect, Permanent source, UUID chosenPermanentId) {
        if (costEffect instanceof TapCreatureCost tapCost
                && tapCost.trackTappedCreatureForSourceAbility()) {
            source.recordTappedPermanentForAbility(chosenPermanentId);
        }
    }

    /**
     * Remembers the land sacrificed to pay a {@link SacrificePermanentCost} when the ability adds
     * mana of a type that land could produce (Squandered Resources) or grants landwalk of its land
     * types (Excavator). The land's card is stored on the source so the effect can still read it
     * after the land has left the battlefield.
     */
    private void recordSacrificedLandCard(CardEffect costEffect, Permanent source, int abilityIndex,
                                          Permanent sacrificed) {
        if (costEffect instanceof CostEffect cost && cost.tracksSacrificedCard() && sacrificed != null) {
            source.setChosenCard(sacrificed.getCard());
        }
        if (costEffect instanceof SacrificeCreatureCost creatureCost
                && creatureCost.recordSacrificedPermanentSnapshot() && sacrificed != null) {
            source.setChosenSacrificedPermanentSnapshot(new Permanent(sacrificed));
        }
        if (!(costEffect instanceof SacrificePermanentCost)
                || sacrificed == null
                || !sacrificed.getCard().hasType(CardType.LAND)) {
            return;
        }
        List<ActivatedAbility> abilities = source.getCard().getActivatedAbilities();
        if (abilityIndex < 0 || abilityIndex >= abilities.size()) {
            return;
        }
        if (abilities.get(abilityIndex).getEffects().stream()
                .anyMatch(e -> e instanceof AwardManaOfTypeSacrificedLandCouldProduceEffect
                        || e instanceof GrantLandwalkOfSacrificedLandToTargetEffect)) {
            source.setChosenCard(sacrificed.getCard());
        }
    }

    private boolean handlePermanentChoiceCost(GameData gameData, Player player, Permanent source,
                                               ActivatedAbility ability, List<CardEffect> abilityEffects,
                                               int abilityIndex, int xValue, UUID targetId, Zone targetZone,
                                               List<UUID> targetIds,
                                               PermanentChoiceCostHandler handler,
                                               List<UUID> chosenCostPermanentIds) {
        int required = handler.requiredCount();
        if (required <= 0) return false;
        UUID playerId = player.getId();
        boolean tapBatch = handler.costEffect() instanceof TapMultiplePermanentsCost;
        if (tapBatch) {
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        }
        if (handler.shouldAutoPayAll(gameData, playerId, required)) {
            try {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                for (UUID id : validIds) {
                    Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                    if (chosen != null) {
                        Integer costDerivedXValue = trackedSacrificedManaValue(handler.costEffect(), chosen);
                        if (costDerivedXValue != null) {
                            targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                                    gameData, player.getId(), ability, abilityEffects, targetId, targetZone,
                                    source.getCard(), costDerivedXValue);
                        }
                        recordSacrificedLandCard(handler.costEffect(), source, abilityIndex, chosen);
                        handler.validateAndPay(gameData, player, chosen);
                        recordUntappedCostPermanent(handler.costEffect(), source, chosen.getId());
                        recordTappedCostPermanent(handler.costEffect(), source, chosen.getId());
                        if (handler.costEffect() instanceof CostEffect cost && cost.tracksChosenPermanents()) {
                            chosenCostPermanentIds.add(id);
                        }
                    }
                }
            } finally {
                if (tapBatch) {
                    triggerCollectionService.endPermanentTapTriggerBatch(gameData);
                }
            }
            return false;
        }
        List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                playerId, source.getId(), abilityIndex, xValue, targetId, targetZone,
                targetIds, handler.costEffect(), required, List.of(), ability, new Permanent(source),
                exiledSourceCard(gameData, source)));
        playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                handler.getPromptMessage(required));
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        return true;
    }

    private Card exiledSourceCard(GameData gameData, Permanent source) {
        return gameData.findExiledCard(source.getCard().getId()) != null ? source.getCard() : null;
    }

    /**
     * Callback for when a player has chosen a permanent for an activated ability's permanent-choice cost
     * (sacrifice subtype, sacrifice artifact, sacrifice multiple, or tap creature). Validates the choice,
     * pays the cost, and either re-prompts for additional choices or completes the ability activation.
     */
    public void completeActivatedAbilityTimeCounterCostChoice(
            GameData gameData, Player player,
            PermanentChoiceContext.ActivatedAbilityCostChoice context,
            UUID chosenCardId) {
        if (!player.getId().equals(context.activatingPlayerId())) {
            throw new IllegalStateException("Not your choice");
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null && context.sourcePermanentSnapshot() != null) {
            sourcePermanent = new Permanent(context.sourcePermanentSnapshot());
        }
        if (sourcePermanent == null && context.sourceCard() != null) {
            sourcePermanent = new Permanent(context.sourceCard());
        }
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, sourcePermanent, context.abilityIndex());
        List<CardEffect> abilityEffects = ability.getEffects();
        RemoveTimeCounterFromPermanentOrSuspendedCardCost cost = abilityEffects.stream()
                .filter(RemoveTimeCounterFromPermanentOrSuspendedCardCost.class::isInstance)
                .map(RemoveTimeCounterFromPermanentOrSuspendedCardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (cost == null) {
            throw new IllegalStateException("Activated ability no longer has the required cost");
        }
        RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler handler =
                new RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler(
                        gameLogService, removeTimeCounterFromExiledCardEffectHandler);
        if (!handler.validCardIds(gameData, player.getId()).contains(chosenCardId)) {
            throw new IllegalStateException("Chosen permanent or suspended card is no longer valid");
        }
        handler.validateAndPay(gameData, player, chosenCardId);
        gameData.interaction.clearAwaitingInput();
        boolean nonTargeting = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget();
        completeActivationAndRecord(gameData, player, sourcePermanent, ability, abilityEffects,
                context.xValue() != null ? context.xValue() : 0,
                context.targetId(), context.targetZone(), nonTargeting,
                effectiveAbilityIndex(context.abilityIndex()), context.targetIds());
    }

    public void completeActivatedAbilityCostChoice(GameData gameData, Player player,
                                                    PermanentChoiceContext.ActivatedAbilityCostChoice context,
                                                    UUID chosenPermanentId) {
        UUID playerId = player.getId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null && context.sourcePermanentSnapshot() != null) {
            sourcePermanent = new Permanent(context.sourcePermanentSnapshot());
        }
        if (sourcePermanent == null && context.sourceCard() != null) {
            sourcePermanent = new Permanent(context.sourceCard());
        }
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        boolean tapBatch = context.costEffect() instanceof TapMultiplePermanentsCost;
        if (tapBatch && gameData.permanentTapTriggerBatchDepth == 0) {
            triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        }

        int effectiveIndex = effectiveAbilityIndex(context.abilityIndex());
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, sourcePermanent, context.abilityIndex());
        List<CardEffect> abilityEffects = ability.getEffects();
        if (!abilityEffects.contains(context.costEffect())) {
            if (!(context.costEffect() instanceof CostEffect)) {
                throw new IllegalStateException("Activated ability no longer has the required cost");
            }
        }

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(gameData, context.costEffect(), context.sourcePermanentId(), context.xValue(), context.chosenSoFar());
        if (handler == null) {
            throw new IllegalStateException("Unknown cost effect type");
        }
        boolean tracksChosenPermanents = context.costEffect() instanceof CostEffect cost
                && cost.tracksChosenPermanents();
        List<UUID> chosenCostPermanentIds = tracksChosenPermanents
                ? new ArrayList<>(context.chosenSoFar() == null ? List.of() : context.chosenSoFar())
                : new ArrayList<>();

        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        boolean opponentControlledCost = context.costEffect() instanceof UntapMultiplePermanentsCost untapCost
                && untapCost.opponentControlled();
        if (!opponentControlledCost) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || !battlefield.contains(chosen)) {
                throw new IllegalStateException("Must choose a permanent you control");
            }
        }

        // Capture sacrificed creature's tracked values before sacrifice (e.g. Birthing Pod, Fling)
        Integer updatedXValue = null;
        if (context.costEffect() instanceof SacrificeCreatureCost sacCost) {
            if (sacCost.trackSacrificedManaValue()) {
                updatedXValue = chosen.getCard().getManaValue();
            }
            if (sacCost.trackSacrificedPower()) {
                updatedXValue = gameQueryService.getEffectivePower(gameData, chosen);
            }
            if (sacCost.trackSacrificedToughness()) {
                updatedXValue = gameQueryService.getEffectiveToughness(gameData, chosen);
            }
            if (sacCost.trackSacrificedColorSymbols() != null) {
                var mc = chosen.getCard().getParsedManaCost();
                updatedXValue = mc != null ? mc.countColorSymbols(sacCost.trackSacrificedColorSymbols()) : 0;
            }
        }

        if (context.costEffect() instanceof SacrificePermanentCost sacPermCost) {
            if (sacPermCost.trackSacrificedPower()) {
                updatedXValue = gameQueryService.getEffectivePower(gameData, chosen);
            }
            if (sacPermCost.trackSacrificedManaValue()) {
                updatedXValue = chosen.getCard().getManaValue();
            }
            if (sacPermCost.trackSacrificedToughness()) {
                updatedXValue = gameQueryService.getEffectiveToughness(gameData, chosen);
            }
        }

        if (context.costEffect() instanceof ExilePermanentCost exilePermCost
                && exilePermCost.trackExiledManaValue()) {
            updatedXValue = chosen.getCard().getManaValue();
        }

        // Remember the tapped creature so ChosenPermanentPower reads its power at resolution (Impelled Giant).
        if (context.costEffect() instanceof TapCreatureCost tapCost && tapCost.trackTappedCreaturePower()) {
            sourcePermanent.setChosenPermanentId(chosenPermanentId);
        }
        Integer costDerivedXValue = trackedSacrificedManaValue(context.costEffect(), chosen);
        if (costDerivedXValue != null) {
            targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                    gameData, playerId, ability, abilityEffects, context.targetId(), context.targetZone(),
                    sourcePermanent.getCard(), costDerivedXValue);
        }
        recordUntappedCostPermanent(context.costEffect(), sourcePermanent, chosenPermanentId);
        recordSacrificedLandCard(context.costEffect(), sourcePermanent, effectiveIndex, chosen);

        handler.validateAndPay(gameData, player, chosen);
        if (tracksChosenPermanents) {
            chosenCostPermanentIds.add(chosenPermanentId);
        }
        recordTappedCostPermanent(context.costEffect(), sourcePermanent, chosenPermanentId);

        int remaining = context.remaining() - handler.lastPaymentWeight();
        // Costs whose valid choices depend on prior picks (e.g. tap two creatures sharing a type)
        // need the just-paid permanent threaded into the handler for the remaining choices.
        List<UUID> chosenSoFar = new ArrayList<>(context.chosenSoFar());
        chosenSoFar.add(chosenPermanentId);
        handler = toPermanentChoiceCostHandler(gameData, context.costEffect(), context.sourcePermanentId(), context.xValue(), chosenSoFar);
        if (remaining > 0) {
            if (!handler.canPayRemaining(gameData, playerId, remaining)) {
                throw new IllegalStateException("Not enough permanents remaining");
            }
            if (handler.shouldAutoPayAll(gameData, playerId, remaining)) {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                for (UUID id : validIds) {
                    Permanent autoPay = gameQueryService.findPermanentById(gameData, id);
                    if (autoPay != null) {
                        handler.validateAndPay(gameData, player, autoPay);
                        if (tracksChosenPermanents) {
                            chosenCostPermanentIds.add(autoPay.getId());
                        }
                        recordUntappedCostPermanent(context.costEffect(), sourcePermanent, autoPay.getId());
                        recordTappedCostPermanent(context.costEffect(), sourcePermanent, autoPay.getId());
                    }
                }
            } else {
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                        playerId, context.sourcePermanentId(), context.abilityIndex(), context.xValue(),
                        context.targetId(), context.targetZone(), context.targetIds(), context.costEffect(), remaining,
                        chosenSoFar, ability, new Permanent(sourcePermanent), context.sourceCard()));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
        }

        if (tapBatch) {
            triggerCollectionService.endPermanentTapTriggerBatch(gameData);
        }

        int finalXValue = updatedXValue != null ? updatedXValue : (context.xValue() != null ? context.xValue() : 0);
        boolean nonTargeting = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget();
        completeActivationAndRecordWithChosenPermanents(gameData, player, sourcePermanent, ability, abilityEffects,
                finalXValue, context.targetId(), context.targetZone(), nonTargeting, effectiveIndex,
                context.targetIds(), null, chosenCostPermanentIds, null, null);
    }

    public void validateActivatedAbilityExileArtifactsChoice(
            GameData gameData, MultiPermanentChoiceContext.ActivatedAbilityExileArtifactsCost context,
            List<UUID> permanentIds) {
        if (permanentIds == null || permanentIds.isEmpty()) {
            throw new IllegalStateException("At least one artifact must be selected");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null && context.sourcePermanentSnapshot() != null) {
            source = new Permanent(context.sourcePermanentSnapshot());
        }
        if (source == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }
        for (UUID permanentId : permanentIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
            if (chosen == null
                    || !context.playerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                    || permanentId.equals(source.getId())
                    || !gameQueryService.isArtifact(gameData, chosen)) {
                throw new IllegalStateException("A selected permanent is no longer another artifact you control");
            }
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null && context.sourcePermanentSnapshot() != null) {
            sourcePermanent = new Permanent(context.sourcePermanentSnapshot());
        }
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, sourcePermanent, context.abilityIndex());
        targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                gameData, context.playerId(), ability, ability.getEffects(), context.targetId(), context.targetZone(),
                sourcePermanent.getCard(), totalManaValueOfPermanents(gameData, permanentIds));
    }

    public void completeActivatedAbilityExileArtifactsCostChoice(
            GameData gameData, Player player,
            MultiPermanentChoiceContext.ActivatedAbilityExileArtifactsCost context,
            List<UUID> permanentIds) {
        validateActivatedAbilityExileArtifactsChoice(gameData, context, permanentIds);
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null && context.sourcePermanentSnapshot() != null) {
            source = new Permanent(context.sourcePermanentSnapshot());
        }
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, source, context.abilityIndex());
        List<CardEffect> abilityEffects = ability.getEffects();
        if (abilityEffects.stream().noneMatch(ExileArtifactsWithTotalManaValueCost.class::isInstance)) {
            throw new IllegalStateException("Activated ability no longer has the required cost");
        }
        int costDerivedXValue = totalManaValueOfPermanents(gameData, permanentIds);
        targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                gameData, player.getId(), ability, abilityEffects, context.targetId(), context.targetZone(),
                source.getCard(), costDerivedXValue);
        payExileArtifactsCost(gameData, player, permanentIds);
        boolean nonTargeting = !ability.isNeedsSpellTarget()
                && !EffectResolution.needsTarget(abilityEffects, List.of(), false, false);
        completeActivationAndRecord(gameData, player, source, ability, abilityEffects, costDerivedXValue,
                context.targetId(), context.targetZone(), nonTargeting, effectiveAbilityIndex(context.abilityIndex()),
                context.targetIds(), context.damageAssignments());
    }

    public void validateActivatedAbilitySacrificeAnyNumberChoice(
            GameData gameData, MultiPermanentChoiceContext.ActivatedAbilitySacrificeAnyNumberCost context,
            List<UUID> permanentIds) {
        if (permanentIds == null) {
            throw new IllegalStateException("Invalid sacrifice selection");
        }
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null && context.sourcePermanentSnapshot() != null) {
            source = new Permanent(context.sourcePermanentSnapshot());
        }
        if (source == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }
        Set<UUID> uniqueIds = new HashSet<>(permanentIds);
        if (uniqueIds.size() != permanentIds.size()) {
            throw new IllegalStateException("Duplicate permanent IDs in selection");
        }
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, source, context.abilityIndex());
        SacrificeAnyNumberOfPermanentsCost cost = ability.getEffects().stream()
                .filter(SacrificeAnyNumberOfPermanentsCost.class::isInstance)
                .map(SacrificeAnyNumberOfPermanentsCost.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Activated ability no longer has the required sacrifice cost"));
        FilterContext filterContext = FilterContext.of(gameData).withSourcePermanentId(source.getId());
        for (UUID permanentId : permanentIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
            if (chosen == null
                    || !context.playerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                    || (cost.excludeSource() && permanentId.equals(source.getId()))
                    || !predicateEvaluationService.matchesPermanentPredicate(chosen, cost.filter(), filterContext)) {
                throw new IllegalStateException("A selected permanent is no longer a matching permanent you control");
            }
        }
    }

    public void completeActivatedAbilitySacrificeAnyNumberCostChoice(
            GameData gameData, Player player,
            MultiPermanentChoiceContext.ActivatedAbilitySacrificeAnyNumberCost context,
            List<UUID> permanentIds) {
        validateActivatedAbilitySacrificeAnyNumberChoice(gameData, context, permanentIds);
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (source == null && context.sourcePermanentSnapshot() != null) {
            source = new Permanent(context.sourcePermanentSnapshot());
        }
        ActivatedAbility ability = context.ability() != null
                ? context.ability()
                : resolveAbility(gameData, source, context.abilityIndex());
        List<CardEffect> abilityEffects = ability.getEffects();
        SacrificeAnyNumberOfPermanentsCost cost = abilityEffects.stream()
                .filter(SacrificeAnyNumberOfPermanentsCost.class::isInstance)
                .map(SacrificeAnyNumberOfPermanentsCost.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Activated ability no longer has the required sacrifice cost"));
        int totalPower = permanentIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .sum();
        int costDerivedXValue = cost.trackSacrificedPower()
                ? Math.max(0, totalPower)
                : permanentIds.size();
        List<UUID> sacrificedCardIds = permanentIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .map(permanent -> permanent.getCard().getId())
                .toList();
        targetLegalityService.validateActivatedAbilityTargetingAfterCostSelection(
                gameData, player.getId(), ability, abilityEffects, context.targetId(), context.targetZone(),
                source.getCard(), costDerivedXValue);
        for (UUID permanentId : permanentIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
            sacrificePermanentAsCost(gameData, player, chosen);
        }
        boolean nonTargeting = !ability.isNeedsSpellTarget()
                && !EffectResolution.needsTarget(abilityEffects, List.of(), false, false);
        completeActivationAndRecord(gameData, player, source, ability, abilityEffects, costDerivedXValue,
                context.targetId(), context.targetZone(), nonTargeting, effectiveAbilityIndex(context.abilityIndex()),
                context.targetIds(), context.damageAssignments(), sacrificedCardIds);
    }

    /**
     * Returns the complete list of activated abilities currently available on a permanent: its own
     * printed abilities (unless it has lost all abilities), abilities granted by static effects,
     * and temporary/until-next-turn abilities. The list order defines the {@code abilityIndex}
     * used by {@code activateAbility}.
     */
    public List<ActivatedAbility> getEffectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities;
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                || permanent.isFaceDown()) {
            // Permanent has lost all its own abilities; only static-granted abilities remain
            abilities = new ArrayList<>(staticBonus.grantedActivatedAbilities());
        } else if (staticBonus.losesAllNonManaAbilities()) {
            abilities = permanent.getCard().getActivatedAbilities().stream()
                    .filter(AbilityActivationService::isManaAbility)
                    .collect(Collectors.toCollection(ArrayList::new));
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        return abilities;
    }

    private String effectiveAbilityManaCost(GameData gameData, Permanent permanent, ActivatedAbility ability) {
        if (!ability.isManaCostOfEnchantedPermanent()) {
            return ability.getManaCost();
        }
        if (!permanent.isAttached()) {
            return null;
        }
        Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
        return enchantedPermanent == null ? null : enchantedPermanent.getCard().getManaCost();
    }

    /**
     * Resolves the source permanent for an activation. {@code permanentIndex} is an index into the
     * activating player's own battlefield for the common case (a player activating an ability of a
     * permanent they control). If it doesn't resolve there, the ability may be one that
     * "any player may activate" (e.g. Oona's Prowler): the index is then interpreted against every
     * other player's battlefield, and only a permanent whose ability at {@code abilityIndex} is
     * flagged {@link ActivatedAbility#isActivatableByAnyPlayer()} is accepted. Returns {@code null}
     * if nothing legal is found.
     */
    private Permanent resolveActivationSource(GameData gameData, UUID activatorId, int permanentIndex, Integer abilityIndex) {
        int idx = abilityIndex != null ? abilityIndex : 0;
        List<Permanent> own = gameData.playerBattlefields.get(activatorId);
        Permanent ownCandidate = null;
        if (own != null && permanentIndex >= 0 && permanentIndex < own.size()) {
            ownCandidate = own.get(permanentIndex);
            List<ActivatedAbility> abilities = getEffectiveActivatedAbilities(gameData, ownCandidate);
            if (idx >= 0 && idx < abilities.size()) {
                return ownCandidate;
            }
            // A printed ability can be absent from this list because the permanent has lost its
            // abilities, and legacy tap-for-mana abilities live in ON_TAP rather than this list.
            // Keep that permanent as the intended source so activation fails locally rather than
            // selecting an opponent's unrelated "any player may activate" permanent at the same
            // battlefield index.
            if ((idx >= 0 && idx < ownCandidate.getCard().getActivatedAbilities().size())
                    || (idx == 0 && !ownCandidate.getCard().getEffects(EffectSlot.ON_TAP).isEmpty())) {
                return ownCandidate;
            }
        }
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (entry.getKey().equals(activatorId)) {
                continue;
            }
            List<Permanent> battlefield = entry.getValue();
            if (permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                continue;
            }
            Permanent candidate = battlefield.get(permanentIndex);
            List<ActivatedAbility> abilities = getEffectiveActivatedAbilities(gameData, candidate);
            if (idx >= 0 && idx < abilities.size() && abilities.get(idx).isActivatableByAnyPlayer()) {
                return candidate;
            }
        }
        return ownCandidate;
    }

    private ActivatedAbility resolveAbility(GameData gameData, Permanent permanent, Integer abilityIndex) {
        List<ActivatedAbility> abilities = getEffectiveActivatedAbilities(gameData, permanent);
        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        return abilities.get(idx);
    }

    /**
     * Pure legality query: could {@code playerId} legally activate the ability at
     * {@code abilityIndex} on {@code permanent} right now, disregarding target choice?
     * X is assumed to be the ability's minimum legal value. Mana affordability is checked against
     * {@code manaPool}, which may be hypothetical (the AI passes the pool of mana it could produce).
     * Never mutates game state.
     */
    public boolean canActivateAbility(GameData gameData, UUID playerId, Permanent permanent,
                                      int abilityIndex, ManaPool manaPool) {
        return canActivateAbility(gameData, playerId, permanent, abilityIndex, manaPool, null, null);
    }

    /**
     * Pure legality query that includes the generic cost imposed by the proposed targets.
     */
    public boolean canActivateAbility(GameData gameData, UUID playerId, Permanent permanent,
                                      int abilityIndex, ManaPool manaPool, UUID targetId,
                                      List<UUID> targetIds) {
        try {
            ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
            int dryRunXValue = Math.max(0, ability.getMinimumXValue());
            boolean usesCreatureManaPayment = ability.getEffects().stream()
                    .anyMatch(TapCreaturesForManaCost.class::isInstance);
            List<UUID> abilityTargetIds = usesCreatureManaPayment ? List.of() : targetIds;
            int additionalGenericCost = getActivatedAbilityAdditionalGenericCost(
                    gameData, playerId, permanent, ability, targetId, abilityTargetIds, dryRunXValue);
            validateActivationLegality(
                    gameData, playerId, permanent, ability, abilityIndex, dryRunXValue, manaPool,
                    additionalGenericCost, false, usesCreatureManaPayment ? targetIds : null);
            return true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the generic cost adjustment that applies to an activated ability in the proposed
     * activation context. This is shared by dry-run legality checks, AI mana planning, and payment.
     */
    public int getActivatedAbilityAdditionalGenericCost(
            GameData gameData, UUID playerId, Permanent permanent, int abilityIndex,
            UUID targetId, List<UUID> targetIds) {
        ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
        return getActivatedAbilityAdditionalGenericCost(
                gameData, playerId, permanent, ability, targetId, targetIds);
    }

    private int getActivatedAbilityAdditionalGenericCost(
            GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability,
            UUID targetId, List<UUID> targetIds) {
        return getActivatedAbilityAdditionalGenericCost(
                gameData, playerId, permanent, ability, targetId, targetIds, 0);
    }

    private int getActivatedAbilityAdditionalGenericCost(
            GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability,
            UUID targetId, List<UUID> targetIds, int effectiveXValue) {
        int additionalGenericCost =
                castingCostService.getTargetingSubtypeTax(gameData, playerId, targetId, targetIds, true)
                        + castingCostService.getActivatedAbilityActivationTax(
                        gameData, playerId, permanent, ability, isManaAbility(ability));
        String abilityCost = effectiveAbilityManaCost(gameData, permanent, ability);
        if (abilityCost == null) {
            return additionalGenericCost;
        }

        ManaCost manaCost = new ManaCost(abilityCost);
        int totalManaCost = manaCost.getManaValue()
                + effectiveXValue * manaCost.getXSymbolCount();
        int genericCost = manaCost.getGenericCost();
        int equipReduction = Math.min(
                castingCostService.getActivatedAbilityCostReduction(
                        gameData, playerId, permanent, ability, targetId, targetIds),
                genericCost);
        additionalGenericCost -= equipReduction;
        int battlefieldReduction = Math.min(
                castingCostService.getActivatedAbilityActivationCostReduction(gameData, permanent, ability),
                Math.max(0, totalManaCost + additionalGenericCost - 1));
        additionalGenericCost -= battlefieldReduction;
        AmountContext activationCostContext = new AmountContext(
                playerId, permanent, targetId, effectiveXValue, 0);
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof ActivationCostModifierEffect modifier) {
                int amount = amountEvaluationService.evaluate(gameData, modifier.amount(), activationCostContext);
                if (modifier.reducesGenericCost()) {
                    int reduction = Math.min(
                            amount, Math.max(0, genericCost + additionalGenericCost));
                    additionalGenericCost -= reduction;
                } else {
                    additionalGenericCost += amount;
                }
            }
        }
        return additionalGenericCost;
    }

    /**
     * Runs every state-based (target-independent) legality check for activating {@code ability},
     * throwing {@link IllegalStateException} on the first violated rule and mutating nothing.
     * This is the single source of truth for activation legality: {@code activateAbilityInternal}
     * calls it before paying any cost, and AI players query it via {@link #canActivateAbility}.
     * Spell-target and target legality are validated separately where the chosen targets are known.
     *
     * @param manaPool              pool to check mana affordability against (may be hypothetical)
     * @param additionalGenericCost extra generic mana required, e.g. targeting tax; 0 when unknown
     */
    public void validateActivationLegality(GameData gameData, UUID playerId, Permanent permanent,
                                           ActivatedAbility ability, int abilityIndex, int xValue,
                                           ManaPool manaPool, int additionalGenericCost) {
        validateActivationLegality(gameData, playerId, permanent, ability, abilityIndex, xValue,
                manaPool, additionalGenericCost, false, null);
    }

    /**
     * @param discardCostAlreadyPaid when true, skip the discard-hand size check (interactive path
     *                               already paid the discard(s) before re-entering activation)
     */
    public void validateActivationLegality(GameData gameData, UUID playerId, Permanent permanent,
                                           ActivatedAbility ability, int abilityIndex, int xValue,
                                           ManaPool manaPool, int additionalGenericCost,
                                           boolean discardCostAlreadyPaid) {
        validateActivationLegality(gameData, playerId, permanent, ability, abilityIndex, xValue,
                manaPool, additionalGenericCost, discardCostAlreadyPaid, null);
    }

    private void validateActivationLegality(GameData gameData, UUID playerId, Permanent permanent,
                                            ActivatedAbility ability, int abilityIndex, int xValue,
                                            ManaPool manaPool, int additionalGenericCost,
                                            boolean discardCostAlreadyPaid,
                                            List<UUID> selectedCreatureManaPaymentIds) {
        List<CardEffect> abilityEffects = ability.getEffects();

        if (xValue < ability.getMinimumXValue()) {
            throw new IllegalStateException("X must be at least " + ability.getMinimumXValue());
        }

        // Sen Triplets: a player locked out this turn can't activate any ability.
        if (gameData.playersCantActivateAbilitiesThisTurn.contains(playerId)) {
            throw new IllegalStateException("You can't activate abilities this turn");
        }

        // City of Solitude: players can activate abilities only during their own turns.
        validateNotBlockedByOwnTurnOnlyRestriction(gameData, playerId);

        // Volrath's Curse: only the enchanted permanent's controller may activate this ability.
        if (ability.isActivatableOnlyByEnchantedPermanentController()) {
            UUID enchantedController = permanent.isAttached()
                    ? gameQueryService.findPermanentController(gameData, permanent.getAttachedTo())
                    : null;
            if (!playerId.equals(enchantedController)) {
                throw new IllegalStateException(
                        "Only the enchanted permanent's controller may activate this ability");
            }
        }

        // Soul Ransom: only opponents of the source permanent's controller may activate this ability.
        if (ability.isActivatableOnlyByOpponents()
                && playerId.equals(gameQueryService.findPermanentController(gameData, permanent.getId()))) {
            throw new IllegalStateException("Only your opponents may activate this ability");
        }

        if (ability.isActivatableOnlyByGrantingPlayer()
                && !playerId.equals(ability.getGrantingPlayerId())) {
            throw new IllegalStateException("Only the player who granted this ability may activate it");
        }

        // Pithing Needle check: block non-mana activated abilities of the chosen name
        validateNotBlockedByPithingNeedle(gameData, permanent, ability);

        // Arrest check: block all activated abilities of enchanted creature
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent, isManaAbility(ability));
        validateNotBlockedByOpponentsTurnRestriction(gameData, playerId, permanent);

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);
        validateNotBlockedByNonManaAbilityLock(gameData, playerId, ability);
        validateNotBlockedByCombatActionLock(gameData, ability);

        // Activation timing restrictions (e.g. "Activate only during your upkeep")
        validateTimingRestrictions(gameData, playerId, permanent, ability);
        validateActivationLimitPerTurn(gameData, playerId, permanent, ability, abilityIndex);

        // Loyalty ability restrictions (the cost itself is paid after target legality is confirmed)
        if (ability.getLoyaltyCost() != null) {
            if (gameQueryService.isPlaneswalkerLoyaltyAbilityLocked(gameData, permanent)) {
                throw new IllegalStateException("Loyalty abilities of planeswalkers can't be activated");
            }
            validateLoyaltyCost(gameData, playerId, permanent, ability, xValue);
        }

        // Tap requirement
        if (ability.isRequiresTap()) {
            // Serra Bestiary / Katabatic Winds: only activated abilities with {T} in their costs are locked.
            if (gameQueryService.isLockedFromActivatingTapAbilities(gameData, permanent)) {
                throw new IllegalStateException("Tap abilities of " + permanent.getCard().getName() + " can't be activated");
            }
            if (permanent.isTapped()) {
                throw new IllegalStateException("Permanent is already tapped");
            }
            if (gameQueryService.isSummoningSickForTapCost(gameData, permanent, playerId)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        if (ability.isRequiresAnotherActivatedAbility()
                && getEffectiveActivatedAbilities(gameData, permanent).size() < 2) {
            throw new IllegalStateException("Activate only if this creature has another activated ability");
        }

        // Untap requirement ({Q}): the permanent must be tapped, and creatures obey the same
        // summoning-sickness restriction as {T} (CR 302.6).
        if (ability.isRequiresUntap()) {
            if (!permanent.isTapped()) {
                throw new IllegalStateException("Permanent is not tapped");
            }
            if (gameQueryService.cantBecomeUntapped(gameData, permanent)) {
                throw new IllegalStateException("Permanent can't become untapped");
            }
            if (gameQueryService.isSummoningSickForTapCost(gameData, permanent, playerId)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        // Angel of Jubilation: life payments and creature sacrifices can't be used as ability costs
        if (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)) {
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof PayLifeCost || effect instanceof PayXLifeCost) {
                    throw new IllegalStateException("Players can't pay life to activate abilities");
                }
                if (effect instanceof SacrificeCreatureCost) {
                    throw new IllegalStateException("Players can't sacrifice creatures to activate abilities");
                }
            }
        }

        // Permanent-choice costs (sacrifice, tap others, crew, ...) need enough valid choices
        UUID sourceId = permanent.getId();
        for (CardEffect effect : abilityEffects) {
            PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(gameData, effect, sourceId, xValue);
            if (handler != null) {
                handler.validateCanPay(gameData, playerId);
            }
        }
        if (abilityEffects.stream().anyMatch(ExileArtifactsWithTotalManaValueCost.class::isInstance)
                && collectExileArtifactsCostCandidates(gameData, playerId, permanent).isEmpty()) {
            throw new IllegalStateException("Must control another artifact to exile as a cost");
        }

        if (abilityEffects.stream().anyMatch(ExileSourceEquipmentCost.class::isInstance)) {
            Permanent equipment = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment == null) {
                throw new IllegalStateException("The granting Equipment is not on the battlefield");
            }
        }

        if (abilityEffects.stream().anyMatch(effect -> effect instanceof CostEffect cost
                && cost.tapsGrantingEquipment())) {
            Permanent equipment = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment == null) {
                throw new IllegalStateException("The granting Equipment is not on the battlefield");
            }
            if (equipment.isTapped()) {
                throw new IllegalStateException("The granting Equipment is already tapped");
            }
        }

        if (abilityEffects.stream().anyMatch(UnattachSourceEquipmentCost.class::isInstance)) {
            Permanent equipment = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment == null || !equipment.isAttached()) {
                throw new IllegalStateException("The granting Equipment is not attached");
            }
        }
        String abilityCost = effectiveAbilityManaCost(gameData, permanent, ability);
        if (ability.isManaCostOfEnchantedPermanent() && abilityCost == null) {
            throw new IllegalStateException("The enchanted permanent has no mana cost");
        }
        int creatureManaPaymentCount = validateCreatureManaPayment(
                gameData, playerId, permanent, ability, abilityCost, xValue,
                additionalGenericCost, selectedCreatureManaPaymentIds);
        CastingCostService.ImposedSacrificeRequirement imposedTax =
                castingCostService.getImposedSacrificeRequirementForAbility(gameData, abilityCost);
        if (!imposedTax.isEmpty()) {
            PermanentChoiceCostHandler imposedHandler = toPermanentChoiceCostHandler(
                    gameData, new SacrificeMultiplePermanentsCost(imposedTax.count(), imposedTax.filter()),
                    sourceId, xValue);
            if (imposedHandler != null) {
                imposedHandler.validateCanPay(gameData, playerId);
            }
        }
        for (CostEffect additionalCost : castingCostService.getActivatedAbilityAdditionalCosts(gameData, permanent)) {
            PermanentChoiceCostHandler additionalHandler = toPermanentChoiceCostHandler(
                    gameData, additionalCost, sourceId, xValue);
            if (additionalHandler != null) {
                additionalHandler.validateCanPay(gameData, playerId);
            }
        }

        WaterbendCost waterbendCost = abilityEffects.stream()
                .filter(WaterbendCost.class::isInstance)
                .map(WaterbendCost.class::cast)
                .findFirst()
                .orElse(null);
        if (waterbendCost != null && minimumWaterbendTaps(
                gameData, playerId, permanent, ability, waterbendCost.effectiveAmount(xValue),
                ability.isRequiresTap()) < 0) {
            throw new IllegalStateException("Not enough mana or untapped artifacts and creatures to pay waterbend cost");
        }

        CraftMaterialCost craftMaterialCost = abilityEffects.stream()
                .filter(CraftMaterialCost.class::isInstance)
                .map(CraftMaterialCost.class::cast)
                .findFirst()
                .orElse(null);
        if (craftMaterialCost != null) {
            validateCraftMaterialCost(gameData, playerId, permanent, craftMaterialCost);
        }

        // Pay-life cost
        Optional<PayLifeCost> payLifeCost = abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst();
        if (payLifeCost.isPresent()) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            int needed = payLifeCost.get().effectiveAmount(life, sourceCounterCount(permanent, payLifeCost.get()));
            if (life < needed) {
                throw new IllegalStateException("Not enough life to pay (need " + needed + ", have " + life + ")");
            }
        }

        if (abilityEffects.stream().anyMatch(PayXLifeCost.class::isInstance)) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            if (life < xValue) {
                throw new IllegalStateException("Not enough life to pay (need " + xValue + ", have " + life + ")");
            }
        }

        Optional<PayEnergyCost> payEnergyCost = abilityEffects.stream()
                .filter(PayEnergyCost.class::isInstance)
                .map(PayEnergyCost.class::cast)
                .findFirst();
        if (payEnergyCost.isPresent()) {
            int energy = gameData.playerEnergyCounters.getOrDefault(playerId, 0);
            int needed = payEnergyCost.get().amount();
            if (energy < needed) {
                throw new IllegalStateException("Not enough energy to pay (need " + needed + ", have " + energy + ")");
            }
        }

        // Mana affordability (CR 602.2b — checked before entering interactive cost choices)
        if (abilityCost != null) {
            ManaCost preCheck = new ManaCost(abilityCost);
            ManaPool affordabilityPool = manaPool;
            if (manaPool != null
                    && gameQueryService.canSpendBlueManaAsAnyColorForActivatedAbilities(gameData, permanent)
                    && !manaPool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
                affordabilityPool = copyManaPool(manaPool);
                affordabilityPool.setBlueSpendableAsAnyColorForActivatedAbilities(true);
            }
            if (manaPool != null
                    && gameQueryService.canSpendManaAsAnyColorForActivatedAbilities(gameData, permanent)
                    && !manaPool.isAllManaSpendableAsAnyColorForActivatedAbilities()) {
                affordabilityPool = copyManaPool(affordabilityPool);
                affordabilityPool.setAllManaSpendableAsAnyColorForActivatedAbilities(true);
            }
            if (manaPool != null && gameQueryService.isCreature(gameData, permanent)) {
                affordabilityPool = copyManaPool(affordabilityPool);
                affordabilityPool.promoteCreatureAbilityMana();
            }
            boolean artifactCtx = gameQueryService.isArtifact(permanent);
            boolean myrCtx = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            Set<CardSubtype> soaCtx = effectiveSpellOrAbilitySubtypes(gameData, permanent, ability);
            Set<CardSubtype> creatureSourceSoaCtx = gameQueryService.isCreature(gameData, permanent)
                    ? soaCtx : Set.of();
            boolean powerstoneCtx = manaPool != null && manaPool.getPowerstoneOnlyColorless() > 0;
            int effectiveAdditionalGenericCost = additionalGenericCost - creatureManaPaymentCount;
            if (preCheck.hasX() && ability.getXColorRestrictions() != null) {
                if (!preCheck.canPay(affordabilityPool, xValue, ability.getXColorRestrictions(), effectiveAdditionalGenericCost)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            } else if (preCheck.hasX()) {
                if (!preCheck.canPay(affordabilityPool, xValue + effectiveAdditionalGenericCost, artifactCtx, myrCtx,
                        false, false, false, null, soaCtx, false, artifactCtx, false, false, Set.of(),
                        creatureSourceSoaCtx, powerstoneCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            } else {
                if (!preCheck.canPay(affordabilityPool, effectiveAdditionalGenericCost, artifactCtx, myrCtx,
                        false, false, false, null, soaCtx, false, artifactCtx, false, false, Set.of(),
                        creatureSourceSoaCtx, powerstoneCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            }
        } else if (additionalGenericCost - creatureManaPaymentCount > 0) {
            // No base mana cost but targeting tax applies — validate player can pay the tax
            ManaPool affordabilityPool = manaPool;
            if (gameQueryService.isCreature(gameData, permanent)) {
                affordabilityPool = copyManaPool(manaPool);
                affordabilityPool.promoteCreatureSpellOrAbilityMana();
            }
            if (affordabilityPool.getTotal() < additionalGenericCost - creatureManaPaymentCount) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
        }

        // Exile-from-graveyard cost needs at least one valid card
        ExileCardFromGraveyardCost exileGraveyardCost = abilityEffects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        boolean noMatchingGraveyardCard = exileGraveyardCost != null
                && (exileGraveyardCost.anyGraveyard()
                ? collectAnyGraveyardExileCandidates(gameData, exileGraveyardCost).isEmpty()
                : collectGraveyardIndicesForType(gameData.playerGraveyards.get(playerId), exileGraveyardCost.requiredType(),
                exileGraveyardCost.alternateType(), exileGraveyardCost.requiredSubtype()).isEmpty());
        if (noMatchingGraveyardCard) {
            String typeName = graveyardExileFilterLabel(exileGraveyardCost.requiredType(),
                    exileGraveyardCost.alternateType(), exileGraveyardCost.requiredSubtype());
            throw new IllegalStateException("No " + typeName + "card in graveyard to exile");
        }

        // Exile-top-card-of-graveyard cost needs a non-empty graveyard (Alms)
        boolean topGraveyardExileUnpayable = abilityEffects.stream()
                .filter(ExileTopCardOfGraveyardCost.class::isInstance)
                .map(ExileTopCardOfGraveyardCost.class::cast)
                .anyMatch(cost -> topMatchingGraveyardCard(
                        gameData.playerGraveyards.get(playerId), cost.requiredType()) == null);
        if (topGraveyardExileUnpayable) {
            throw new IllegalStateException("No card in graveyard to exile");
        }

        if (abilityEffects.stream().anyMatch(ExileInstantOrSorcerySpellCost.class::isInstance)
                && collectExileInstantOrSorcerySpellIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No instant or sorcery spell you control to exile from the stack");
        }

        if (abilityEffects.stream().anyMatch(PutCardExiledWithSourceIntoGraveyardCost.class::isInstance)
                && gameData.getCardsExiledByPermanent(permanent.getId()).isEmpty()) {
            throw new IllegalStateException("No card is exiled with this permanent");
        }

        // Exile-N-cards-from-graveyard cost (e.g. Immortal Coil "Exile two cards from your graveyard")
        // needs at least N cards in the graveyard.
        ExileNCardsFromGraveyardCost exileNGraveyardCost = abilityEffects.stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNGraveyardCost != null) {
            List<Card> gy = gameData.playerGraveyards.get(playerId);
            if (matchingGraveyardExileCandidates(gy, exileNGraveyardCost, null).size()
                    < exileNGraveyardCost.count()) {
                String typeName = graveyardExileFilterLabel(exileNGraveyardCost.requiredType(), null);
                throw new IllegalStateException("Not enough " + typeName + "cards in graveyard to exile (need "
                        + exileNGraveyardCost.count() + ")");
            }
        }

        PutCardsFromGraveyardOnBottomOfLibraryCost putBottomLibraryCost = abilityEffects.stream()
                .filter(PutCardsFromGraveyardOnBottomOfLibraryCost.class::isInstance)
                .map(PutCardsFromGraveyardOnBottomOfLibraryCost.class::cast)
                .findFirst()
                .orElse(null);
        if (putBottomLibraryCost != null
                && gameData.playerGraveyards.getOrDefault(playerId, List.of()).size()
                < putBottomLibraryCost.count()) {
            throw new IllegalStateException("Not enough cards in graveyard to pay the library cost (need "
                    + putBottomLibraryCost.count() + ")");
        }

        ExileNCardsFromSingleGraveyardCost exileSingleGraveyardCost = abilityEffects.stream()
                .filter(ExileNCardsFromSingleGraveyardCost.class::isInstance)
                .map(ExileNCardsFromSingleGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileSingleGraveyardCost != null
                && collectAnySingleGraveyardExileCandidates(gameData, exileSingleGraveyardCost).isEmpty()) {
            throw new IllegalStateException("Not enough matching cards in a single graveyard to exile");
        }

        ExileXCardsFromGraveyardCost exileXGraveyardCost = abilityEffects.stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileXGraveyardCost != null) {
            List<Card> gy = gameData.playerGraveyards.get(playerId);
            if (matchingGraveyardExileCandidates(gy, exileXGraveyardCost, null).size() < xValue) {
                String typeName = graveyardExileFilterLabel(exileXGraveyardCost.requiredType(), null);
                throw new IllegalStateException("Not enough " + typeName + "cards in graveyard to exile (need "
                        + xValue + ")");
            }
        }

        CollectEvidenceCost collectEvidenceCost = abilityEffects.stream()
                .filter(CollectEvidenceCost.class::isInstance)
                .map(CollectEvidenceCost.class::cast)
                .findFirst()
                .orElse(null);
        if (collectEvidenceCost != null) {
            List<Card> gy = gameData.playerGraveyards.get(playerId);
            int totalManaValue = gy == null ? 0 : gy.stream().mapToInt(Card::getManaValue).sum();
            if (totalManaValue < collectEvidenceCost.minimumManaValue()) {
                throw new IllegalStateException("Not enough mana value in graveyard to collect evidence (need "
                        + collectEvidenceCost.minimumManaValue() + ")");
            }
        }

        // Discard cost needs enough valid cards in hand (skipped when already paid interactively)
        if (!discardCostAlreadyPaid) {
            HandCardCost discardCardTypeCost = abilityEffects.stream()
                    .filter(HandCardCost.class::isInstance)
                    .map(HandCardCost.class::cast)
                    .findFirst()
                    .orElse(null);
            if (discardCardTypeCost != null
                    && collectDiscardIndices(gameData, playerId, gameData.playerHands.get(playerId),
                    discardCardTypeCost, xValue).size()
                    < discardCardTypeCost.requiredCount(xValue)) {
                String costLabel = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
                throw new IllegalStateException("Must " + discardCardTypeCost.payVerb() + " a " + costLabel
                        + "card to activate ability");
            }
        }

        // Random-discard cost needs enough cards in hand
        int randomDiscardCount = abilityEffects.stream()
                .filter(DiscardRandomCardCost.class::isInstance)
                .mapToInt(effect -> ((DiscardRandomCardCost) effect).count())
                .sum();
        if (randomDiscardCount > 0) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.size() < randomDiscardCount) {
                throw new IllegalStateException("Must have " + randomDiscardCount
                        + " cards to discard at random to activate ability");
            }
        }

        // Reveal-two-color-sharing-cards cost needs a qualifying pair in hand
        if (abilityEffects.stream().anyMatch(e -> e instanceof RevealTwoCardsSharingColorCost)
                && colorSharingPair(gameData.playerHands.get(playerId)) == null) {
            throw new IllegalStateException("Must reveal two cards that share a color to activate ability");
        }

        HandRevealCost handRevealCost = abilityEffects.stream()
                .filter(HandRevealCost.class::isInstance)
                .map(HandRevealCost.class::cast)
                .findFirst()
                .orElse(null);
        if (handRevealCost != null) {
            int matchingCards = matchingHandCards(gameData, playerId, handRevealCost).size();
            if (xValue < 0 || matchingCards < xValue) {
                throw new IllegalStateException("Not enough matching cards in hand to reveal (need "
                        + xValue + ", have " + matchingCards + ")");
            }
        }

        // Remove-counter cost availability
        Optional<RemoveCounterFromSourceCost> removeCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveCounterFromSourceCost)
                .map(e -> (RemoveCounterFromSourceCost) e)
                .findFirst();

        Optional<RemoveCounterFromGrantingPermanentCost> removeGrantingCounterCost = abilityEffects.stream()
                .filter(RemoveCounterFromGrantingPermanentCost.class::isInstance)
                .map(RemoveCounterFromGrantingPermanentCost.class::cast)
                .findFirst();
        if (removeCounterCost.isPresent()) {
            int required = removeCounterCost.get().count();
            CounterType ct = removeCounterCost.get().counterType();
            int available = switch (ct) {
                case SILVER -> 0; // Silver counters are on exiled cards, not permanents
                case ANY -> countPermanentCounters(permanent);
                default -> permanent.getCounterCount(ct);
            };
            if (available < required) {
                throw new IllegalStateException("Not enough counters to remove (need " + required + ", have " + available + ")");
            }
        }

        if (removeGrantingCounterCost.isPresent()) {
            Permanent grantingPermanent = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (grantingPermanent == null) {
                throw new IllegalStateException("The granting permanent is not on the battlefield");
            }
            int required = removeGrantingCounterCost.get().count();
            CounterType ct = removeGrantingCounterCost.get().counterType();
            int available = switch (ct) {
                case SILVER -> 0;
                case ANY -> countPermanentCounters(grantingPermanent);
                default -> grantingPermanent.getCounterCount(ct);
            };
            if (available < required) {
                throw new IllegalStateException("Not enough counters to remove (need " + required + ", have " + available + ")");
            }
        }

        // Remove-X-counter cost: the chosen X may not exceed the counters actually present
        Optional<RemoveXCountersFromSourceCost> removeXCounterCost = abilityEffects.stream()
                .filter(RemoveXCountersFromSourceCost.class::isInstance)
                .map(RemoveXCountersFromSourceCost.class::cast)
                .findFirst();
        if (removeXCounterCost.isPresent()) {
            int available = permanent.getCounterCount(removeXCounterCost.get().counterType());
            if (xValue < 0 || xValue > available) {
                throw new IllegalStateException("Not enough counters to remove (need " + xValue + ", have " + available + ")");
            }
        }

        Optional<RemoveOneOrMoreCountersFromSourceCost> removeOneOrMoreCountersCost = abilityEffects.stream()
                .filter(RemoveOneOrMoreCountersFromSourceCost.class::isInstance)
                .map(RemoveOneOrMoreCountersFromSourceCost.class::cast)
                .findFirst();
        if (removeOneOrMoreCountersCost.isPresent()) {
            int available = permanent.getCounterCount(removeOneOrMoreCountersCost.get().counterType());
            if (xValue < 1 || xValue > available) {
                throw new IllegalStateException("Must remove between one and " + available + " counters");
            }
        }

        // "Tap enchanted permanent" cost needs the Aura to be attached to an untapped permanent
        if (abilityEffects.stream().anyMatch(e -> e instanceof TapEnchantedPermanentCost)) {
            enchantedPermanentForTapCost(gameData, permanent);
        }

        // Mill-controller cost (e.g. Deranged Assistant: "{T}, Mill a card: Add {C}.")
        Optional<MillControllerCost> millControllerCost = abilityEffects.stream()
                .filter(e -> e instanceof MillControllerCost)
                .map(e -> (MillControllerCost) e)
                .findFirst();
        if (millControllerCost.isPresent()) {
            int required = millControllerCost.get().count();
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null || deck.size() < required) {
                throw new IllegalStateException("Not enough cards in library to mill (need " + required + ")");
            }
        }

        // Exile-top-of-library cost (e.g. Royal Herbalist: "{2}, Exile the top card of your library: …")
        Optional<ExileTopCardOfLibraryCost> exileTopLibraryCost = abilityEffects.stream()
                .filter(e -> e instanceof ExileTopCardOfLibraryCost)
                .map(e -> (ExileTopCardOfLibraryCost) e)
                .findFirst();
        if (exileTopLibraryCost.isPresent()) {
            int required = exileTopLibraryCost.get().count();
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null || deck.size() < required) {
                throw new IllegalStateException("Not enough cards in library to exile (need " + required + ")");
            }
        }

        // Imprinted-copy X requirement — unless this same ability's exile cost sets the imprint
        // during payment, in which case the check runs after that cost (validateImprintedCopyXValue)
        if (exileGraveyardCost == null || !exileGraveyardCost.imprintOnSource()) {
            validateImprintedCopyXValue(gameData, permanent, abilityEffects, xValue);
        }
    }

    /**
     * Validates X for imprinted-card X-cost abilities (Prototype Portal, Elite Arcanist). Per ruling: "You may not activate the
     * second ability if no card has been exiled with Prototype Portal." X is defined by the exiled
     * card's mana value (not chosen freely), so no imprint = can't activate.
     */
    private void validateImprintedCopyXValue(GameData gameData, Permanent permanent, List<CardEffect> abilityEffects, int effectiveXValue) {
        ImprintedCardXCostEffect imprintedCopyEffect = abilityEffects.stream()
                .filter(ImprintedCardXCostEffect.class::isInstance)
                .map(ImprintedCardXCostEffect.class::cast)
                .filter(ImprintedCardXCostEffect::requiresImprintedXCost)
                .findFirst().orElse(null);
        if (imprintedCopyEffect != null) {
            Card imprintedCard = gameData.getImprintedCard(permanent.getCard());
            if (imprintedCard == null) {
                throw new IllegalStateException("No card has been exiled with " + permanent.getCard().getName());
            }
            int requiredX = imprintedCard.getManaValue();
            if (effectiveXValue != requiredX) {
                throw new IllegalStateException("X must equal the mana value of the imprinted card (" + requiredX + ")");
            }
        }
    }

    private int effectiveAbilityIndex(Integer abilityIndex) {
        return abilityIndex != null ? abilityIndex : 0;
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex) {
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone, nonTargeting, abilityIndex, null, null);
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds) {
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone, nonTargeting, abilityIndex, targetIds, null);
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds,
                                              Map<UUID, Integer> damageAssignments) {
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects, xValue, targetId,
                targetZone, nonTargeting, abilityIndex, targetIds, damageAssignments, List.of());
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds,
                                              Map<UUID, Integer> damageAssignments,
                                              List<UUID> sacrificedCardIds) {
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone, nonTargeting,
                targetIds, damageAssignments, sacrificedCardIds);
    }

    private void completeActivationAndRecordWithChosenPermanents(
                                              GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds,
                                              Map<UUID, Integer> damageAssignments,
                                              List<UUID> chosenCostPermanentIds,
                                              Card discardedCardSnapshot,
                                              Card exiledCostCardSnapshot) {
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
        if (chosenCostPermanentIds == null || chosenCostPermanentIds.isEmpty()) {
            activatedAbilityExecutionService.completeActivationAfterCosts(
                    gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone,
                    nonTargeting, targetIds, damageAssignments, List.of(), discardedCardSnapshot,
                    exiledCostCardSnapshot);
        } else {
            activatedAbilityExecutionService.completeActivationAfterCosts(
                    gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone,
                    nonTargeting, targetIds, damageAssignments,
                    chosenCostPermanentIds == null ? List.of() : chosenCostPermanentIds,
                    List.of(), discardedCardSnapshot, exiledCostCardSnapshot);
        }
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds,
                                              Map<UUID, Integer> damageAssignments, Card discardedCardSnapshot) {
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone,
                nonTargeting, targetIds, damageAssignments, discardedCardSnapshot);
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetIds,
                                              Map<UUID, Integer> damageAssignments, Card discardedCardSnapshot,
                                              Card exiledCostCardSnapshot) {
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone,
                nonTargeting, targetIds, damageAssignments, List.of(), discardedCardSnapshot,
                exiledCostCardSnapshot);
    }

    public void handleCraftMaterialChosen(GameData gameData, Player player,
                                          PendingInteraction.CraftMaterialChoice choice,
                                          List<UUID> cardIds) {
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        PendingInteraction.CraftMaterialChoice activeChoice =
                gameData.interaction.activeInteraction(PendingInteraction.CraftMaterialChoice.class);
        if (activeChoice == null || !activeChoice.equals(choice)) {
            throw new IllegalStateException("Not awaiting craft material choice");
        }
        if (cardIds == null
                || cardIds.size() < choice.minimumCards()
                || cardIds.size() > choice.maximumCards()
                || new HashSet<>(cardIds).size() != cardIds.size()
                || !choice.validCardIds().containsAll(cardIds)) {
            throw new IllegalStateException("Choose between " + choice.minimumCards() + " and "
                    + choice.maximumCards() + " valid craft materials");
        }

        Permanent source = gameQueryService.findPermanentById(gameData, choice.sourcePermanentId());
        if (source == null) {
            gameData.interaction.clearAwaitingInput();
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }
        ActivatedAbility ability = resolveAbility(gameData, source, choice.abilityIndex());
        CraftMaterialCost craftMaterialCost = ability.getEffects().stream()
                .filter(CraftMaterialCost.class::isInstance)
                .map(CraftMaterialCost.class::cast)
                .findFirst()
                .orElse(null);
        if (craftMaterialCost == null) {
            throw new IllegalStateException("Source ability no longer has a craft material cost");
        }

        payCraftMaterialCost(gameData, player, source, cardIds, craftMaterialCost);
        gameData.interaction.clearAwaitingInput();
        boolean nonTargeting = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget();
        completeActivationAndRecord(gameData, player, source, ability, ability.getEffects(), choice.xValue(),
                choice.targetId(), choice.targetZone(), nonTargeting, choice.abilityIndex(),
                choice.targetIds(), choice.damageAssignments());
    }

    private List<Card> collectCraftMaterialCandidates(GameData gameData, UUID playerId, Permanent source,
                                                       CraftMaterialCost cost) {
        List<Card> candidates = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            battlefield.stream()
                    .filter(permanent -> permanent != source)
                    .filter(permanent -> matchesCraftMaterial(gameData, permanent, cost))
                    .map(Permanent::getCard)
                    .forEach(candidates::add);
        }
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard != null) {
            graveyard.stream()
                    .filter(card -> matchesCraftMaterial(card, cost))
                    .forEach(candidates::add);
        }
        return candidates;
    }

    private List<Card> validateCraftMaterialCost(GameData gameData, UUID playerId, Permanent source,
                                                  CraftMaterialCost cost) {
        List<Card> candidates = collectCraftMaterialCandidates(gameData, playerId, source, cost);
        if (candidates.size() < cost.minimumCount()
                || !canSatisfyRequiredCraftSubtypes(gameData, candidates, cost)) {
            throw new IllegalStateException(craftMaterialError(cost));
        }
        return candidates;
    }

    private boolean matchesCraftMaterial(GameData gameData, Permanent permanent, CraftMaterialCost cost) {
        if (cost.nonlandOnly() && gameQueryService.isLand(gameData, permanent)) {
            return false;
        }
        if (cost.requiredType() != null
                && (cost.requiredType() == CardType.ARTIFACT
                ? !gameQueryService.isArtifact(gameData, permanent)
                : !permanent.getCard().hasType(cost.requiredType()))) {
            return false;
        }
        if (cost.requiredSubtype() != null
                && !predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, new PermanentHasSubtypePredicate(cost.requiredSubtype()))) {
            return false;
        }
        if (!cost.requiredSubtypes().isEmpty()
                && cost.requiredSubtypes().stream().noneMatch(subtype ->
                predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, new PermanentHasSubtypePredicate(subtype)))) {
            return false;
        }
        return !cost.requireActivatedAbility() || hasActivatedAbility(gameData, permanent);
    }

    private boolean matchesCraftMaterial(Card card, CraftMaterialCost cost) {
        if (cost.nonlandOnly() && card.hasType(CardType.LAND)) {
            return false;
        }
        if (cost.requiredType() != null && !card.hasType(cost.requiredType())) {
            return false;
        }
        if (cost.requiredSubtype() != null
                && !predicateEvaluationService.matchesCardPredicate(
                card, new CardSubtypePredicate(cost.requiredSubtype()), card.getId())) {
            return false;
        }
        if (!cost.requiredSubtypes().isEmpty()
                && cost.requiredSubtypes().stream().noneMatch(card.getSubtypes()::contains)) {
            return false;
        }
        return !cost.requireActivatedAbility()
                || !card.getActivatedAbilities().isEmpty()
                || !card.getEffects(EffectSlot.ON_TAP).isEmpty();
    }

    private boolean hasActivatedAbility(GameData gameData, Permanent permanent) {
        return !getEffectiveActivatedAbilities(gameData, permanent).isEmpty()
                || !permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty();
    }

    private String craftMaterialPrompt(CraftMaterialCost cost) {
        if (!cost.requiredSubtypes().isEmpty()) {
            return "one of each required creature type to exile as craft materials";
        }
        return cost.minimumCount() + " " + craftMaterialLabel(cost) + " to exile as craft materials";
    }

    private String craftMaterialError(CraftMaterialCost cost) {
        if (!cost.requiredSubtypes().isEmpty()) {
            return "Must have one of each required creature type to exile as craft materials";
        }
        if (cost.minimumCount() == 1 && cost.requiredType() == CardType.ARTIFACT
                && !cost.nonlandOnly() && !cost.requireActivatedAbility()) {
            return "Must exile another artifact you control or an artifact card from your graveyard";
        }
        return "Must have at least " + cost.minimumCount() + " " + craftMaterialLabel(cost)
                + " to exile as craft materials";
    }

    private String craftMaterialLabel(CraftMaterialCost cost) {
        if (!cost.requiredSubtypes().isEmpty()) {
            return cost.requiredSubtypes().stream()
                    .map(subtype -> "a " + subtype.name().toLowerCase())
                    .collect(Collectors.joining(", "));
        }
        if (cost.requireActivatedAbility() && cost.nonlandOnly()) {
            return "nonlands with activated abilities";
        }
        if (cost.requiredType() != null) {
            return cost.requiredType().name().toLowerCase() + "s";
        }
        return "matching craft materials";
    }

    private List<UUID> collectExileArtifactsCostCandidates(GameData gameData, UUID playerId,
                                                            Permanent source) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(candidate -> !candidate.getId().equals(source.getId()))
                .filter(candidate -> gameQueryService.isArtifact(gameData, candidate))
                .map(Permanent::getId)
                .toList();
    }

    private int totalManaValueOfPermanents(GameData gameData, List<UUID> permanentIds) {
        return permanentIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .sum();
    }

    private List<UUID> collectSacrificeAnyNumberCostCandidates(
            GameData gameData, UUID playerId, Permanent source,
            SacrificeAnyNumberOfPermanentsCost cost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        FilterContext filterContext = FilterContext.of(gameData).withSourcePermanentId(source.getId());
        return battlefield.stream()
                .filter(candidate -> !cost.excludeSource() || !candidate.getId().equals(source.getId()))
                .filter(candidate -> predicateEvaluationService.matchesPermanentPredicate(
                        candidate, cost.filter(), filterContext))
                .map(Permanent::getId)
                .toList();
    }

    private void payExileArtifactsCost(GameData gameData, Player player, List<UUID> permanentIds) {
        for (UUID permanentId : permanentIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
            if (chosen == null) {
                throw new IllegalStateException("A selected artifact is no longer on the battlefield");
            }
            exilePermanentAsCost(gameData, player, chosen);
        }
    }

    private void payCraftMaterialCost(GameData gameData, Player player, Permanent source, List<UUID> cardIds,
                                      CraftMaterialCost cost) {
        if (cardIds == null || cardIds.size() < cost.minimumCount()
                || (!cost.allowsAdditionalMaterials() && cardIds.size() > cost.minimumCount())
                || new HashSet<>(cardIds).size() != cardIds.size()) {
            throw new IllegalStateException("Not enough distinct craft materials selected");
        }
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Permanent> battlefieldChoices = new ArrayList<>();
        List<Card> graveyardChoices = new ArrayList<>();
        List<Set<CardSubtype>> selectedSubtypes = new ArrayList<>();
        for (UUID cardId : cardIds) {
            Permanent battlefieldChoice = battlefield == null ? null : battlefield.stream()
                    .filter(permanent -> permanent.getCard().getId().equals(cardId))
                    .findFirst()
                    .orElse(null);
            if (battlefieldChoice != null) {
                if (battlefieldChoice == source || !matchesCraftMaterial(gameData, battlefieldChoice, cost)) {
                    throw new IllegalStateException("Selected card is not a legal craft material");
                }
                battlefieldChoices.add(battlefieldChoice);
                selectedSubtypes.add(craftSubtypesForPermanent(gameData, battlefieldChoice, cost));
                continue;
            }
            Card graveyardChoice = graveyard == null ? null : graveyard.stream()
                    .filter(card -> card.getId().equals(cardId))
                    .findFirst()
                    .orElse(null);
            if (graveyardChoice == null || !matchesCraftMaterial(graveyardChoice, cost)) {
                throw new IllegalStateException("Selected card is not a legal craft material");
            }
            graveyardChoices.add(graveyardChoice);
            selectedSubtypes.add(craftSubtypesForCard(graveyardChoice, cost));
        }

        if (!canAssignCraftSubtypes(selectedSubtypes, cost.requiredSubtypes(), 0,
                new boolean[selectedSubtypes.size()])) {
            throw new IllegalStateException(craftMaterialError(cost));
        }

        if (cardIds.size() > 0) {
            for (Permanent chosen : battlefieldChoices) {
                int stackBeforeCraftMaterial = gameData.stack.size();
                permanentRemovalService.removePermanentToExileAsCraftMaterial(gameData, chosen, source.getId());
                if (gameData.stack.size() > stackBeforeCraftMaterial) {
                    gameData.pendingActivatedAbilityCostTriggers.addAll(
                            new ArrayList<>(gameData.stack.subList(stackBeforeCraftMaterial, gameData.stack.size())));
                    gameData.stack.subList(stackBeforeCraftMaterial, gameData.stack.size()).clear();
                }
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " exiles ", chosen.getCard(), " as a craft material."));
            }
            for (Card chosen : graveyardChoices) {
                graveyard.remove(chosen);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, chosen);
                exileService.exileCard(gameData, playerId, chosen, source.getId());
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " exiles ", chosen, " from graveyard as a craft material."));
            }
        }
    }

    private boolean canSatisfyRequiredCraftSubtypes(GameData gameData, List<Card> candidates,
                                                     CraftMaterialCost cost) {
        if (cost.requiredSubtypes().isEmpty()) {
            return true;
        }
        List<Set<CardSubtype>> candidateSubtypes = candidates.stream()
                .map(card -> {
                    Permanent permanent = findBattlefieldPermanentByCardId(gameData, card.getId());
                    return permanent == null
                            ? craftSubtypesForCard(card, cost)
                            : craftSubtypesForPermanent(gameData, permanent, cost);
                })
                .toList();
        return canAssignCraftSubtypes(candidateSubtypes, cost.requiredSubtypes(), 0,
                new boolean[candidateSubtypes.size()]);
    }

    private Permanent findBattlefieldPermanentByCardId(GameData gameData, UUID cardId) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private Set<CardSubtype> craftSubtypesForPermanent(GameData gameData, Permanent permanent,
                                                        CraftMaterialCost cost) {
        if (cost.requiredSubtypes().isEmpty()) {
            return Set.of();
        }
        return cost.requiredSubtypes().stream()
                .filter(subtype -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, new PermanentHasSubtypePredicate(subtype)))
                .collect(Collectors.toSet());
    }

    private Set<CardSubtype> craftSubtypesForCard(Card card, CraftMaterialCost cost) {
        if (cost.requiredSubtypes().isEmpty()) {
            return Set.of();
        }
        return cost.requiredSubtypes().stream()
                .filter(card.getSubtypes()::contains)
                .collect(Collectors.toSet());
    }

    private boolean canAssignCraftSubtypes(List<Set<CardSubtype>> candidateSubtypes,
                                           List<CardSubtype> requiredSubtypes, int requiredIndex,
                                           boolean[] usedCandidates) {
        if (requiredIndex == requiredSubtypes.size()) {
            return true;
        }
        CardSubtype requiredSubtype = requiredSubtypes.get(requiredIndex);
        for (int candidateIndex = 0; candidateIndex < candidateSubtypes.size(); candidateIndex++) {
            if (!usedCandidates[candidateIndex]
                    && candidateSubtypes.get(candidateIndex).contains(requiredSubtype)) {
                usedCandidates[candidateIndex] = true;
                if (canAssignCraftSubtypes(candidateSubtypes, requiredSubtypes, requiredIndex + 1,
                        usedCandidates)) {
                    return true;
                }
                usedCandidates[candidateIndex] = false;
            }
        }
        return false;
    }

    private void sacrificePermanentAsCost(GameData gameData, Player player, Permanent sacTarget) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(sacTarget)) {
            throw new IllegalStateException("Must sacrifice a permanent you control");
        }
        permanentRemovalService.removePermanentToGraveyard(gameData, sacTarget);
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, sacTarget.getCard());
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " sacrifices " , sacTarget.getCard(), "."));
    }

    private void exilePermanentAsCost(GameData gameData, Player player, Permanent exileTarget) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(exileTarget)) {
            throw new IllegalStateException("Must exile a permanent you control");
        }
        permanentRemovalService.removePermanentToExile(gameData, exileTarget);
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles " , exileTarget.getCard(), " as an activation cost."));
    }

    private void returnPermanentToHandAsCost(GameData gameData, Player player, Permanent target) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(target)) {
            throw new IllegalStateException("Must return a permanent you control");
        }
        permanentRemovalService.removePermanentToHand(gameData, target);
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " returns " , target.getCard(), " to hand."));
        }

    private static int countPermanentCounters(Permanent permanent) {
        int total = 0;
        for (CounterType type : CounterType.values()) {
            if (type != CounterType.ANY && type != CounterType.SILVER) {
                total += permanent.getCounterCount(type);
            }
        }
        return total;
    }

    private static void removeOtherPermanentCounters(GameData gameData, Permanent permanent, int count) {
        int remaining = count;
        for (CounterType type : CounterType.values()) {
            if (remaining == 0) {
                return;
            }
            if (type == CounterType.ANY || type == CounterType.SILVER
                    || type == CounterType.MINUS_ONE_MINUS_ONE || type == CounterType.PLUS_ONE_PLUS_ONE) {
                continue;
            }
            int removed = Math.min(remaining, permanent.getCounterCount(type));
            permanent.setCounterCount(type, permanent.getCounterCount(type) - removed);
            if (type == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
            remaining -= removed;
        }
    }

    private void validateTimingRestrictions(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability) {
        if (ability.getTimingRestriction() != null) {
            if (ability.getTimingRestriction() == ActivationTimingRestriction.COVEN) {
                if (!gameQueryService.isCovenMet(gameData, playerId)) {
                    throw new IllegalStateException("Coven — activate only if you control three or more creatures with different powers");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.METALCRAFT) {
                if (!gameQueryService.isMetalcraftMet(gameData, playerId)) {
                    throw new IllegalStateException("Metalcraft — activate only if you control three or more artifacts");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.MORBID) {
                if (!gameQueryService.isMorbidMet(gameData)) {
                    throw new IllegalStateException("Morbid — activate only if a creature died this turn");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.CAST_NONCREATURE_SPELL_THIS_TURN) {
                if (!gameQueryService.playerCastNoncreatureSpellThisTurn(gameData, playerId)) {
                    throw new IllegalStateException("Activate only if you've cast a noncreature spell this turn");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.OPPONENT_CONTROLS_FLYING_CREATURE) {
                if (!gameQueryService.anyOpponentControlsFlyingCreature(gameData, playerId)) {
                    throw new IllegalStateException("Activate only if an opponent controls a creature with flying");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.OPPONENT_CONTROLS_MORE_LANDS) {
                if (!gameQueryService.anyOpponentControlsMoreLands(gameData, playerId)) {
                    throw new IllegalStateException("Activate only if an opponent controls more lands than you");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING) {
                if (!permanent.isAttacking()) {
                    throw new IllegalStateException("Activate only if this creature is attacking");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING_OR_BLOCKING) {
                if (!permanent.isAttacking() && !permanent.isBlocking()) {
                    throw new IllegalStateException("Activate only if this creature is attacking or blocking");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_OPPONENTS_TURN) {
                if (playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during an opponent's turn");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_OPPONENTS_TURN_BEFORE_COMBAT) {
                if (playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during an opponent's turn");
                }
                if (!gameData.currentStep.isBeforeCombat()) {
                    throw new IllegalStateException("This ability can only be activated before combat");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during your turn, before attackers are declared");
                }
                if (!gameData.currentStep.isBeforeAttackersDeclared()) {
                    throw new IllegalStateException("This ability can only be activated before attackers are declared");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.BEFORE_ATTACKERS_DECLARED) {
                if (!gameData.currentStep.isBeforeAttackersDeclared()
                        || gameData.combatPhasesThisTurn > 1) {
                    throw new IllegalStateException("This ability can only be activated before attackers are declared");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.BEFORE_BLOCKERS_DECLARED) {
                if (!gameData.currentStep.isBeforeBlockersDeclared()
                        || gameData.combatPhasesThisTurn > 1) {
                    throw new IllegalStateException("This ability can only be activated before blockers are declared");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.BEFORE_COMBAT_DAMAGE
                    && !gameData.currentStep.isBeforeCombatDamage()) {
                throw new IllegalStateException("This ability can only be activated before the combat damage step");
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_COMBAT) {
                if (!gameData.currentStep.isCombatPhase()) {
                    throw new IllegalStateException("This ability can only be activated during combat");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_END_OF_COMBAT) {
                if (gameData.currentStep != TurnStep.END_OF_COMBAT) {
                    throw new IllegalStateException("This ability can only be activated during the end of combat step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_BEFORE_END_OF_COMBAT) {
                if (!gameData.currentStep.isBeforeEndOfCombat()) {
                    throw new IllegalStateException("This ability can only be activated before the end of combat step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED) {
                if (gameData.currentStep != TurnStep.DECLARE_ATTACKERS) {
                    throw new IllegalStateException("This ability can only be activated during the declare attackers step");
                }
                if (!gameQueryService.isPlayerBeingAttacked(gameData, playerId)) {
                    throw new IllegalStateException("This ability can only be activated if you've been attacked this step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS) {
                if (gameData.currentStep != TurnStep.DECLARE_BLOCKERS) {
                    throw new IllegalStateException("This ability can only be activated during the declare blockers step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_DECLARE_BLOCKERS_IF_BLOCKED) {
                if (gameData.currentStep != TurnStep.DECLARE_BLOCKERS) {
                    throw new IllegalStateException("This ability can only be activated during the declare blockers step");
                }
                if (!gameQueryService.isBlockedByAnyCreature(gameData, permanent)) {
                    throw new IllegalStateException("This ability can only be activated if a creature is blocking this creature");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_CREATURE) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    throw new IllegalStateException("This ability can only be activated while this permanent is a creature");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_TURN) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during your turn");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_TURN_BEFORE_END_STEP) {
                if (!playerId.equals(gameData.activePlayerId)
                        || !gameData.currentStep.isBeforeEndStep()) {
                    throw new IllegalStateException("This ability can only be activated during your turn before the end step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during your upkeep");
                }
                if (gameData.currentStep != TurnStep.UPKEEP) {
                    throw new IllegalStateException("This ability can only be activated during your upkeep");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP) {
                if (gameData.currentStep != TurnStep.UPKEEP) {
                    throw new IllegalStateException("This ability can only be activated during an upkeep step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_DRAW_STEP) {
                if (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.DRAW) {
                    throw new IllegalStateException("This ability can only be activated during your draw step");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_OPPONENTS_UPKEEP) {
                if (gameData.currentStep != TurnStep.UPKEEP || playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during an opponent's upkeep");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.POWER_4_OR_GREATER) {
                int effectivePower = gameQueryService.getEffectivePower(gameData, permanent);
                if (effectivePower < 4) {
                    throw new IllegalStateException("Activate only if this creature's power is 4 or greater");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.RAID) {
                if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) {
                    throw new IllegalStateException("Raid — activate only if you attacked this turn");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                    && !gameQueryService.canActivateAbilityAtInstantSpeed(gameData, playerId, ability)) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated at sorcery speed");
                }
                if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                    throw new IllegalStateException("This ability can only be activated at sorcery speed during your main phase");
                }
                if (!gameData.stack.isEmpty()) {
                    throw new IllegalStateException("This ability can only be activated when the stack is empty");
                }
            }
        }

        // Subtype count restriction (e.g. "Activate only if you control five or more Vampires")
        if (ability.getRequiredControlledSubtype() != null) {
            int count = gameQueryService.countControlledSubtypePermanents(gameData, playerId, ability.getRequiredControlledSubtype());
            if (count < ability.getRequiredControlledSubtypeCount()) {
                throw new IllegalStateException("Activate only if you control " + ability.getRequiredControlledSubtypeCount()
                        + " or more " + ability.getRequiredControlledSubtype().name() + "s");
            }
        }

        // Source-counter restriction (e.g. Edifice of Authority's "Activate only if there are three
        // or more brick counters on this artifact").
        if (ability.getRequiredSourceCounterType() != null
                && permanent.getCounterCount(ability.getRequiredSourceCounterType()) < ability.getRequiredSourceCounterCount()) {
            throw new IllegalStateException("Activate only if there are " + ability.getRequiredSourceCounterCount()
                    + " or more " + ability.getRequiredSourceCounterType().name().toLowerCase() + " counters on "
                    + permanent.getCard().getName());
        }

        // Predicate-count restriction (e.g. Leechridden Swamp's "Activate only if you control two or more black permanents")
        if (ability.getRequiredControlledPermanentPredicate() != null) {
            int count = gameQueryService.countControlledPermanentsMatching(gameData, playerId, ability.getRequiredControlledPermanentPredicate());
            if (count < ability.getRequiredControlledPermanentCount()) {
                throw new IllegalStateException("Activate only if you control " + ability.getRequiredControlledPermanentCount()
                        + " or more " + ability.getRequiredControlledPermanentDescription());
            }
        }

        // Graveyard-card-count restriction (e.g. Gate to the Afterlife's "Activate only if there are
        // six or more creature cards in your graveyard"). Counts non-token cards in the controller's graveyard.
        if (ability.getRequiredGraveyardCardPredicate() != null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            int count = 0;
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (!card.isToken()
                            && predicateEvaluationService.matchesCardPredicate(
                            card, ability.getRequiredGraveyardCardPredicate(), null, gameData, playerId)) {
                        count++;
                    }
                }
            }
            if (count < ability.getRequiredGraveyardCardCount()) {
                throw new IllegalStateException("Activate only if there are " + ability.getRequiredGraveyardCardCount()
                        + " or more " + ability.getRequiredGraveyardCardDescription());
            }
        }

        // Compound activation condition (e.g. "Activate only if you control a Desert or there is a
        // Desert card in your graveyard"). Prefer typed helpers above when they alone express the gate.
        if (ability.getActivationCondition() != null
                && !conditionEvaluationService.isMet(gameData, ability.getActivationCondition(),
                        ConditionContext.forPermanent(permanent, playerId))) {
            String message = ability.getActivationConditionDescription();
            throw new IllegalStateException(message != null ? message : "Activation condition not met");
        }

        validateHandSizeRestrictions(gameData, playerId, ability);
    }

    private void validateHandTimingRestriction(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
                && !playerId.equals(gameData.activePlayerId)) {
            throw new IllegalStateException("This ability can only be activated during your turn");
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
                && (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.UPKEEP)) {
            throw new IllegalStateException("This ability can only be activated during your upkeep");
        }
    }

    /**
     * Enforces hand-size activation gates common to battlefield and graveyard abilities: a minimum
     * (e.g. Resonating Lute's "seven or more cards in your hand") and/or a maximum (e.g. Dread
     * Wanderer's "one or fewer cards in hand").
     */
    private void validateHandSizeRestrictions(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability.getMinCardsInHandToActivate() <= 0 && ability.getMaxCardsInHandToActivate() == null) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        int handSize = hand != null ? hand.size() : 0;
        if (ability.getMinCardsInHandToActivate() > 0 && handSize < ability.getMinCardsInHandToActivate()) {
            throw new IllegalStateException("Activate only if you have " + ability.getMinCardsInHandToActivate()
                    + " or more cards in your hand");
        }
        if (ability.getMaxCardsInHandToActivate() != null && handSize > ability.getMaxCardsInHandToActivate()) {
            throw new IllegalStateException("Activate only if you have " + ability.getMaxCardsInHandToActivate()
                    + " or fewer cards in your hand");
        }
    }

    private void validateGraveyardTimingRestrictions(GameData gameData, UUID playerId, ActivatedAbility ability,
                                                     Card card) {
        validateHandSizeRestrictions(gameData, playerId, ability);
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
                && !playerId.equals(gameData.activePlayerId)) {
            throw new IllegalStateException("This ability can only be activated during your turn");
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED) {
            if (!playerId.equals(gameData.activePlayerId)) {
                throw new IllegalStateException("This ability can only be activated at sorcery speed");
            }
            if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                throw new IllegalStateException("This ability can only be activated at sorcery speed during your main phase");
            }
            if (!gameData.stack.isEmpty()) {
                throw new IllegalStateException("This ability can only be activated when the stack is empty");
            }
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.RAID) {
            if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) {
                throw new IllegalStateException("Raid — activate only if you attacked this turn");
            }
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP) {
            if (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.UPKEEP) {
                throw new IllegalStateException("This ability can only be activated during your upkeep");
            }
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP) {
            if (gameData.currentStep != TurnStep.UPKEEP) {
                throw new IllegalStateException("This ability can only be activated during an upkeep step");
            }
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_OPPONENTS_UPKEEP) {
            if (gameData.currentStep != TurnStep.UPKEEP || playerId.equals(gameData.activePlayerId)) {
                throw new IllegalStateException("This ability can only be activated during an opponent's upkeep");
            }
        }
        // Graveyard activation gates that need the source card (e.g. Ashen Ghoul's
        // CardsAboveSelfInGraveyard). Battlefield gates use ConditionContext.forPermanent.
        if (ability.getActivationCondition() != null
                && !conditionEvaluationService.isMet(gameData, ability.getActivationCondition(),
                        ConditionContext.forCard(card, playerId))) {
            String message = ability.getActivationConditionDescription();
            throw new IllegalStateException(message != null ? message : "Activation condition not met");
        }
    }

    /**
     * Validates all loyalty-ability activation rules without paying anything, returning the
     * (possibly negative) loyalty delta the activation will apply.
     */
    private int validateLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int effectiveXValue) {
        if (gameData.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn.contains(playerId)) {
            throw new IllegalStateException(
                    "You can't activate planeswalkers' loyalty abilities this turn");
        }
        if (!gameQueryService.allowsInstantSpeedLoyaltyActivation(permanent)) {
            if (!playerId.equals(gameData.activePlayerId)) {
                throw new IllegalStateException("Loyalty abilities can only be activated on your turn");
            }
            if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                throw new IllegalStateException("Loyalty abilities can only be activated during a main phase");
            }
            if (!gameData.stack.isEmpty()) {
                throw new IllegalStateException("Loyalty abilities can only be activated when the stack is empty");
            }
        }
        // Once per turn (twice with AllowExtraLoyaltyActivationEffect, e.g. Oath of Teferi), plus any
        // one-shot extra activations granted to this planeswalker this turn (The Chain Veil).
        int maxActivations = (gameQueryService.hasExtraLoyaltyActivation(gameData, playerId) ? 2 : 1)
                + permanent.getExtraLoyaltyActivationsThisTurn();
        if (permanent.getLoyaltyActivationsThisTurn() >= maxActivations) {
            throw new IllegalStateException("Only one loyalty ability per planeswalker per turn");
        }

        int loyaltyCost;
        if (ability.isVariableLoyaltyCost()) {
            // Variable loyalty cost (-X): player chooses X via xValue, cost is -X
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (effectiveXValue > permanent.getCounterCount(CounterType.LOYALTY)) {
                throw new IllegalStateException("Not enough loyalty counters");
            }
            loyaltyCost = -effectiveXValue;
        } else {
            loyaltyCost = ability.getLoyaltyCost();
            // For negative loyalty costs, check sufficient loyalty
            if (loyaltyCost < 0 && permanent.getCounterCount(CounterType.LOYALTY) < Math.abs(loyaltyCost)) {
                throw new IllegalStateException("Not enough loyalty counters");
            }
        }
        return loyaltyCost;
    }

    /**
     * Counters on the source permanent that scale a {@link PayLifeCost} ("pay 3 life for each
     * velocity counter on this enchantment"). Zero for the fixed and half-life forms.
     */
    private int sourceCounterCount(Permanent permanent, PayLifeCost cost) {
        return cost.perSourceCounter() == null ? 0 : permanent.getCounterCount(cost.perSourceCounter());
    }

    private void payLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int effectiveXValue) {
        int loyaltyCost = validateLoyaltyCost(gameData, playerId, permanent, ability, effectiveXValue);
        permanent.setCounterCount(CounterType.LOYALTY, permanent.getCounterCount(CounterType.LOYALTY) + loyaltyCost);
        permanent.setLoyaltyActivationsThisTurn(permanent.getLoyaltyActivationsThisTurn() + 1);
        gameData.playersWhoActivatedLoyaltyAbilityThisTurn.add(playerId);
    }

    private int minimumWaterbendTaps(GameData gameData, UUID playerId, Permanent source,
                                     ActivatedAbility ability, int amount, boolean sourceMustBeTapped) {
        List<Permanent> eligible = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> !sourceMustBeTapped || !permanent.getId().equals(source.getId()))
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .toList();
        int maxTaps = Math.min(amount, eligible.size());
        for (int taps = 0; taps <= maxTaps; taps++) {
            if (canPayWaterbendMana(gameData, playerId, source, ability, amount - taps)) {
                return taps;
            }
        }
        return -1;
    }

    private boolean canPayWaterbendMana(GameData gameData, UUID playerId, Permanent source,
                                        ActivatedAbility ability, int amount) {
        if (amount <= 0) {
            return true;
        }
        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        if (manaPool == null) {
            return false;
        }
        ManaPool affordabilityPool = manaPool;
        if (gameQueryService.canSpendBlueManaAsAnyColorForActivatedAbilities(gameData, source)
                && !manaPool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            affordabilityPool = copyManaPool(manaPool);
            affordabilityPool.setBlueSpendableAsAnyColorForActivatedAbilities(true);
        }
        if (gameQueryService.isCreature(gameData, source)) {
            affordabilityPool = copyManaPool(affordabilityPool);
            affordabilityPool.promoteCreatureSpellOrAbilityMana();
        }
        boolean artifactContext = gameQueryService.isArtifact(source);
        boolean myrContext = source.getCard().getSubtypes().contains(CardSubtype.MYR);
        Set<CardSubtype> subtypeSpellOrAbilityContext = effectiveSpellOrAbilitySubtypes(gameData, source, ability);
        Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext = gameQueryService.isCreature(gameData, source)
                ? subtypeSpellOrAbilityContext : Set.of();
        boolean powerstoneContext = manaPool.getPowerstoneOnlyColorless() > 0;
        return new ManaCost("{" + amount + "}").canPay(affordabilityPool, 0,
                artifactContext, myrContext, false, false, false, null,
                subtypeSpellOrAbilityContext, false, artifactContext, false, false, Set.of(),
                subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext);
    }

    private void payWaterbendCost(GameData gameData, UUID playerId, Permanent source,
                                  ActivatedAbility ability, WaterbendCost cost,
                                  int announcedXValue, boolean sourceMustBeTapped) {
        int amount = cost.effectiveAmount(announcedXValue);
        int stackBeforeWaterbendTriggers = gameData.stack.size();
        List<Permanent> eligible = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> !sourceMustBeTapped || !permanent.getId().equals(source.getId()))
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .toList();
        int tapCount = minimumWaterbendTaps(
                gameData, playerId, source, ability, amount, sourceMustBeTapped);
        if (tapCount < 0) {
            throw new IllegalStateException("Not enough mana or untapped artifacts and creatures to pay waterbend cost");
        }
        for (int i = 0; i < tapCount; i++) {
            Permanent chosen = eligible.get(i);
            chosen.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " taps ", chosen.getCard(), " as a waterbend cost."));
        }

        int remaining = amount - tapCount;
        if (remaining > 0) {
            boolean artifactContext = gameQueryService.isArtifact(source);
            boolean myrContext = source.getCard().getSubtypes().contains(CardSubtype.MYR);
            Set<CardSubtype> subtypeSpellOrAbilityContext = effectiveSpellOrAbilitySubtypes(gameData, source, ability);
            Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext = gameQueryService.isCreature(gameData, source)
                    ? subtypeSpellOrAbilityContext : Set.of();
            payManaCost(gameData, playerId, "{" + remaining + "}", 0,
                    artifactContext, myrContext, subtypeSpellOrAbilityContext,
                    subtypeCreatureSourceSpellOrAbilityContext, 0, null);
        }
        triggerCollectionService.checkBendingTriggers(gameData, playerId, BendingType.WATERBEND);
        if (gameData.stack.size() > stackBeforeWaterbendTriggers) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(new ArrayList<>(
                    gameData.stack.subList(stackBeforeWaterbendTriggers, gameData.stack.size())));
            gameData.stack.subList(stackBeforeWaterbendTriggers, gameData.stack.size()).clear();
        }
    }

    private int validateCreatureManaPayment(GameData gameData, UUID playerId, Permanent source,
                                            ActivatedAbility ability, String abilityCost, int xValue,
                                            int additionalGenericCost,
                                            List<UUID> selectedCreatureManaPaymentIds) {
        boolean usesCreatureManaPayment = ability.getEffects().stream()
                .anyMatch(TapCreaturesForManaCost.class::isInstance);
        if (!usesCreatureManaPayment) {
            return 0;
        }

        int totalManaCost = additionalGenericCost;
        if (abilityCost != null) {
            ManaCost manaCost = new ManaCost(abilityCost);
            totalManaCost += manaCost.getManaValue();
            totalManaCost += xValue * manaCost.getXSymbolCount();
        }
        int maximumCreatureCount = Math.max(0, totalManaCost);
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<Permanent> eligibleCreatures = battlefield.stream()
                .filter(creature -> !creature.isTapped())
                .filter(creature -> !ability.isRequiresTap() || !creature.getId().equals(source.getId()))
                .filter(creature -> gameQueryService.isCreature(gameData, creature))
                .toList();

        if (selectedCreatureManaPaymentIds == null) {
            return Math.min(maximumCreatureCount, eligibleCreatures.size());
        }
        if (selectedCreatureManaPaymentIds.size() > maximumCreatureCount) {
            throw new IllegalStateException("Too many creatures selected to pay the ability's mana cost");
        }
        Set<UUID> distinctIds = new HashSet<>();
        for (UUID creatureId : selectedCreatureManaPaymentIds) {
            if (!distinctIds.add(creatureId)) {
                throw new IllegalStateException("A creature cannot be selected more than once");
            }
            Permanent creature = battlefield.stream()
                    .filter(candidate -> candidate.getId().equals(creatureId))
                    .findFirst()
                    .orElse(null);
            if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
                throw new IllegalStateException("You may select only creatures you control");
            }
            if (creature.isTapped()) {
                throw new IllegalStateException("Selected creature is already tapped");
            }
            if (ability.isRequiresTap() && creature.getId().equals(source.getId())) {
                throw new IllegalStateException("The source cannot pay both tap costs");
            }
        }
        return selectedCreatureManaPaymentIds.size();
    }

    private void payCreatureManaPayment(GameData gameData, Player player, List<UUID> creatureIds) {
        for (UUID creatureId : creatureIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null) {
                throw new IllegalStateException("Selected creature is no longer on the battlefield");
            }
            creature.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, creature);
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " taps ", creature.getCard(), " as a cost."));
        }
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext) {
        payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext, null, 0);
    }

    private void payManaCostForSourceCard(GameData gameData, UUID playerId, Card sourceCard,
                                           String abilityCost, int effectiveXValue,
                                           boolean artifactContext, boolean myrContext,
                                           int additionalCost) {
        if (!sourceCard.hasType(CardType.CREATURE)) {
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext,
                    null, additionalCost);
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);
        EnumMap<ManaColor, Integer> regularManaBefore = snapshotPoolColors(pool);
        EnumMap<ManaColor, Integer> promotedCreatureSourceMana = pool.promoteCreatureSpellOrAbilityMana();
        try {
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext,
                    null, additionalCost);
        } finally {
            pool.restorePromotedCreatureSpellOrAbilityMana(promotedCreatureSourceMana, regularManaBefore);
        }
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext, Set<CardSubtype> subtypeSpellOrAbilityContext, int additionalCost) {
        payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext,
                subtypeSpellOrAbilityContext, additionalCost, null);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue,
                             boolean artifactContext, boolean myrContext,
                             Set<CardSubtype> subtypeSpellOrAbilityContext, int additionalCost,
                             Set<ManaColor> xColorRestrictions) {
        payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext,
                subtypeSpellOrAbilityContext, Set.of(), additionalCost, xColorRestrictions);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue,
                             boolean artifactContext, boolean myrContext,
                             Set<CardSubtype> subtypeSpellOrAbilityContext,
                             Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                             int additionalCost, Set<ManaColor> xColorRestrictions) {
        ManaCost cost = new ManaCost(abilityCost);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (cost.hasX() && xColorRestrictions != null) {
            if (!cost.canPay(pool, effectiveXValue, xColorRestrictions, additionalCost)) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
            cost.pay(pool, effectiveXValue, xColorRestrictions, additionalCost);
            return;
        }
        boolean hasSubtypeSoa = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        boolean hasCreatureSourceSoa = subtypeCreatureSourceSpellOrAbilityContext != null
                && !subtypeCreatureSourceSpellOrAbilityContext.isEmpty();
        boolean powerstoneContext = pool.getPowerstoneOnlyColorless() > 0;
        boolean hasRestricted = artifactContext || myrContext || hasSubtypeSoa
                || hasCreatureSourceSoa || powerstoneContext;

        // Pay Phyrexian mana first so colored mana is reserved for Phyrexian symbols before
        // generic costs consume it — but only where the rest of the cost stays payable,
        // falling back to life otherwise (the legality pre-check assumes life is an option)
        int phyrexianLifeCost = 0;
        if (cost.hasPhyrexianMana()) {
            int restDemand = cost.hasX() ? effectiveXValue + additionalCost : additionalCost;
            phyrexianLifeCost = cost.payPhyrexianManaAuto(pool, restDemand);
        }

        if (cost.hasX()) {
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (hasRestricted) {
                if (!cost.canPay(pool, effectiveXValue + additionalCost, artifactContext, myrContext, false, false, false, null,
                        subtypeSpellOrAbilityContext, false, artifactContext, false, false, Set.of(),
                        subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost, artifactContext, myrContext, false, false, false, null,
                        subtypeSpellOrAbilityContext, false, artifactContext, false, false, Set.of(),
                        subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext);
            } else {
                if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost);
            }
        } else {
            if (hasRestricted) {
                if (!cost.canPay(pool, additionalCost, artifactContext, myrContext, false, false, false, null,
                        subtypeSpellOrAbilityContext, false, artifactContext, false, false, Set.of(),
                        subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, additionalCost, artifactContext, myrContext, false, false, false, null,
                        subtypeSpellOrAbilityContext, false, artifactContext, false, false, Set.of(),
                        subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext);
            } else {
                if (additionalCost != 0) {
                    // additionalCost may be negative (a static generic-cost reduction, floored to the
                    // generic portion by the caller so the net generic never goes below zero).
                    if (!cost.canPay(pool, additionalCost)) {
                        throw new IllegalStateException("Not enough mana to activate ability");
                    }
                    cost.pay(pool, additionalCost);
                } else {
                    if (!cost.canPay(pool)) {
                        throw new IllegalStateException("Not enough mana to activate ability");
                    }
                    cost.pay(pool);
                }
            }
        }

        if (phyrexianLifeCost > 0) {
            lifeSupport.applyLifePayment(gameData, playerId, phyrexianLifeCost, "Phyrexian mana");
        }
    }

    /**
     * Records the per-color mana just spent on this permanent's activation cost, so a
     * {@code NoteManaSpentForActivationEffect} on the ability can read it back when it resolves
     * (Ice Cauldron). Keyed by card id like the imprint map, and overwritten on every activation.
     */
    private void recordActivationManaSpent(GameData gameData, Permanent permanent,
                                           EnumMap<ManaColor, Integer> before, EnumMap<ManaColor, Integer> after,
                                           int treasureManaBefore, int treasureManaAfter) {
        EnumMap<ManaColor, Integer> spent = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = before.getOrDefault(color, 0) - after.getOrDefault(color, 0);
            if (amount > 0) {
                spent.put(color, amount);
            }
        }
        gameData.abilityActivationManaSpent.put(permanent.getCard().getId(), spent);
        gameData.abilityActivationUsedTreasureMana.put(permanent.getCard().getId(),
                treasureManaBefore > treasureManaAfter);
    }

    /**
     * The permanent's effective creature subtypes (base + transient + granted). Used as the context
     * for spell-or-ability restricted mana (e.g. Smokebraider) when paying an activated ability's cost.
     */
    private Set<CardSubtype> effectiveSubtypes(Permanent permanent) {
        Set<CardSubtype> subtypes = new HashSet<>(permanent.getCard().getSubtypes());
        subtypes.addAll(permanent.getTransientSubtypes());
        subtypes.addAll(permanent.getGrantedSubtypes());
        return subtypes;
    }

    private Set<CardSubtype> effectiveSpellOrAbilitySubtypes(GameData gameData, Permanent permanent,
                                                              ActivatedAbility ability) {
        Set<CardSubtype> subtypes = effectiveSubtypes(permanent);
        if (ability.getEffects().stream().anyMatch(EquipEffect.class::isInstance)) {
            subtypes.add(CardSubtype.EQUIPMENT);
        } else {
            subtypes.remove(CardSubtype.EQUIPMENT);
        }
        if (!gameQueryService.getEffectiveColors(gameData, permanent).isEmpty()) {
            subtypes.remove(CardSubtype.ELDRAZI);
        }
        return subtypes;
    }

    private ManaPool copyManaPool(ManaPool manaPool) {
        return manaPool instanceof VirtualManaPool virtualManaPool
                ? new VirtualManaPool(virtualManaPool)
                : new ManaPool(manaPool);
    }

    /**
     * Finds two cards in {@code hand} that share a color with each other (for
     * {@link RevealTwoCardsSharingColorCost}). Colorless cards share no color and never qualify.
     * Returns the qualifying pair, or {@code null} if none exists.
     */
    private List<Card> colorSharingPair(List<Card> hand) {
        if (hand == null) {
            return null;
        }
        for (int i = 0; i < hand.size(); i++) {
            List<CardColor> colorsA = hand.get(i).getColors();
            if (colorsA.isEmpty()) {
                continue;
            }
            for (int j = i + 1; j < hand.size(); j++) {
                if (hand.get(j).getColors().stream().anyMatch(colorsA::contains)) {
                    return List.of(hand.get(i), hand.get(j));
                }
            }
        }
        return null;
    }

    private List<Card> matchingHandCards(GameData gameData, UUID playerId, HandRevealCost cost) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        return hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, cost.filter(), null, gameData, playerId))
                .toList();
    }

    private void validateHandRevealSelection(GameData gameData, UUID playerId, HandRevealCost cost,
                                             int requiredCount, List<UUID> selectedIds) {
        if (selectedIds == null || selectedIds.size() != requiredCount
                || new HashSet<>(selectedIds).size() != selectedIds.size()) {
            throw new IllegalStateException("Must select exactly " + requiredCount + " cards to reveal");
        }
        List<Card> matchingCards = matchingHandCards(gameData, playerId, cost);
        Set<UUID> validIds = matchingCards.stream().map(Card::getId).collect(Collectors.toSet());
        if (!validIds.containsAll(selectedIds)) {
            throw new IllegalStateException("Selected card is not a valid hand reveal cost");
        }
    }

    private void payHandRevealCost(GameData gameData, UUID playerId, HandRevealCost cost,
                                   List<UUID> selectedIds) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        Set<UUID> selectedSet = new HashSet<>(selectedIds);
        List<Card> selectedCards = hand.stream()
                .filter(card -> selectedSet.contains(card.getId()))
                .toList();
        if (selectedCards.isEmpty()) {
            return;
        }
        String playerName = gameData.playerIdToName.get(playerId);
        GameLog.Builder reveal = GameLog.builder().text(playerName + " reveals ");
        for (int i = 0; i < selectedCards.size(); i++) {
            if (i > 0) {
                reveal.text(", ");
            }
            reveal.card(selectedCards.get(i));
        }
        gameLogService.append(gameData, reveal.text(" as a cost.").build());
        cardRevealService.revealToAllPlayers(
                gameData, playerId, GameEventFact.RevealZone.HAND, selectedCards);
    }

    private List<Integer> collectDiscardIndices(GameData gameData, UUID playerId, List<Card> hand,
                                                HandCardCost cost, int xValue) {
        return collectDiscardIndices(gameData, playerId, hand, cost, xValue, null);
    }

    /**
     * Hand indices that may legally pay {@code cost} right now.
     *
     * <p>For a same-name cost (Sphinx of the Chimes) the first pick is narrowed to cards whose name
     * appears at least {@link HandCardCost#count()} times among the otherwise-legal cards, so a lone
     * copy can never be chosen and strand the activation; once the first card is chosen,
     * {@code requiredName} pins every remaining pick to that name.
     */
    private List<Integer> collectDiscardIndices(GameData gameData, UUID playerId, List<Card> hand,
                                                HandCardCost cost, int xValue, String requiredName) {
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (cost.manaValueEqualsX() && card.getManaValue() != xValue) {
                continue;
            }
            if (requiredName != null && !requiredName.equals(card.getName())) {
                continue;
            }
            if (!cost.isEligible(gameData, playerId, card)) {
                continue;
            }
            if (cost.predicate() == null || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null)) {
                validIndices.add(i);
            }
        }
        int requiredCount = cost.requiredCount(xValue);
        if (cost.sameName() && requiredName == null && requiredCount > 1) {
            Map<String, Long> countsByName = validIndices.stream()
                    .collect(Collectors.groupingBy(i -> hand.get(i).getName(), Collectors.counting()));
            validIndices.removeIf(i -> countsByName.get(hand.get(i).getName()) < requiredCount);
        }
        return validIndices;
    }

    private void beginDiscardCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                        UUID targetId, Zone targetZone, List<UUID> targetIds,
                                        Map<UUID, Integer> damageAssignments, String costLabel, List<Integer> validDiscardIndices,
                                        int remainingDiscards, String payVerb) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
                targetZone,
                costLabel,
                remainingDiscards,
                null,
                targetIds,
                damageAssignments
        );
        String labelText = costLabel != null ? costLabel + " " : "";
        String prompt = remainingDiscards > 1
                ? "Choose a " + labelText + "card to " + payVerb + " as an activation cost ("
                + remainingDiscards + " remaining)."
                : "Choose a " + labelText + "card to " + payVerb + " as an activation cost.";
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice(
                playerId, validDiscardIndices, prompt));
    }

    private PaidHandCard payDiscardCost(GameData gameData, Player player, HandCardCost cost, Integer discardCardIndex,
                                        int xValue) {
        return payDiscardCost(gameData, player, cost, discardCardIndex, xValue, null, null);
    }

    private PaidHandCard payDiscardCost(GameData gameData, Player player, HandCardCost cost, Integer discardCardIndex,
                                        int xValue, String requiredName) {
        return payDiscardCost(gameData, player, cost, discardCardIndex, xValue, requiredName, null);
    }

    /**
     * Pays one card of a hand-card cost and returns the paid card's name and mana value, so a
     * same-name cost can pin its remaining picks and a trackManaValue cost can snapshot into xValue.
     *
     * <p>An {@code imprintOnSource} discard cost also remembers the paid card on {@code sourceCard},
     * so the ability's own effects can ask what was discarded (Necromancer's Stockpile).
     */
    private PaidHandCard payDiscardCost(GameData gameData, Player player, HandCardCost cost, Integer discardCardIndex,
                                        int xValue, String requiredName, Card sourceCard) {
        if (discardCardIndex == null) {
            throw new IllegalStateException("Must choose a card to " + cost.payVerb());
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validDiscardIndices = collectDiscardIndices(gameData, player.getId(), hand, cost,
                xValue, requiredName);
        Set<Integer> validSet = new HashSet<>(validDiscardIndices);
        if (!validSet.contains(discardCardIndex)) {
            String costLabel = cost.label() != null ? cost.label() + " " : "";
            throw new IllegalStateException("Must " + cost.payVerb() + " a " + costLabel + "card");
        }

        Card paid = hand.remove((int) discardCardIndex);
        int manaValue = paid.getManaValue();
        if (sourceCard != null && cost.imprintOnSource()) {
            gameData.setImprintedCard(sourceCard, paid);
        }
        if (cost.exilesPaidCards()) {
            exileService.exileCard(gameData, player.getId(), paid);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", paid, " from their hand as an activation cost."));
            log.info("Game {} - {} exiles {} from hand as activation cost", gameData.id, player.getUsername(), paid.getName());
            return new PaidHandCard(paid.getName(), manaValue);
        }

        if (cost.putsPaidCardsOnTopOfLibrary()) {
            gameData.playerDecks.get(player.getId()).addFirst(paid);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", paid, " on top of their library as an activation cost."));
            log.info("Game {} - {} puts {} from hand on top of their library as an activation cost",
                    gameData.id, player.getUsername(), paid.getName());
            return new PaidHandCard(paid.getName(), manaValue);
        }

        graveyardService.addCardToGraveyard(gameData, player.getId(), paid);
        gameData.discardCausedByOpponent = false;
        collectDiscardTriggersAsAbilityCost(gameData, player.getId(), paid);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " discards " , paid, " as an activation cost."));
        log.info("Game {} - {} discards {} as activation cost", gameData.id, player.getUsername(), paid.getName());
        return new PaidHandCard(paid.getName(), manaValue);
    }

    private record PaidHandCard(String name, int manaValue) {
    }

    private void collectDiscardTriggersAsAbilityCost(GameData gameData, UUID playerId, Card discardedCard) {
        collectDiscardTriggersAsAbilityCost(gameData, playerId, discardedCard, false);
    }

    private void collectDiscardTriggersAsAbilityCost(GameData gameData, UUID playerId, Card discardedCard,
                                                     boolean cycled) {
        int stackBefore = gameData.stack.size();
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, discardedCard);
        if (cycled) {
            triggerCollectionService.checkCycleTriggers(gameData, playerId, discardedCard);
        }
        if (gameData.stack.size() > stackBefore) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(
                    new ArrayList<>(gameData.stack.subList(stackBefore, gameData.stack.size())));
            gameData.stack.subList(stackBefore, gameData.stack.size()).clear();
        }
    }

    private void flushActivatedAbilityCostTriggers(GameData gameData) {
        if (!gameData.pendingActivatedAbilityCostTriggers.isEmpty()) {
            gameData.stack.addAll(gameData.pendingActivatedAbilityCostTriggers);
            gameData.pendingActivatedAbilityCostTriggers.clear();
        }
    }

    private Card payExileTopOfLibraryCost(GameData gameData, UUID playerId, Permanent permanent,
                                          ExileTopCardOfLibraryCost cost) {
        int count = cost.count();
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        Card lastExiledCard = null;
        for (int i = 0; i < count; i++) {
            if (deck == null || deck.isEmpty()) {
                throw new IllegalStateException("Not enough cards in library to exile (need " + count + ")");
            }
            Card topCard = deck.removeFirst();
            lastExiledCard = topCard;
            exileService.exileCard(gameData, playerId, topCard);
            // Remember the exiled card so the ability's effect can look at it at resolution
            // (Storm Elemental's "if the exiled card is a snow land").
            if (cost.imprintOnSource()) {
                gameData.setImprintedCard(permanent.getCard(), topCard);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " exiles ", topCard, " from the top of their library as a cost."));
            log.info("Game {} - {} exiles {} from library top as activation cost",
                    gameData.id, playerName, topCard.getName());
        }
        return lastExiledCard;
    }

    private void payDiscardHandCost(GameData gameData, Player player) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = false;
        for (Card card : discarded) {
            graveyardService.addCardToGraveyard(gameData, playerId, card);
            collectDiscardTriggersAsAbilityCost(gameData, playerId, card);
        }

        String logEntry = player.getUsername() + " discards their hand (" + discarded.size()
                + " card" + (discarded.size() != 1 ? "s" : "") + ") as an activation cost.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} discards hand of {} cards as activation cost", gameData.id, player.getUsername(), discarded.size());
    }

    private Card payRandomDiscardCost(GameData gameData, Player player, int count) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return null;
        }

        Card lastDiscarded = null;
        for (int i = 0; i < count && !hand.isEmpty(); i++) {
            Card discarded = hand.remove(ThreadLocalRandom.current().nextInt(hand.size()));
            lastDiscarded = discarded;
            graveyardService.addCardToGraveyard(gameData, playerId, discarded);
            gameData.discardCausedByOpponent = false;
            collectDiscardTriggersAsAbilityCost(gameData, playerId, discarded);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " discards " , discarded, " at random as an activation cost."));
            log.info("Game {} - {} discards {} at random as activation cost", gameData.id, player.getUsername(), discarded.getName());
        }
        return lastDiscarded;
    }

    private List<Integer> collectGraveyardIndicesForType(List<Card> graveyard, CardType requiredType, CardSubtype requiredSubtype) {
        return collectGraveyardIndicesForType(graveyard, requiredType, null, requiredSubtype);
    }

    private List<Integer> collectGraveyardIndicesForType(List<Card> graveyard, CardType requiredType, CardType alternateType,
                                                         CardSubtype requiredSubtype) {
        List<Integer> validIndices = new ArrayList<>();
        if (graveyard == null) {
            return validIndices;
        }
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            boolean typeMatch = requiredType == null || card.getType() == requiredType
                    || (alternateType != null && card.getType() == alternateType);
            boolean subtypeMatch = requiredSubtype == null || card.getSubtypes().contains(requiredSubtype);
            if (typeMatch && subtypeMatch) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private List<UUID> collectExileInstantOrSorcerySpellIds(GameData gameData, UUID playerId) {
        return gameData.stack.stream()
                .filter(entry -> (entry.getEntryType() == StackEntryType.INSTANT_SPELL
                        || entry.getEntryType() == StackEntryType.SORCERY_SPELL)
                        && playerId.equals(entry.getControllerId()))
                .map(entry -> entry.getCard().getId())
                .toList();
    }

    private void payExileInstantOrSorcerySpellCost(GameData gameData, Player player, UUID cardId) {
        StackEntry entry = gameQueryService.findStackEntryByCardId(gameData, cardId);
        if (entry == null
                || (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL)
                || !player.getId().equals(entry.getControllerId())) {
            throw new IllegalStateException("Must exile an instant or sorcery spell you control from the stack");
        }

        gameData.stack.remove(entry);
        if (!entry.isCopy()) {
            gameData.addToExile(entry.getOwnerId(), entry.getPhysicalCard());
        }
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", entry.getCard(), " from the stack as an activation cost."));
    }

    private void payPutCardExiledWithSourceIntoGraveyardCost(GameData gameData, Player player,
                                                              Permanent source, UUID targetId,
                                                              Zone targetZone) {
        if (targetZone != Zone.EXILE || targetId == null) {
            throw new IllegalStateException("Choose a card exiled with this permanent");
        }
        ExiledCardEntry exiled = gameData.findExiledCard(targetId);
        if (exiled == null || !source.getId().equals(exiled.sourcePermanentId())) {
            throw new IllegalStateException("Card was not exiled with this permanent");
        }
        if (!gameData.removeFromExile(targetId)) {
            throw new IllegalStateException("Card is no longer in exile");
        }
        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " puts ", exiled.card(), " into its owner's graveyard as an activation cost."));
    }

    /**
     * Collects the cards in {@code graveyard} that can pay an {@link ExileNCardsFromGraveyardCost} of
     * {@code requiredType} (null = any type), excluding {@code sourceCard}. Uses {@code hasType} so an
     * artifact creature counts as an artifact card. The source is excluded so a graveyard-activated
     * ability that returns itself (Salvage Titan) never exiles the very card it means to bring back.
     */
    private List<Card> matchingGraveyardExileCandidates(List<Card> graveyard,
                                                        ExileNCardsFromGraveyardCost cost,
                                                        Card sourceCard) {
        List<Card> candidates = new ArrayList<>();
        if (graveyard == null) {
            return candidates;
        }
        for (Card card : graveyard) {
            if (card == sourceCard) {
                continue;
            }
            if ((cost.requiredType() == null || card.hasType(cost.requiredType()))
                    && (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null))) {
                candidates.add(card);
            }
        }
        return candidates;
    }

    private List<Card> matchingGraveyardExileCandidates(List<Card> graveyard,
                                                        ExileXCardsFromGraveyardCost cost,
                                                        Card sourceCard) {
        List<Card> candidates = new ArrayList<>();
        if (graveyard == null) {
            return candidates;
        }
        for (Card card : graveyard) {
            if (card != sourceCard && (cost.requiredType() == null || card.hasType(cost.requiredType()))) {
                candidates.add(card);
            }
        }
        return candidates;
    }

    private List<Card> collectAnySingleGraveyardExileCandidates(
            GameData gameData, ExileNCardsFromSingleGraveyardCost cost) {
        boolean canPayFromOneGraveyard = gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> matchingSingleGraveyardExileCandidates(graveyard, cost).size() >= cost.count());
        if (!canPayFromOneGraveyard) {
            return List.of();
        }

        List<Card> candidates = new ArrayList<>();
        boolean eligibleGraveyardFound = false;
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            List<Card> matchingCards = matchingSingleGraveyardExileCandidates(graveyard, cost);
            if (matchingCards.size() >= cost.count() && !eligibleGraveyardFound) {
                candidates.addAll(matchingCards);
                eligibleGraveyardFound = true;
            }
        }
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            List<Card> matchingCards = matchingSingleGraveyardExileCandidates(graveyard, cost);
            if (matchingCards.size() < cost.count() || !candidates.containsAll(matchingCards)) {
                candidates.addAll(matchingCards);
            }
        }
        return candidates;
    }

    private List<Card> collectAnyGraveyardExileCandidates(
            GameData gameData, ExileCardFromGraveyardCost cost) {
        List<Card> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                boolean typeMatch = cost.requiredType() == null || card.getType() == cost.requiredType()
                        || (cost.alternateType() != null && card.getType() == cost.alternateType());
                boolean subtypeMatch = cost.requiredSubtype() == null
                        || card.getSubtypes().contains(cost.requiredSubtype());
                if (typeMatch && subtypeMatch) {
                    candidates.add(card);
                }
            }
        }
        return candidates;
    }

    private List<Card> matchingSingleGraveyardExileCandidates(
            List<Card> graveyard, ExileNCardsFromSingleGraveyardCost cost) {
        List<Card> candidates = new ArrayList<>();
        if (graveyard == null) {
            return candidates;
        }
        for (Card card : graveyard) {
            if ((cost.requiredType() == null || card.hasType(cost.requiredType()))
                    && (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null))) {
                candidates.add(card);
            }
        }
        return candidates;
    }

    private boolean cardsAreFromSingleGraveyard(GameData gameData, List<UUID> cardIds) {
        UUID graveyardOwnerId = null;
        for (UUID cardId : cardIds) {
            UUID ownerId = findGraveyardOwner(gameData, cardId);
            if (ownerId == null) {
                return false;
            }
            if (graveyardOwnerId == null) {
                graveyardOwnerId = ownerId;
            } else if (!graveyardOwnerId.equals(ownerId)) {
                return false;
            }
        }
        return graveyardOwnerId != null;
    }

    private UUID findGraveyardOwner(GameData gameData, UUID cardId) {
        for (Map.Entry<UUID, List<Card>> entry : gameData.playerGraveyards.entrySet()) {
            if (entry.getValue().stream().anyMatch(card -> card.getId().equals(cardId))) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void validateAnySingleGraveyardExileSelection(
            GameData gameData, ExileNCardsFromSingleGraveyardCost cost, List<UUID> cardIds) {
        if (cardIds == null || cardIds.size() != cost.count()
                || new HashSet<>(cardIds).size() != cardIds.size()
                || !cardsAreFromSingleGraveyard(gameData, cardIds)) {
            throw new IllegalStateException("Must choose exactly " + cost.count()
                    + " matching cards from a single graveyard");
        }

        UUID graveyardOwnerId = findGraveyardOwner(gameData, cardIds.get(0));
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        Set<UUID> selectedIds = Set.copyOf(cardIds);
        List<Card> matchingCards = matchingSingleGraveyardExileCandidates(graveyard, cost);
        if (matchingCards.stream().map(Card::getId).filter(selectedIds::contains).count() != cost.count()) {
            throw new IllegalStateException("Selected card is no longer a valid graveyard exile cost");
        }
    }

    private void validatePutBottomLibraryCostSelection(
            GameData gameData, UUID playerId, PutCardsFromGraveyardOnBottomOfLibraryCost cost,
            List<UUID> cardIds) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        if (cardIds == null || cardIds.size() != cost.count()
                || new HashSet<>(cardIds).size() != cardIds.size()
                || cardIds.stream().anyMatch(cardId -> graveyard.stream()
                .noneMatch(card -> card.getId().equals(cardId)))) {
            throw new IllegalStateException("Must choose exactly " + cost.count()
                    + " cards from your graveyard");
        }
    }

    private void payAnySingleGraveyardExileCost(
            GameData gameData, Player player, ExileNCardsFromSingleGraveyardCost cost, List<UUID> cardIds) {
        validateAnySingleGraveyardExileSelection(gameData, cost, cardIds);

        UUID graveyardOwnerId = findGraveyardOwner(gameData, cardIds.get(0));
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        List<Card> toExile = cardIds.stream()
                .map(cardId -> graveyard.stream()
                        .filter(card -> card.getId().equals(cardId))
                        .findFirst()
                        .orElseThrow())
                .toList();
        graveyard.removeAll(toExile);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, graveyardOwnerId, toExile);
        for (Card exiled : toExile) {
            exileService.exileCard(gameData, graveyardOwnerId, exiled);
        }
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " exiles "
                + toExile.size() + " creature cards from a single graveyard as an activation cost."));
    }

    private void payPutCardsFromGraveyardOnBottomOfLibraryCost(
            GameData gameData, Player player, PutCardsFromGraveyardOnBottomOfLibraryCost cost,
            List<UUID> cardIds) {
        validatePutBottomLibraryCostSelection(gameData, player.getId(), cost, cardIds);

        List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
        List<Card> selectedCards = cardIds.stream()
                .map(cardId -> graveyard.stream()
                        .filter(card -> card.getId().equals(cardId))
                        .findFirst()
                        .orElseThrow())
                .toList();
        for (int i = 0; i < cardIds.size(); i++) {
            Card card = selectedCards.get(i);
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardIds.get(i));
            gameData.playerDecks.get(player.getId()).addLast(card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " on the bottom of their library."));
        }
    }

    private void payGraveyardExileNCost(GameData gameData, Player player, ExileNCardsFromGraveyardCost cost, Card sourceCard) {
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> candidates = matchingGraveyardExileCandidates(graveyard, cost, sourceCard);
        if (candidates.size() < cost.count()) {
            throw new IllegalStateException("Not enough cards in graveyard to exile");
        }
        List<Card> toExile = new ArrayList<>(candidates.subList(0, cost.count()));
        exileGraveyardCardsAsActivationCost(gameData, player, cost, toExile);
    }

    private void payChosenGraveyardExileNCost(
            GameData gameData, Player player, ExileNCardsFromGraveyardCost cost,
            List<UUID> cardIds, Card sourceCard) {
        UUID playerId = player.getId();
        validateControllerGraveyardExileSelection(gameData, playerId, cost, cardIds, sourceCard);
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> toExile = cardIds.stream()
                .map(cardId -> graveyard.stream()
                        .filter(card -> card.getId().equals(cardId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Selected graveyard card is no longer available")))
                .toList();
        exileGraveyardCardsAsActivationCost(gameData, player, cost, toExile);
    }

    private void validateControllerGraveyardExileSelection(
            GameData gameData, UUID playerId, ExileNCardsFromGraveyardCost cost,
            List<UUID> cardIds, Card sourceCard) {
        if (cardIds == null || cardIds.size() != cost.count()
                || new HashSet<>(cardIds).size() != cardIds.size()) {
            throw new IllegalStateException("Must choose exactly " + cost.count()
                    + " distinct graveyard cards to exile");
        }
        List<Card> candidates = matchingGraveyardExileCandidates(
                gameData.playerGraveyards.get(playerId), cost, sourceCard);
        Set<UUID> candidateIds = candidates.stream().map(Card::getId).collect(Collectors.toSet());
        if (!candidateIds.containsAll(cardIds)) {
            throw new IllegalStateException("Must choose matching cards from your graveyard");
        }
    }

    private void exileGraveyardCardsAsActivationCost(
            GameData gameData, Player player, ExileNCardsFromGraveyardCost cost, List<Card> toExile) {
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        graveyard.removeAll(toExile);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, toExile);
        for (Card exiled : toExile) {
            exileService.exileCard(gameData, playerId, exiled);
        }
        String typeName = graveyardExileFilterLabel(cost.requiredType(), null);
        String logEntry = player.getUsername() + " exiles " + toExile.size() + " " + typeName
                + "card" + (toExile.size() != 1 ? "s" : "") + " from graveyard as an activation cost.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} {}cards from graveyard as activation cost",
                gameData.id, player.getUsername(), toExile.size(), typeName);
    }

    private void payGraveyardExileXCost(GameData gameData, Player player, ExileXCardsFromGraveyardCost cost,
                                        int count, Card sourceCard) {
        payGraveyardExileXCost(gameData, player, cost, count, sourceCard, null);
    }

    private void payGraveyardExileXCost(GameData gameData, Player player, ExileXCardsFromGraveyardCost cost,
                                        int count, Card sourceCard, UUID sourcePermanentId) {
        if (count < 0) {
            throw new IllegalStateException("X value cannot be negative");
        }
        if (count == 0) {
            return;
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> candidates = matchingGraveyardExileCandidates(graveyard, cost, sourceCard);
        if (candidates.size() < count) {
            throw new IllegalStateException("Not enough cards in graveyard to exile");
        }
        List<Card> toExile = new ArrayList<>(candidates.subList(0, count));
        graveyard.removeAll(toExile);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, toExile);
        for (Card exiled : toExile) {
            if (sourcePermanentId == null) {
                exileService.exileCard(gameData, playerId, exiled);
            } else {
                exileService.exileCard(gameData, playerId, exiled, sourcePermanentId);
            }
        }
        String typeName = graveyardExileFilterLabel(cost.requiredType(), null);
        String logEntry = player.getUsername() + " exiles " + toExile.size() + " " + typeName
                + "card" + (toExile.size() != 1 ? "s" : "") + " from graveyard as an activation cost.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} {}cards from graveyard as activation cost",
                gameData.id, player.getUsername(), toExile.size(), typeName);
    }

    private void payChosenGraveyardExileXCost(GameData gameData, Player player,
                                               ExileXCardsFromGraveyardCost cost,
                                               List<Integer> selectedIndices) {
        payChosenGraveyardExileXCost(gameData, player, cost, selectedIndices, null);
    }

    private void payChosenGraveyardExileXCost(GameData gameData, Player player,
                                               ExileXCardsFromGraveyardCost cost,
                                               List<Integer> selectedIndices,
                                               UUID sourcePermanentId) {
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            throw new IllegalStateException("Selected graveyard cards are no longer available");
        }

        List<Card> toExile = new ArrayList<>();
        for (int index : selectedIndices) {
            if (index < 0 || index >= graveyard.size()) {
                throw new IllegalStateException("Selected graveyard cards are no longer available");
            }
            Card selected = graveyard.get(index);
            if (cost.requiredType() != null && !selected.hasType(cost.requiredType())) {
                throw new IllegalStateException("Selected card is no longer a valid graveyard exile cost");
            }
            toExile.add(selected);
        }
        if (new HashSet<>(toExile).size() != toExile.size()) {
            throw new IllegalStateException("Duplicate graveyard card selection");
        }

        graveyard.removeAll(toExile);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, toExile);
        for (Card exiled : toExile) {
            if (sourcePermanentId == null) {
                exileService.exileCard(gameData, playerId, exiled);
            } else {
                exileService.exileCard(gameData, playerId, exiled, sourcePermanentId);
            }
        }
        String typeName = graveyardExileFilterLabel(cost.requiredType(), null);
        String logEntry = player.getUsername() + " exiles " + toExile.size() + " " + typeName
                + "card" + (toExile.size() != 1 ? "s" : "") + " from graveyard as an activation cost.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} {}cards from graveyard as activation cost",
                gameData.id, player.getUsername(), toExile.size(), typeName);
    }

    private void validateCollectEvidenceSelection(GameData gameData, UUID playerId,
                                                  CollectEvidenceCost cost, List<UUID> cardIds) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            throw new IllegalStateException("Selected graveyard cards are no longer available");
        }

        int totalManaValue = 0;
        for (UUID cardId : cardIds) {
            int index = indexOfCard(graveyard, cardId);
            if (index < 0) {
                throw new IllegalStateException("Selected card is no longer a valid evidence cost");
            }
            totalManaValue += graveyard.get(index).getManaValue();
        }
        if (totalManaValue < cost.minimumManaValue()) {
            throw new IllegalStateException("Selected cards do not have enough total mana value to collect evidence");
        }
    }

    private int indexOfCard(List<Card> cards, UUID cardId) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(cardId)) {
                return i;
            }
        }
        return -1;
    }

    private void payCollectEvidenceCost(GameData gameData, Player player, CollectEvidenceCost cost,
                                        List<Integer> selectedIndices) {
        int stackBefore = gameData.stack.size();
        if (selectedIndices == null) {
            throw new IllegalStateException("No cards selected for evidence cost");
        }
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            throw new IllegalStateException("Selected graveyard cards are no longer available");
        }

        List<Card> toExile = new ArrayList<>();
        for (int index : selectedIndices) {
            if (index < 0 || index >= graveyard.size()) {
                throw new IllegalStateException("Selected graveyard cards are no longer available");
            }
            toExile.add(graveyard.get(index));
        }
        if (new HashSet<>(toExile).size() != toExile.size()) {
            throw new IllegalStateException("Duplicate graveyard card selection");
        }
        int totalManaValue = toExile.stream().mapToInt(Card::getManaValue).sum();
        if (totalManaValue < cost.minimumManaValue()) {
            throw new IllegalStateException("Selected cards do not have enough total mana value to collect evidence");
        }

        graveyard.removeAll(toExile);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, toExile);
        for (Card exiled : toExile) {
            exileService.exileCard(gameData, playerId, exiled);
        }
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " exiles "
                + toExile.size() + " cards from their graveyard to collect evidence."));
        triggerCollectionService.checkCollectEvidenceTriggers(gameData, player.getId());
        if (gameData.stack.size() > stackBefore) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(
                    new ArrayList<>(gameData.stack.subList(stackBefore, gameData.stack.size())));
            gameData.stack.subList(stackBefore, gameData.stack.size()).clear();
        }
    }

    /**
     * Pays an "Exile the top card of your graveyard" activation cost. The top card is the one most
     * recently put into the graveyard, i.e. the last element of the append-ordered list.
     */
    private void payTopOfGraveyardExileCost(GameData gameData, Player player, CardType requiredType) {
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        Card exiled = topMatchingGraveyardCard(graveyard, requiredType);
        if (exiled == null) {
            throw new IllegalStateException("No card in graveyard to exile");
        }
        graveyard.remove(exiled);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiled);
        exileService.exileCard(gameData, playerId, exiled);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", exiled, " from the top of their graveyard as an activation cost."));
    }

    /** Topmost (most recently added) graveyard card matching {@code requiredType}; null = any. */
    private Card topMatchingGraveyardCard(List<Card> graveyard, CardType requiredType) {
        if (graveyard == null) {
            return null;
        }
        for (int i = graveyard.size() - 1; i >= 0; i--) {
            Card card = graveyard.get(i);
            if (requiredType == null || card.hasType(requiredType)) {
                return card;
            }
        }
        return null;
    }

    private String graveyardExileFilterLabel(CardType requiredType, CardSubtype requiredSubtype) {
        return graveyardExileFilterLabel(requiredType, null, requiredSubtype);
    }

    private String graveyardExileFilterLabel(CardType requiredType, CardType alternateType, CardSubtype requiredSubtype) {
        if (requiredSubtype != null) {
            return requiredSubtype.getDisplayName() + " ";
        }
        if (requiredType != null) {
            String label = requiredType.name().toLowerCase();
            if (alternateType != null) {
                label += " or " + alternateType.name().toLowerCase();
            }
            return label + " ";
        }
        return "";
    }

    private void beginGraveyardExileCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                               UUID targetId, Zone targetZone, List<UUID> targetIds,
                                               Map<UUID, Integer> damageAssignments, ExileCardFromGraveyardCost cost,
                                               List<Integer> validExileIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
                targetZone,
                null,
                1,
                null,
                targetIds,
                damageAssignments
        );
        String typeName = graveyardExileFilterLabel(cost.requiredType(), cost.alternateType(), cost.requiredSubtype());
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.GraveyardExileCostChoice(
                playerId, validExileIndices,
                "Choose a " + typeName + "card from your graveyard to exile as an activation cost."));
    }

    private Card payGraveyardExileCost(GameData gameData, Player player, ExileCardFromGraveyardCost cost,
                                       Integer exileCardIndex) {
        if (exileCardIndex == null) {
            throw new IllegalStateException("Must choose a card to exile from graveyard");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, cost.requiredType(),
                cost.alternateType(), cost.requiredSubtype());
        Set<Integer> validSet = new HashSet<>(validExileIndices);
        if (!validSet.contains(exileCardIndex)) {
            String typeName = graveyardExileFilterLabel(cost.requiredType(), cost.alternateType(), cost.requiredSubtype());
            throw new IllegalStateException("Must exile a " + typeName + "card from your graveyard");
        }

        Card exiled = graveyard.remove((int) exileCardIndex);
        int stackBeforeCostTriggers = gameData.stack.size();
        graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, exiled);
        if (gameData.stack.size() > stackBeforeCostTriggers) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(
                    new ArrayList<>(gameData.stack.subList(stackBeforeCostTriggers, gameData.stack.size())));
            gameData.stack.subList(stackBeforeCostTriggers, gameData.stack.size()).clear();
        }
        exileService.exileCard(gameData, playerId, exiled);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles " , exiled, " from graveyard as an activation cost."));
        log.info("Game {} - {} exiles {} from graveyard as activation cost", gameData.id, player.getUsername(), exiled.getName());
        return exiled;
    }

    private Card payAnyGraveyardExileCost(GameData gameData, Player player,
                                           ExileCardFromGraveyardCost cost,
                                           List<UUID> selectedCardIds) {
        if (selectedCardIds == null || selectedCardIds.size() != 1) {
            throw new IllegalStateException("Must choose a card to exile from a graveyard");
        }

        UUID selectedCardId = selectedCardIds.getFirst();
        Card selected = collectAnyGraveyardExileCandidates(gameData, cost).stream()
                .filter(card -> card.getId().equals(selectedCardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Must exile a matching card from a graveyard"));
        UUID graveyardOwnerId = findGraveyardOwner(gameData, selectedCardId);
        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        graveyard.remove(selected);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, graveyardOwnerId, selected);
        exileService.exileCard(gameData, graveyardOwnerId, selected);

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles ", selected,
                " from graveyard as an activation cost."));
        log.info("Game {} - {} exiles {} from a graveyard as activation cost",
                gameData.id, player.getUsername(), selected.getName());
        return selected;
    }

    private void clearPendingAbilityActivation(GameData gameData) {
        gameData.pendingAbilityActivation = null;
        gameData.interaction.clearAwaitingInput();
    }

    private void deferActivatedAbilityCostTriggers(GameData gameData, int stackSizeBeforeCosts) {
        if (gameData.stack.size() > stackSizeBeforeCosts) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(
                    new ArrayList<>(gameData.stack.subList(stackSizeBeforeCosts, gameData.stack.size())));
            gameData.stack.subList(stackSizeBeforeCosts, gameData.stack.size()).clear();
        }
    }

    private void validateHandActivationLimitPerTurn(GameData gameData, Card card,
                                                     ActivatedAbility ability, int abilityIndex) {
        Integer maxActivationsPerTurn = ability.getMaxActivationsPerTurn();
        if (maxActivationsPerTurn == null) {
            return;
        }

        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(card.getId());
        int currentCount = perAbilityCounts != null ? perAbilityCounts.getOrDefault(abilityIndex, 0) : 0;
        if (currentCount >= maxActivationsPerTurn) {
            if (maxActivationsPerTurn == 1) {
                throw new IllegalStateException("This ability can be activated only once each turn");
            }
            throw new IllegalStateException("This ability can be activated no more than "
                    + maxActivationsPerTurn + " times each turn");
        }
    }

    private void recordHandAbilityActivationUse(GameData gameData, Card card, int abilityIndex) {
        gameData.activatedAbilityUsesThisTurn
                .computeIfAbsent(card.getId(), ignored -> new ConcurrentHashMap<>())
                .merge(abilityIndex, 1, Integer::sum);
    }

    private void validateActivationLimitPerTurn(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int abilityIndex) {
        validateActivationLimitPerGame(gameData, playerId, permanent, ability, abilityIndex);

        Integer maxActivationsPerTurn = ability.getMaxActivationsPerTurn();
        if (ability.isBoast() && maxActivationsPerTurn != null
                && gameQueryService.hasExtraBoastActivation(gameData, playerId)) {
            maxActivationsPerTurn++;
        }
        if (maxActivationsPerTurn == null && ability.getMaxActivationsPerTurnAmount() == null) {
            return;
        }

        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(permanent.getId());
        int currentCount = perAbilityCounts != null ? perAbilityCounts.getOrDefault(abilityIndex, 0) : 0;

        if (ability.getMaxActivationsPerTurnAmount() != null) {
            // Cap recomputed from the current board at every activation (Withering Wisps).
            int dynamicCap = amountEvaluationService.evaluate(gameData, ability.getMaxActivationsPerTurnAmount(),
                    new AmountContext(playerId, permanent, null, 0, 0));
            if (currentCount >= dynamicCap) {
                throw new IllegalStateException("This ability can be activated no more times each turn than "
                        + (ability.getMaxActivationsPerTurnDescription() != null
                                ? ability.getMaxActivationsPerTurnDescription()
                                : "the current limit") + " (" + dynamicCap + ")");
            }
        }

        if (maxActivationsPerTurn != null && currentCount >= maxActivationsPerTurn) {
            if (maxActivationsPerTurn == 1) {
                throw new IllegalStateException("This ability can be activated only once each turn");
            }
            throw new IllegalStateException("This ability can be activated no more than " + maxActivationsPerTurn + " times each turn");
        }
    }

    /**
     * "Activate only once" (Goblin Ski Patrol): counted for the whole game against this permanent
     * object, so a permanent that leaves and re-enters the battlefield may activate again (CR 400.7).
     */
    private void validateActivationLimitPerGame(GameData gameData, UUID playerId, Permanent permanent,
                                                ActivatedAbility ability, int abilityIndex) {
        Integer maxActivationsPerGame = ability.getMaxActivationsPerGame();
        if (maxActivationsPerGame == null) {
            return;
        }

        if (ability.isExhaustAbility()
                && gameQueryService.canActivateExhaustAbilityAsThoughNotActivated(gameData, playerId)) {
            return;
        }

        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisGame.get(permanent.getId());
        int currentCount = perAbilityCounts != null ? perAbilityCounts.getOrDefault(abilityIndex, 0) : 0;
        if (currentCount >= maxActivationsPerGame) {
            throw new IllegalStateException(maxActivationsPerGame == 1
                    ? "This ability can be activated only once"
                    : "This ability can be activated no more than " + maxActivationsPerGame + " times");
        }
    }

    private void recordAbilityActivationUse(GameData gameData, Permanent permanent, int abilityIndex) {
        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn
                .computeIfAbsent(permanent.getId(), ignored -> new ConcurrentHashMap<>());
        perAbilityCounts.merge(abilityIndex, 1, Integer::sum);

        gameData.activatedAbilityUsesThisGame
                .computeIfAbsent(permanent.getId(), ignored -> new ConcurrentHashMap<>())
                .merge(abilityIndex, 1, Integer::sum);
    }

    private void validateNotBlockedByPithingNeedle(GameData gameData, Permanent permanent, ActivatedAbility ability) {
        validateNotBlockedByNameLock(gameData, permanent.getCard().getName(), isManaAbility(ability));
    }

    /**
     * Overwhelming Splendor: the enchanted player can activate only mana abilities and loyalty
     * abilities. {@code ability} is the activated ability being played, or {@code null} for
     * activations that are never mana or loyalty abilities (e.g. an ON_SACRIFICE ability).
     */
    private void validateEnchantedPlayerAbilityRestriction(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability != null && (isManaAbility(ability) || ability.getLoyaltyCost() != null)) {
            return;
        }
        if (gameQueryService.playerCantActivateNonManaOrLoyaltyAbilities(gameData, playerId)) {
            throw new IllegalStateException(
                    "You can only activate mana abilities and loyalty abilities (Overwhelming Splendor)");
        }
    }

    /**
     * Abeyance: a player locked out this turn can't activate abilities that aren't mana abilities
     * (loyalty abilities included). {@code ability} is the activated ability being played, or
     * {@code null} for activations that are never mana abilities (e.g. an ON_SACRIFICE ability).
     */
    private void validateNotBlockedByNonManaAbilityLock(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability != null && isManaAbility(ability)) return;
        if (gameData.stack.stream()
                .anyMatch(entry -> entry.hasKeyword(Keyword.SPLIT_SECOND))) {
            throw new IllegalStateException(
                    "You can't activate abilities that aren't mana abilities while a spell with split second is on the stack");
        }
        if (gameData.playersCantActivateNonManaAbilitiesThisTurn.contains(playerId)) {
            throw new IllegalStateException("You can't activate abilities that aren't mana abilities this turn");
        }
    }

    /**
     * Hand to Hand: during combat, no player can activate abilities that aren't mana abilities.
     * {@code ability} is the activated ability being played, or {@code null} for activations that
     * are never mana abilities (e.g. an ON_SACRIFICE ability).
     */
    private void validateNotBlockedByCombatActionLock(GameData gameData, ActivatedAbility ability) {
        if (ability != null && isManaAbility(ability)) return;
        if (gameQueryService.isCombatActionLockActive(gameData)) {
            throw new IllegalStateException(
                    "You can't activate abilities that aren't mana abilities during combat");
        }
    }

    /**
     * City of Solitude: players can cast spells and activate abilities only during their own turns
     * (mana abilities included; all zones).
     */
    private void validateNotBlockedByOwnTurnOnlyRestriction(GameData gameData, UUID playerId) {
        if (gameQueryService.isLockedOutByOwnTurnOnlyRestriction(gameData, playerId)) {
            throw new IllegalStateException(
                    "You can only cast spells and activate abilities during your own turn");
        }
    }

    /**
     * Grand Abolisher: during its controller's turn, their opponents can't activate abilities of
     * artifacts, creatures or enchantments. Mana abilities are included; abilities of lands and
     * other permanent types stay usable.
     */
    private void validateNotBlockedByOpponentsTurnRestriction(GameData gameData, UUID playerId, Permanent permanent) {
        if (!gameQueryService.isLockedOutByOpponentsTurnAbilityRestriction(gameData, playerId)) return;
        if (!gameQueryService.isCreature(gameData, permanent)
                && !gameQueryService.isArtifact(gameData, permanent)
                && !gameQueryService.isEnchantment(gameData, permanent)) return;
        throw new IllegalStateException("You can't activate abilities of artifacts, creatures, "
                + "or enchantments during your opponent's turn");
    }

    private void validateNotBlockedByStaticAbilityLock(GameData gameData, Permanent permanent,
                                                       boolean manaAbility) {
        if (gameQueryService.hasAuraWithEffect(gameData, permanent,
                effect -> effect instanceof EnchantedPermanentAbilityLockEffect lock
                        && (lock.blocksManaAbilities() || !manaAbility))) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName()
                    + " can't be activated (enchanted permanent lock)");
        }
        // Detain / Edifice of Authority: a floating lock forbids activating this permanent's
        // activated abilities (mana abilities included; triggered abilities are unaffected).
        if (gameQueryService.isLockedFromActivatingAbilities(gameData, permanent.getId())) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName()
                    + " can't be activated (detained)");
        }
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock
                            && (lock.blocksManaAbilities() || !manaAbility)) {
                        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, p.getId());
                        FilterContext filterContext = FilterContext.of(gameData)
                                .withSourceCardId(p.getCard().getId())
                                .withSourceControllerId(sourceControllerId)
                                .withSourcePermanentId(p.getId());
                        if (predicateEvaluationService.matchesPermanentPredicate(
                                permanent, lock.predicate(), filterContext)) {
                            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName()
                                    + " can't be activated (" + p.getCard().getName() + ")");
                        }
                    }
                }
            }
        }
    }

    private void validateNotBlockedByNameLock(GameData gameData, String cardName, boolean manaAbility) {
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                if (!java.util.Objects.equals(cardName, p.getChosenName())) continue;
                var lockEffect = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect)
                        .map(e -> (ActivatedAbilitiesOfChosenNameCantBeActivatedEffect) e)
                        .findFirst().orElse(null);
                if (lockEffect == null) continue;
                if (manaAbility && !lockEffect.blocksManaAbilities()) continue;
                throw new IllegalStateException("Activated abilities of " + cardName
                        + " can't be activated (" + p.getCard().getName() + ")");
            }
        }
    }

    /**
     * Returns the mana color that a land should produce if its type has been overridden
     * by an aura (e.g. Evil Presence making it a Swamp), or {@code null} if no override applies.
     */
    private boolean isDampingManaReplacementActiveOnTap(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent perm : bf) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof ReplaceLandExcessManaWithColorlessEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ManaColor getOverriddenLandManaColor(GameData gameData, Permanent permanent) {
        return gameQueryService.getOverriddenLandManaColor(gameData, permanent);
    }

    public boolean isManaAbilityAt(GameData gameData, UUID playerId, int permanentIndex, Integer abilityIndex) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null || permanentIndex < 0 || permanentIndex >= bf.size() || abilityIndex == null) return false;
        Permanent perm = bf.get(permanentIndex);
        ActivatedAbility ability = resolveAbility(gameData, perm, abilityIndex);
        return isManaAbility(ability);
    }

    /**
     * Returns true if an activated ability is a mana ability per CR 605.1a: no target, no spell
     * target, no loyalty cost, at least one mana-producing effect, and no cost or effect that moves
     * a card to or from a library.
     */
    public static boolean isManaAbility(ActivatedAbility ability) {
        return isManaAbility(ability, ability.getEffects());
    }

    /**
     * Classifies an ability using the supplied effects. This overload is used during activation,
     * where effects may be snapshotted or resolved from a granted ability.
     */
    public static boolean isManaAbility(ActivatedAbility ability, List<? extends CardEffect> abilityEffects) {
        if (ability.isNeedsTarget() || ability.isNeedsSpellTarget() || ability.getLoyaltyCost() != null
                || abilityEffects.stream().anyMatch(AbilityActivationService::movesCardToOrFromLibrary)) {
            return false;
        }
        List<? extends CardEffect> effects = abilityEffects.stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();
        return !effects.isEmpty() && effects.stream().anyMatch(e -> e instanceof ManaProducingEffect);
    }

    private static boolean movesCardToOrFromLibrary(CardEffect effect) {
        // Registering a delayed upkeep draw does not move a card during this ability's resolution.
        return (effect instanceof CardDrawingEffect
                && !(effect instanceof RegisterDrawCardsAtNextUpkeepEffect))
                || effect instanceof MillEffect
                || effect instanceof DrawCardsCost
                || effect instanceof ExileTopCardOfLibraryCost
                || effect instanceof MillControllerCost
                || effect instanceof ExileTopCardOfOwnLibraryEffect
                || effect instanceof SearchLibraryEffect
                || (effect instanceof HandCardCost handCardCost
                        && handCardCost.putsPaidCardsOnTopOfLibrary());
    }

    private boolean isCaveSource(GameData gameData, Permanent permanent) {
        return predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, new PermanentHasSubtypePredicate(CardSubtype.CAVE));
    }
}




