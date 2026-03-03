package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.ability.cost.ArtifactSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentChoiceCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentSacrificeAction;
import com.github.laxika.magicalvibes.service.ability.cost.SubtypeSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCreatureCostHandler;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetLegalityService targetLegalityService;
    private final ActivatedAbilityExecutionService activatedAbilityExecutionService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    /**
     * Taps a permanent for its mana ability (ON_TAP effects), adding the produced mana to the player's pool.
     *
     * @param gameData       the current game state
     * @param player         the player tapping the permanent
     * @param permanentIndex index of the permanent on the player's battlefield
     * @throws IllegalStateException if the permanent is already tapped, has no tap effects,
     *                               has summoning sickness (creatures without haste), or is blocked by Arrest
     */
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
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty()) {
            throw new IllegalStateException("Permanent has no tap effects");
        }
        if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
            throw new IllegalStateException("Creature has summoning sickness");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
            if (effect instanceof AwardManaEffect awardMana) {
                manaPool.add(awardMana.color(), awardMana.amount());
            }
        }

        String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Check for "whenever a player taps a land for mana" triggers (e.g. Manabarbs)
        if (permanent.getCard().getType() == CardType.LAND) {
            triggerCollectionService.checkLandTapTriggers(gameData, playerId, permanent.getId());
        }

        // Check for "whenever enchanted permanent becomes tapped" triggers (e.g. Relic Putrescence)
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);

        gameBroadcastService.broadcastGameState(gameData);
    }

    /**
     * Activates an ON_SACRIFICE ability by sacrificing the source permanent and placing the ability on the stack.
     *
     * @param gameData          the current game state
     * @param player            the player sacrificing the permanent
     * @param permanentIndex    index of the permanent on the player's battlefield
     * @param targetPermanentId target for the sacrifice effect (e.g. for destroy-target abilities), or {@code null}
     * @throws IllegalStateException if the permanent has no sacrifice abilities, is blocked by Pithing Needle
     *                               or Arrest, or the target is invalid/protected
     */
    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetPermanentId) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        if (permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE).isEmpty()) {
            throw new IllegalStateException("Permanent has no sacrifice abilities");
        }

        // Pithing Needle check: sacrifice abilities are activated abilities
        if (isPithingNeedleBlockingCard(gameData, permanent.getCard().getName())) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Pithing Needle)");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }

        // Validate target for effects that need one
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)) {
            if (effect instanceof DestroyTargetPermanentEffect destroy) {
                if (targetPermanentId == null) {
                    throw new IllegalStateException("Sacrifice ability requires a target");
                }
                Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
                if (target == null) {
                    throw new IllegalStateException("Invalid target permanent");
                }
                if (permanent.getCard().getTargetFilter() != null) {
                    gameQueryService.validateTargetFilter(permanent.getCard().getTargetFilter(), target);
                }
                if (gameQueryService.hasProtectionFrom(gameData, target, permanent.getEffectiveColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getEffectiveColor().name().toLowerCase());
                }
                if (gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, permanent)) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getType().getDisplayName().toLowerCase() + "s");
                }
            }
        }

        // Sacrifice: remove from battlefield, add to graveyard
        boolean wasCreature = gameQueryService.isCreature(gameData, permanent);
        battlefield.remove(permanentIndex);
        boolean wentToGraveyard = gameHelper.addCardToGraveyard(gameData, playerId, permanent.getOriginalCard(), Zone.BATTLEFIELD);
        if (wentToGraveyard) {
            gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            if (wasCreature) {
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
            }
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId);
        permanentRemovalService.removeOrphanedAuras(gameData);

        String logEntry = player.getUsername() + " sacrifices " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Put activated ability on stack
        gameData.stack.add(new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                permanent.getCard(),
                playerId,
                permanent.getCard().getName() + "'s ability",
                new ArrayList<>(permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)),
                0,
                targetPermanentId,
                Map.of()
        ));
        gameData.priorityPassedBy.clear();

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
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
     * @param targetPermanentId target permanent for the ability, or creature to sacrifice as cost, or {@code null}
     * @param targetZone        target zone for zone-targeted effects, or {@code null}
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone, null, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone, List<UUID> targetPermanentIds) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone, null, null, targetPermanentIds);
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
    public void handleActivatedAbilityDiscardCostChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (gameData.pendingAbilityActivation == null) {
            throw new IllegalStateException("No pending ability activation");
        }
        if (cardChoice.validIndices() == null || !cardChoice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        PendingAbilityActivation pending = gameData.pendingAbilityActivation;
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        int permanentIndex = gameData.playerBattlefields.get(player.getId()).indexOf(source);
        if (permanentIndex < 0) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer under your control");
        }

        clearPendingAbilityActivation(gameData);
        activateAbilityInternal(
                gameData,
                player,
                permanentIndex,
                pending.abilityIndex(),
                pending.xValue(),
                pending.targetPermanentId(),
                pending.targetZone(),
                cardIndex,
                null,
                null
        );
    }

    public void handleActivatedAbilityGraveyardExileCostChosen(GameData gameData, Player player, int cardIndex) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE)) {
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

        int permanentIndex = gameData.playerBattlefields.get(playerId).indexOf(source);
        if (permanentIndex < 0) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer under your control");
        }

        clearPendingAbilityActivation(gameData);
        activateAbilityInternal(
                gameData,
                player,
                permanentIndex,
                pending.abilityIndex(),
                pending.xValue(),
                pending.targetPermanentId(),
                pending.targetZone(),
                null,
                cardIndex,
                null
        );
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetPermanentId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetPermanentIds) {
        int effectiveXValue = xValue != null ? xValue : 0;

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        List<ActivatedAbility> abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
        abilities.addAll(gameQueryService.computeStaticBonus(gameData, permanent).grantedActivatedAbilities());
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Permanent has no activated ability");
        }

        int effectiveIndex = effectiveAbilityIndex(abilityIndex);
        ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
        List<CardEffect> abilityEffects = ability.getEffects();
        String abilityCost = ability.getManaCost();
        boolean isTapAbility = ability.isRequiresTap();

        // Pithing Needle check: block non-mana activated abilities of the chosen name
        validateNotBlockedByPithingNeedle(gameData, permanent, ability);

        // Arrest check: block all activated abilities of enchanted creature
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }

        // Validate activation timing restrictions (e.g. "Activate only during your upkeep")
        validateTimingRestrictions(gameData, playerId, permanent, ability);
        validateActivationLimitPerTurn(gameData, permanent, ability, effectiveIndex);

        // Validate loyalty ability restrictions
        if (ability.getLoyaltyCost() != null) {
            validateAndPayLoyaltyCost(gameData, playerId, permanent, ability);
        }

        // Validate tap requirement
        if (isTapAbility) {
            if (permanent.isTapped()) {
                throw new IllegalStateException("Permanent is already tapped");
            }
            if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        // Validate spell target for abilities that counter spells
        if (ability.isNeedsSpellTarget()) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetPermanentId, ability.getTargetFilter(), playerId);
        }

        boolean hasSacCreatureCost = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeCreatureCost);
        List<PermanentChoiceCostHandler> permanentChoiceCosts = abilityEffects.stream()
                .map(this::toPermanentChoiceCostHandler)
                .filter(Objects::nonNull)
                .toList();

        // For regular targeting abilities, validate legality before costs are paid (CR 602.2b/601.2c).
        if (ability.isMultiTarget()) {
            targetLegalityService.validateMultiTargetAbility(gameData, playerId, ability, targetPermanentIds, permanent.getCard());
        } else if (!hasSacCreatureCost) {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetPermanentId, targetZone, permanent.getCard(), effectiveXValue);
        }
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }

        ExileCardFromGraveyardCost exileGraveyardCost = abilityEffects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileGraveyardCost != null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, exileGraveyardCost.requiredType());
            if (validExileIndices.isEmpty()) {
                String typeName = exileGraveyardCost.requiredType() != null
                        ? exileGraveyardCost.requiredType().name().toLowerCase() + " " : "";
                throw new IllegalStateException("No " + typeName + "card in graveyard to exile");
            }
            if (exileGraveyardCardIndex == null) {
                beginGraveyardExileCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetPermanentId, targetZone,
                        exileGraveyardCost.requiredType(), validExileIndices);
                return;
            }
        }

        DiscardCardTypeCost discardCardTypeCost = abilityEffects.stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Integer> validDiscardIndices = collectDiscardIndicesForType(hand, discardCardTypeCost.requiredType());
            if (validDiscardIndices.isEmpty()) {
                throw new IllegalStateException("Must discard a " + discardCardTypeCost.requiredType().name().toLowerCase() + " card to activate ability");
            }
            if (discardCardIndex == null) {
                beginDiscardCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetPermanentId, targetZone,
                        discardCardTypeCost.requiredType(), validDiscardIndices);
                return;
            }
        }

        // Validate and pay remove-counter cost
        boolean hasRemoveCounterCost = abilityEffects.stream().anyMatch(e -> e instanceof RemoveCounterFromSourceCost);
        if (hasRemoveCounterCost) {
            int totalCounters = permanent.getPlusOnePlusOneCounters() + permanent.getMinusOneMinusOneCounters();
            if (totalCounters <= 0) {
                throw new IllegalStateException("No counters to remove");
            }
        }

        // Validate and pay remove-charge-counter cost
        Optional<RemoveChargeCountersFromSourceCost> removeChargeCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveChargeCountersFromSourceCost)
                .map(e -> (RemoveChargeCountersFromSourceCost) e)
                .findFirst();
        if (removeChargeCost.isPresent()) {
            int required = removeChargeCost.get().count();
            if (permanent.getChargeCounters() < required) {
                throw new IllegalStateException("Not enough charge counters (need " + required + ", have " + permanent.getChargeCounters() + ")");
            }
        }

        // Validate X value for Prototype Portal-style abilities:
        // Per ruling: "You may not activate the second ability if no card has been exiled with Prototype Portal."
        // X is defined by the exiled card's mana value (not chosen freely), so no imprint = can't activate.
        CreateTokenCopyOfImprintedCardEffect imprintedCopyEffect = abilityEffects.stream()
                .filter(CreateTokenCopyOfImprintedCardEffect.class::isInstance)
                .map(CreateTokenCopyOfImprintedCardEffect.class::cast)
                .findFirst().orElse(null);
        if (imprintedCopyEffect != null && !imprintedCopyEffect.exileAtEndStep()) {
            Card imprintedCard = permanent.getCard().getImprintedCard();
            if (imprintedCard == null) {
                throw new IllegalStateException("No card has been exiled with " + permanent.getCard().getName());
            }
            int requiredX = imprintedCard.getManaValue();
            if (effectiveXValue != requiredX) {
                throw new IllegalStateException("X must equal the mana value of the imprinted card (" + requiredX + ")");
            }
        }

        // Pay mana cost
        if (abilityCost != null) {
            boolean artifactContext = gameQueryService.isArtifact(permanent);
            boolean myrContext = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext);
        }

        if (discardCardTypeCost != null) {
            payDiscardCost(gameData, player, discardCardTypeCost.requiredType(), discardCardIndex);
        }

        if (exileGraveyardCost != null) {
            payGraveyardExileCost(gameData, player, exileGraveyardCost.requiredType(), exileGraveyardCardIndex);
        }

        // Pay remove-counter cost: remove one counter (prefer -1/-1, then +1/+1)
        if (hasRemoveCounterCost) {
            if (permanent.getMinusOneMinusOneCounters() > 0) {
                permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() - 1);
                String counterLog = player.getUsername() + " removes a -1/-1 counter from " + permanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, counterLog);
            } else if (permanent.getPlusOnePlusOneCounters() > 0) {
                permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - 1);
                String counterLog = player.getUsername() + " removes a +1/+1 counter from " + permanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, counterLog);
            }
        }

        // Pay remove-charge-counter cost
        if (removeChargeCost.isPresent()) {
            int required = removeChargeCost.get().count();
            permanent.setChargeCounters(permanent.getChargeCounters() - required);
            String counterLog = player.getUsername() + " removes " + required + " charge counter(s) from " + permanent.getCard().getName()
                    + " (" + permanent.getChargeCounters() + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, counterLog);
        }
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            if (handlePermanentChoiceCost(gameData, player, permanent, effectiveIndex,
                    effectiveXValue, targetPermanentId, targetZone, handler)) {
                return;
            }
        }

        // Handle sacrifice-a-creature cost (e.g. Nantuko Husk: "Sacrifice a creature: ...")
        if (hasSacCreatureCost) {
            if (targetPermanentId == null) {
                throw new IllegalStateException("Must choose a creature to sacrifice");
            }
            Permanent sacTarget = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (sacTarget == null) {
                throw new IllegalStateException("Invalid sacrifice target");
            }
            if (!gameQueryService.isCreature(gameData, sacTarget)) {
                throw new IllegalStateException("Must sacrifice a creature");
            }
            List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
            if (playerBf == null || !playerBf.contains(sacTarget)) {
                throw new IllegalStateException("Must sacrifice a creature you control");
            }

            sacrificePermanentAsCost(gameData, player, sacTarget);

            // Clear targetPermanentId so it's not used for effect targeting
            targetPermanentId = null;
        }

        // Sacrifice-a-creature cost abilities use target selection as UI for cost payment.
        // Validate any remaining real targets after the cost has been paid.
        if (hasSacCreatureCost) {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetPermanentId, targetZone, permanent.getCard(), effectiveXValue);
        }
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects,
                effectiveXValue, targetPermanentId, targetZone, hasSacCreatureCost, effectiveIndex, targetPermanentIds);
    }

    PermanentChoiceCostHandler toPermanentChoiceCostHandler(CardEffect effect) {
        PermanentSacrificeAction sacAction = this::sacrificePermanentAsCost;
        if (effect instanceof SacrificeSubtypeCreatureCost c) return new SubtypeSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof SacrificeArtifactCost c) return new ArtifactSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof SacrificeMultiplePermanentsCost c) return new MultiplePermanentSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof TapCreatureCost c) return new TapCreatureCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService);
        if (effect instanceof TapMultiplePermanentsCost c) return new MultiplePermanentTapCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService);
        return null;
    }

    private boolean handlePermanentChoiceCost(GameData gameData, Player player, Permanent source,
                                               int abilityIndex, int xValue, UUID targetPermanentId, Zone targetZone,
                                               PermanentChoiceCostHandler handler) {
        List<UUID> validIds = handler.getValidChoiceIds(gameData, player.getId());
        int required = handler.requiredCount();
        if (validIds.size() <= required) {
            for (UUID id : validIds) {
                Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                if (chosen != null) {
                    handler.validateAndPay(gameData, player, chosen);
                }
            }
            return false;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                player.getId(), source.getId(), abilityIndex, xValue, targetPermanentId, targetZone,
                handler.costEffect(), required));
        playerInputService.beginPermanentChoice(gameData, player.getId(), validIds,
                handler.getPromptMessage(required));
        gameBroadcastService.broadcastGameState(gameData);
        return true;
    }

    /**
     * Callback for when a player has chosen a permanent for an activated ability's permanent-choice cost
     * (sacrifice subtype, sacrifice artifact, sacrifice multiple, or tap creature). Validates the choice,
     * pays the cost, and either re-prompts for additional choices or completes the ability activation.
     */
    public void completeActivatedAbilityCostChoice(GameData gameData, Player player,
                                                    PermanentChoiceContext.ActivatedAbilityCostChoice context,
                                                    UUID chosenPermanentId) {
        UUID playerId = player.getId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }

        int effectiveIndex = effectiveAbilityIndex(context.abilityIndex());
        ActivatedAbility ability = resolveAbility(gameData, sourcePermanent, context.abilityIndex());
        List<CardEffect> abilityEffects = ability.getEffects();
        if (!abilityEffects.contains(context.costEffect())) {
            throw new IllegalStateException("Activated ability no longer has the required cost");
        }

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(context.costEffect());
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

        int remaining = context.remaining() - 1;
        if (remaining > 0) {
            List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
            if (validIds.size() < remaining) {
                throw new IllegalStateException("Not enough permanents remaining");
            }
            if (validIds.size() == remaining) {
                for (UUID id : validIds) {
                    Permanent autoPay = gameQueryService.findPermanentById(gameData, id);
                    if (autoPay != null) {
                        handler.validateAndPay(gameData, player, autoPay);
                    }
                }
            } else {
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                        playerId, context.sourcePermanentId(), context.abilityIndex(), context.xValue(),
                        context.targetPermanentId(), context.targetZone(), context.costEffect(), remaining));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        }

        completeActivationAndRecord(gameData, player, sourcePermanent, ability, abilityEffects,
                context.xValue() != null ? context.xValue() : 0, context.targetPermanentId(), context.targetZone(), false, effectiveIndex);
    }

    private ActivatedAbility resolveAbility(GameData gameData, Permanent permanent, Integer abilityIndex) {
        List<ActivatedAbility> abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
        abilities.addAll(gameQueryService.computeStaticBonus(gameData, permanent).grantedActivatedAbilities());
        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        return abilities.get(idx);
    }

    private int effectiveAbilityIndex(Integer abilityIndex) {
        return abilityIndex != null ? abilityIndex : 0;
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetPermanentId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex) {
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects, xValue, targetPermanentId, targetZone, nonTargeting, abilityIndex, null);
    }

    private void completeActivationAndRecord(GameData gameData, Player player, Permanent permanent,
                                              ActivatedAbility ability, List<CardEffect> abilityEffects,
                                              int xValue, UUID targetPermanentId, Zone targetZone,
                                              boolean nonTargeting, int abilityIndex, List<UUID> targetPermanentIds) {
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, xValue, targetPermanentId, targetZone, nonTargeting, targetPermanentIds);
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
    }

    private void sacrificePermanentAsCost(GameData gameData, Player player, Permanent sacTarget) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(sacTarget)) {
            throw new IllegalStateException("Must sacrifice a permanent you control");
        }
        playerBf.remove(sacTarget);
        boolean wentToGraveyard = gameHelper.addCardToGraveyard(gameData, playerId, sacTarget.getCard(), Zone.BATTLEFIELD);
        if (wentToGraveyard && gameQueryService.isCreature(gameData, sacTarget)) {
            gameHelper.collectDeathTrigger(gameData, sacTarget.getCard(), playerId, true);
            gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId);
        String sacLog = player.getUsername() + " sacrifices " + sacTarget.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, sacLog);
    }

    private void validateTimingRestrictions(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability) {
        if (ability.getTimingRestriction() != null) {
            if (ability.getTimingRestriction() == ActivationTimingRestriction.METALCRAFT) {
                if (!gameQueryService.isMetalcraftMet(gameData, playerId)) {
                    throw new IllegalStateException("Metalcraft — activate only if you control three or more artifacts");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_CREATURE) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    throw new IllegalStateException("This ability can only be activated while this permanent is a creature");
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
            if (ability.getTimingRestriction() == ActivationTimingRestriction.POWER_4_OR_GREATER) {
                int effectivePower = gameQueryService.getEffectivePower(gameData, permanent);
                if (effectivePower < 4) {
                    throw new IllegalStateException("Activate only if this creature's power is 4 or greater");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated at sorcery speed");
                }
                if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                    throw new IllegalStateException("This ability can only be activated during a main phase");
                }
                if (!gameData.stack.isEmpty()) {
                    throw new IllegalStateException("This ability can only be activated when the stack is empty");
                }
            }
        }
    }

    private void validateAndPayLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability) {
        // Sorcery-speed timing: must be active player, main phase, stack empty
        if (!playerId.equals(gameData.activePlayerId)) {
            throw new IllegalStateException("Loyalty abilities can only be activated on your turn");
        }
        if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
            throw new IllegalStateException("Loyalty abilities can only be activated during a main phase");
        }
        if (!gameData.stack.isEmpty()) {
            throw new IllegalStateException("Loyalty abilities can only be activated when the stack is empty");
        }
        // Once per turn
        if (permanent.isLoyaltyAbilityUsedThisTurn()) {
            throw new IllegalStateException("Only one loyalty ability per planeswalker per turn");
        }
        // For negative loyalty costs, check sufficient loyalty
        int loyaltyCost = ability.getLoyaltyCost();
        if (loyaltyCost < 0 && permanent.getLoyaltyCounters() < Math.abs(loyaltyCost)) {
            throw new IllegalStateException("Not enough loyalty counters");
        }
        // Pay loyalty cost
        permanent.setLoyaltyCounters(permanent.getLoyaltyCounters() + loyaltyCost);
        permanent.setLoyaltyAbilityUsedThisTurn(true);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext) {
        ManaCost cost = new ManaCost(abilityCost);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        boolean hasRestricted = artifactContext || myrContext;
        if (cost.hasX()) {
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (hasRestricted) {
                if (!cost.canPay(pool, effectiveXValue, artifactContext, myrContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue, artifactContext, myrContext);
            } else {
                if (!cost.canPay(pool, effectiveXValue)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue);
            }
        } else {
            if (hasRestricted) {
                if (!cost.canPay(pool, 0, artifactContext, myrContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, 0, artifactContext, myrContext);
            } else {
                if (!cost.canPay(pool)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool);
            }
        }
    }

    private List<Integer> collectDiscardIndicesForType(List<Card> hand, CardType requiredType) {
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getType() == requiredType) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void beginDiscardCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                        UUID targetPermanentId, Zone targetZone, CardType requiredType, List<Integer> validDiscardIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetPermanentId,
                targetZone,
                requiredType
        );
        gameData.interaction.beginCardChoice(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE, playerId, new HashSet<>(validDiscardIndices), null);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                validDiscardIndices,
                "Choose a " + requiredType.name().toLowerCase() + " card to discard as an activation cost."
        ));
    }

    private void payDiscardCost(GameData gameData, Player player, CardType requiredType, Integer discardCardIndex) {
        if (discardCardIndex == null) {
            throw new IllegalStateException("Must choose a card to discard");
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validDiscardIndices = collectDiscardIndicesForType(hand, requiredType);
        Set<Integer> validSet = new HashSet<>(validDiscardIndices);
        if (!validSet.contains(discardCardIndex)) {
            throw new IllegalStateException("Must discard a " + requiredType.name().toLowerCase() + " card");
        }

        Card discarded = hand.remove((int) discardCardIndex);
        gameHelper.addCardToGraveyard(gameData, player.getId(), discarded);
        gameData.discardCausedByOpponent = false;
        triggerCollectionService.checkDiscardTriggers(gameData, player.getId(), discarded);

        String logEntry = player.getUsername() + " discards " + discarded.getName() + " as an activation cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {} as activation cost", gameData.id, player.getUsername(), discarded.getName());
    }

    private List<Integer> collectGraveyardIndicesForType(List<Card> graveyard, CardType requiredType) {
        List<Integer> validIndices = new ArrayList<>();
        if (graveyard == null) {
            return validIndices;
        }
        for (int i = 0; i < graveyard.size(); i++) {
            if (requiredType == null || graveyard.get(i).getType() == requiredType) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void beginGraveyardExileCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                               UUID targetPermanentId, Zone targetZone, CardType requiredType, List<Integer> validExileIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetPermanentId,
                targetZone,
                null
        );
        gameData.interaction.beginGraveyardChoice(playerId, new HashSet<>(validExileIndices), null, null);
        gameData.interaction.setAwaitingInput(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE);
        String typeName = requiredType != null ? requiredType.name().toLowerCase() + " " : "";
        sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(
                validExileIndices,
                "Choose a " + typeName + "card from your graveyard to exile as an activation cost.",
                false
        ));
    }

    private void payGraveyardExileCost(GameData gameData, Player player, CardType requiredType, Integer exileCardIndex) {
        if (exileCardIndex == null) {
            throw new IllegalStateException("Must choose a card to exile from graveyard");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, requiredType);
        Set<Integer> validSet = new HashSet<>(validExileIndices);
        if (!validSet.contains(exileCardIndex)) {
            String typeName = requiredType != null ? requiredType.name().toLowerCase() + " " : "";
            throw new IllegalStateException("Must exile a " + typeName + "card from your graveyard");
        }

        Card exiled = graveyard.remove((int) exileCardIndex);
        gameData.playerExiledCards.computeIfAbsent(playerId, k -> new ArrayList<>()).add(exiled);

        String logEntry = player.getUsername() + " exiles " + exiled.getName() + " from graveyard as an activation cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} from graveyard as activation cost", gameData.id, player.getUsername(), exiled.getName());
    }

    private void clearPendingAbilityActivation(GameData gameData) {
        gameData.pendingAbilityActivation = null;
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearCardChoice();
        gameData.interaction.clearGraveyardChoice();
    }

    private void validateActivationLimitPerTurn(GameData gameData, Permanent permanent, ActivatedAbility ability, int abilityIndex) {
        Integer maxActivationsPerTurn = ability.getMaxActivationsPerTurn();
        if (maxActivationsPerTurn == null) {
            return;
        }

        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(permanent.getId());
        int currentCount = perAbilityCounts != null ? perAbilityCounts.getOrDefault(abilityIndex, 0) : 0;
        if (currentCount >= maxActivationsPerTurn) {
            throw new IllegalStateException("This ability can be activated no more than " + maxActivationsPerTurn + " times each turn");
        }
    }

    private void recordAbilityActivationUse(GameData gameData, Permanent permanent, int abilityIndex) {
        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn
                .computeIfAbsent(permanent.getId(), ignored -> new ConcurrentHashMap<>());
        perAbilityCounts.merge(abilityIndex, 1, Integer::sum);
    }

    private void validateNotBlockedByPithingNeedle(GameData gameData, Permanent permanent, ActivatedAbility ability) {
        if (isManaAbility(ability)) {
            return;
        }
        if (isPithingNeedleBlockingCard(gameData, permanent.getCard().getName())) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Pithing Needle)");
        }
    }

    private boolean isPithingNeedleBlockingCard(GameData gameData, String cardName) {
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                boolean hasNeedleEffect = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect);
                if (hasNeedleEffect && cardName.equals(p.getChosenName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isManaAbility(ActivatedAbility ability) {
        if (ability.isNeedsTarget() || ability.isNeedsSpellTarget() || ability.getLoyaltyCost() != null) {
            return false;
        }
        List<CardEffect> effects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();
        return !effects.isEmpty() && effects.stream().allMatch(e -> e instanceof ManaProducingEffect);
    }
}




