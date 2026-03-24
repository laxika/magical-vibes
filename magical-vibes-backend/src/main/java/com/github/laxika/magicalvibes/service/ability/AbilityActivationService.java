package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.ability.cost.ArtifactSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CreatureSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentReturnToHandCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentBounceAction;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentChoiceCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentSacrificeAction;
import com.github.laxika.magicalvibes.service.ability.cost.SubtypeSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCreatureCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapXPermanentsCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CrewCostHandler;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapXPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
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

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetLegalityService targetLegalityService;
    private final ActivatedAbilityExecutionService activatedAbilityExecutionService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileService exileService;

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
        // Check for land type override (e.g. Evil Presence making a land into a Swamp)
        ManaColor overriddenManaColor = getOverriddenLandManaColor(gameData, permanent);
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty() && overriddenManaColor == null) {
            throw new IllegalStateException("Permanent has no tap effects");
        }
        if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
            throw new IllegalStateException("Creature has summoning sickness");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent);

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);
        if (overriddenManaColor != null) {
            // Land type is overridden — produce the new basic land type's mana instead of original
            manaPool.add(overriddenManaColor, 1);
        } else {
            // Damping Sphere replacement: if a land is tapped for two or more mana, it produces {C} instead.
            boolean dampingReplacement = false;
            if (permanent.getCard().hasType(CardType.LAND) && isDampingManaReplacementActiveOnTap(gameData)) {
                int totalMana = 0;
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        totalMana += awardMana.amount();
                    }
                }
                if (totalMana >= 2) {
                    dampingReplacement = true;
                    manaPool.add(ManaColor.COLORLESS, 1);
                }
            }
            if (!dampingReplacement) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        manaPool.add(awardMana.color(), awardMana.amount());
                        if (isCreatureSource) {
                            manaPool.addCreatureMana(awardMana.color(), awardMana.amount());
                        }
                    }
                }
            }
        }

        String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Check for "whenever a player taps a land for mana" triggers (e.g. Manabarbs)
        if (permanent.getCard().hasType(CardType.LAND)) {
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
        validateNotBlockedByStaticAbilityLock(gameData, permanent);

        // Validate target for effects that need one
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)) {
            if (effect instanceof DestroyTargetPermanentEffect destroy) {
                if (targetId == null) {
                    throw new IllegalStateException("Sacrifice ability requires a target");
                }
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
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
                if (gameQueryService.hasProtectionFromSourceSubtypes(gameData, target, permanent)) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from source's subtype");
                }
            }
        }

        // Sacrifice: remove from battlefield, add to graveyard
        permanentRemovalService.removePermanentToGraveyard(gameData, permanent);
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, permanent.getCard());
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
                targetId,
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
     * @param targetId target permanent for the ability, or creature to sacrifice as cost, or {@code null}
     * @param targetZone        target zone for zone-targeted effects, or {@code null}
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, damageAssignments);
    }

    /**
     * Activates an activated ability on a card in the player's graveyard (e.g. Magma Phoenix's
     * "{3}{R}{R}: Return Magma Phoenix from your graveyard to your hand.").
     *
     * <p>Validates the card exists in the graveyard, has a graveyard activated ability, and that
     * the player can pay the mana cost. Pays the cost and pushes the ability onto the stack.</p>
     */
    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex) {
        // Ashes of the Abhorrent etc.: players can't activate abilities of cards in graveyards
        if (!gameQueryService.canPlayersActivateGraveyardAbilities(gameData)) {
            throw new IllegalStateException("Abilities of cards in graveyards can't be activated");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyardCardIndex < 0 || graveyardCardIndex >= graveyard.size()) {
            throw new IllegalStateException("Invalid graveyard card index");
        }

        Card card = graveyard.get(graveyardCardIndex);
        List<ActivatedAbility> abilities = card.getGraveyardActivatedAbilities();
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no graveyard activated ability");
        }

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);

        // Validate timing restrictions applicable to graveyard abilities (e.g. Raid)
        validateGraveyardTimingRestrictions(gameData, playerId, ability);

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

        // Identify permanent-choice costs (e.g. return lands to hand)
        List<PermanentChoiceCostHandler> permanentChoiceCosts = ability.getEffects().stream()
                .map(e -> toPermanentChoiceCostHandler(e, null, 0))
                .filter(Objects::nonNull)
                .toList();

        // Validate permanent-choice costs can be paid before paying mana
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }

        // Pay mana cost
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            payManaCost(gameData, playerId, abilityCost, 0, false, false);
        }

        // Pay permanent-choice costs (auto-pay or enter interactive mode)
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            if (handleGraveyardPermanentChoiceCost(gameData, player, card, graveyardCardIndex, idx, handler)) {
                return; // Entering interactive choice mode; activation will complete later
            }
        }

        completeGraveyardAbilityActivation(gameData, player, card, ability);
    }

    private boolean handleGraveyardPermanentChoiceCost(GameData gameData, Player player, Card card,
                                                        int graveyardCardIndex, int abilityIndex,
                                                        PermanentChoiceCostHandler handler) {
        int required = handler.requiredCount();
        if (required <= 0) return false;
        UUID playerId = player.getId();
        List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
        if (validIds.size() <= required) {
            // Auto-pay: exactly enough permanents
            for (UUID id : validIds) {
                Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                if (chosen != null) {
                    handler.validateAndPay(gameData, player, chosen);
                }
            }
            return false;
        }
        // Interactive choice: more valid permanents than required
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.GraveyardAbilityCostChoice(
                playerId, card, graveyardCardIndex, abilityIndex, handler.costEffect(), required));
        playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                handler.getPromptMessage(required));
        gameBroadcastService.broadcastGameState(gameData);
        return true;
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
        ActivatedAbility ability = card.getGraveyardActivatedAbilities().get(idx);

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(context.costEffect(), null, 0);
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
                // Re-prompt for next choice
                List<UUID> validIds = handler.getValidChoiceIds(gameData, playerId);
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.GraveyardAbilityCostChoice(
                        playerId, card, context.graveyardCardIndex(), context.abilityIndex(),
                        context.costEffect(), remaining));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        }

        completeGraveyardAbilityActivation(gameData, player, card, ability);
    }

    private void completeGraveyardAbilityActivation(GameData gameData, Player player, Card card, ActivatedAbility ability) {
        UUID playerId = player.getId();

        // Filter out cost effects for the snapshot
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (!(effect instanceof CostEffect)) {
                snapshotEffects.add(effect);
            }
        }

        // Push ability onto the stack
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects
        );
        gameData.stack.add(stackEntry);

        String logEntry = player.getUsername() + " activates " + card.getName() + "'s ability from the graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} activates {}'s graveyard ability", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
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
            // Invalid index — re-prompt the discard cost choice
            log.warn("Game {} - {} sent invalid discard cost card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            PendingAbilityActivation pending = gameData.pendingAbilityActivation;
            String costLabel = pending.discardCostLabel();
            String labelText = costLabel != null ? costLabel + " " : "";
            sessionManager.sendToPlayer(player.getId(), new ChooseCardFromHandMessage(
                    new ArrayList<>(cardChoice.validIndices()),
                    "Choose a " + labelText + "card to discard as an activation cost."
            ));
            return;
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
                pending.targetId(),
                pending.targetZone(),
                cardIndex,
                null,
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
                pending.targetId(),
                pending.targetZone(),
                null,
                cardIndex,
                null,
                null
        );
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        int effectiveXValue = xValue != null ? xValue : 0;

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities;
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            // Creature has lost all its own abilities; only static-granted abilities remain
            abilities = new ArrayList<>(staticBonus.grantedActivatedAbilities());
        } else {
            abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
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
        validateNotBlockedByStaticAbilityLock(gameData, permanent);

        // Validate activation timing restrictions (e.g. "Activate only during your upkeep")
        validateTimingRestrictions(gameData, playerId, permanent, ability);
        validateActivationLimitPerTurn(gameData, permanent, ability, effectiveIndex);

        // Validate loyalty ability restrictions
        if (ability.getLoyaltyCost() != null) {
            validateAndPayLoyaltyCost(gameData, playerId, permanent, ability, effectiveXValue);
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
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, ability.getTargetFilter(), playerId);
        }

        UUID sourceId = permanent.getId();
        final int xValueForCost = effectiveXValue;
        List<PermanentChoiceCostHandler> permanentChoiceCosts = abilityEffects.stream()
                .map(e -> toPermanentChoiceCostHandler(e, sourceId, xValueForCost))
                .filter(Objects::nonNull)
                .toList();

        // For regular targeting abilities, validate legality before costs are paid (CR 602.2b/601.2c).
        if (ability.isMultiTarget()) {
            targetLegalityService.validateMultiTargetAbility(gameData, playerId, ability, targetIds, permanent.getCard());
        } else if (targetZone == Zone.GRAVEYARD && targetIds != null && !targetIds.isEmpty()) {
            targetLegalityService.validateMultiTargetGraveyardAbility(gameData, playerId, abilityEffects, targetIds);
        } else {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetId, targetZone, permanent.getCard(), effectiveXValue);
        }
        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            handler.validateCanPay(gameData, playerId);
        }

        // Validate pay-life cost
        Optional<PayLifeCost> payLifeCost = abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst();
        if (payLifeCost.isPresent()) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            if (life < payLifeCost.get().amount()) {
                throw new IllegalStateException("Not enough life to pay (need " + payLifeCost.get().amount() + ", have " + life + ")");
            }
        }

        // Compute targeting tax from effects like Kopala, Warden of Waves
        int targetingTax = gameBroadcastService.getTargetingSubtypeTax(gameData, playerId, targetId, targetIds);

        // Pre-validate mana cost before entering interactive cost choices (CR 602.2b)
        if (abilityCost != null) {
            ManaCost preCheck = new ManaCost(abilityCost);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            boolean artifactCtx = gameQueryService.isArtifact(permanent);
            boolean myrCtx = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            if (preCheck.hasX()) {
                if (!preCheck.canPay(pool, effectiveXValue + targetingTax, artifactCtx, myrCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            } else {
                if (!preCheck.canPay(pool, targetingTax, artifactCtx, myrCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            }
        } else if (targetingTax > 0) {
            // No base mana cost but targeting tax applies — validate player can pay the tax
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getTotal() < targetingTax) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
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
                beginGraveyardExileCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                        exileGraveyardCost.requiredType(), validExileIndices);
                return;
            }
            // Handle "exile and pay its mana cost" abilities (e.g. Back from the Brink)
            if (exileGraveyardCost.payExiledCardManaCost()) {
                abilityCost = graveyard.get(exileGraveyardCardIndex).getManaCost();
            }
            if (exileGraveyardCost.imprintOnSource()) {
                permanent.getCard().setImprintedCard(graveyard.get(exileGraveyardCardIndex));
            }
        }

        DiscardCardTypeCost discardCardTypeCost = abilityEffects.stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Integer> validDiscardIndices = collectDiscardIndices(hand, discardCardTypeCost);
            if (validDiscardIndices.isEmpty()) {
                String costLabel = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
                throw new IllegalStateException("Must discard a " + costLabel + "card to activate ability");
            }
            if (discardCardIndex == null) {
                beginDiscardCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                        discardCardTypeCost.label(), validDiscardIndices);
                return;
            }
        }

        // Validate and pay remove-counter cost
        Optional<RemoveCounterFromSourceCost> removeCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveCounterFromSourceCost)
                .map(e -> (RemoveCounterFromSourceCost) e)
                .findFirst();
        if (removeCounterCost.isPresent()) {
            int required = removeCounterCost.get().count();
            CounterType ct = removeCounterCost.get().counterType();
            int available = switch (ct) {
                case AIM -> permanent.getAimCounters();
                case CHARGE -> permanent.getChargeCounters();
                case HATCHLING -> permanent.getHatchlingCounters();
                case MINUS_ONE_MINUS_ONE -> permanent.getMinusOneMinusOneCounters();
                case PLUS_ONE_PLUS_ONE -> permanent.getPlusOnePlusOneCounters();
                case SLIME -> permanent.getSlimeCounters();
                case STUDY -> permanent.getStudyCounters();
                case WISH -> permanent.getWishCounters();
                case LORE -> permanent.getLoreCounters();
                case LOYALTY -> permanent.getLoyaltyCounters();
                case SILVER -> 0; // Silver counters are on exiled cards, not permanents
                case ANY -> permanent.getPlusOnePlusOneCounters() + permanent.getMinusOneMinusOneCounters();
            };
            if (available < required) {
                throw new IllegalStateException("Not enough counters to remove (need " + required + ", have " + available + ")");
            }
        }

        // Validate mill-controller cost (e.g. Deranged Assistant: "{T}, Mill a card: Add {C}.")
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

        // Pay mana cost (including targeting tax if applicable)
        if (abilityCost != null) {
            boolean artifactContext = gameQueryService.isArtifact(permanent);
            boolean myrContext = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext, targetingTax);
        } else if (targetingTax > 0) {
            // No base mana cost but targeting tax applies — pay generic mana for the tax
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost taxCost = new ManaCost("{" + targetingTax + "}");
            taxCost.pay(pool);
        }

        // Pay life cost
        if (payLifeCost.isPresent()) {
            int amount = payLifeCost.get().amount();
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            gameData.playerLifeTotals.put(playerId, currentLife - amount);
        }

        if (discardCardTypeCost != null) {
            payDiscardCost(gameData, player, discardCardTypeCost, discardCardIndex);
        }

        if (exileGraveyardCost != null) {
            payGraveyardExileCost(gameData, player, exileGraveyardCost.requiredType(), exileGraveyardCardIndex);
        }

        // Pay remove-counter cost: remove counters respecting counter type
        if (removeCounterCost.isPresent()) {
            int count = removeCounterCost.get().count();
            CounterType ct = removeCounterCost.get().counterType();
            int removedMinus = 0;
            int removedPlus = 0;
            switch (ct) {
                case CHARGE -> {
                    permanent.setChargeCounters(permanent.getChargeCounters() - count);
                }
                case HATCHLING -> {
                    permanent.setHatchlingCounters(permanent.getHatchlingCounters() - count);
                }
                case MINUS_ONE_MINUS_ONE -> {
                    removedMinus = count;
                    permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() - count);
                }
                case PLUS_ONE_PLUS_ONE -> {
                    removedPlus = count;
                    permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - count);
                }
                case SLIME -> {
                    permanent.setSlimeCounters(permanent.getSlimeCounters() - count);
                }
                case STUDY -> {
                    permanent.setStudyCounters(permanent.getStudyCounters() - count);
                }
                case WISH -> {
                    permanent.setWishCounters(permanent.getWishCounters() - count);
                }
                case ANY -> {
                    removedMinus = Math.min(count, permanent.getMinusOneMinusOneCounters());
                    permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() - removedMinus);
                    int remaining = count - removedMinus;
                    if (remaining > 0) {
                        removedPlus = remaining;
                        permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - remaining);
                    }
                }
            }
            String counterTypeLabel;
            if (ct == CounterType.CHARGE) {
                counterTypeLabel = "charge";
            } else if (ct == CounterType.SLIME) {
                counterTypeLabel = "slime";
            } else if (ct == CounterType.STUDY) {
                counterTypeLabel = "study";
            } else if (ct == CounterType.WISH) {
                counterTypeLabel = "wish";
            } else if (removedMinus > 0 && removedPlus == 0) {
                counterTypeLabel = "-1/-1";
            } else if (removedPlus > 0 && removedMinus == 0) {
                counterTypeLabel = "+1/+1";
            } else {
                counterTypeLabel = "";
            }
            String counterWord = count == 1 ? "a " + counterTypeLabel + " counter" : count + " " + counterTypeLabel + " counters";
            String counterLog = player.getUsername() + " removes " + counterWord + " from " + permanent.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, counterLog);
        }

        // Pay remove-charge-counter cost
        if (removeChargeCost.isPresent()) {
            int required = removeChargeCost.get().count();
            permanent.setChargeCounters(permanent.getChargeCounters() - required);
            String counterLog = player.getUsername() + " removes " + required + " charge counter(s) from " + permanent.getCard().getName()
                    + " (" + permanent.getChargeCounters() + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, counterLog);
        }

        // Pay mill-controller cost
        if (millControllerCost.isPresent()) {
            graveyardService.resolveMillPlayer(gameData, playerId, millControllerCost.get().count());
        }

        for (PermanentChoiceCostHandler handler : permanentChoiceCosts) {
            // Capture sacrificed creature's tracked values before auto-pay (e.g. Birthing Pod, Fling)
            if (handler.costEffect() instanceof SacrificeCreatureCost sacCost
                    && (sacCost.trackSacrificedManaValue() || sacCost.trackSacrificedPower() || sacCost.trackSacrificedToughness())) {
                List<UUID> autoPayIds = handler.getValidChoiceIds(gameData, playerId);
                if (autoPayIds.size() <= handler.requiredCount() && !autoPayIds.isEmpty()) {
                    Permanent autoTarget = gameQueryService.findPermanentById(gameData, autoPayIds.getFirst());
                    if (autoTarget != null) {
                        if (sacCost.trackSacrificedManaValue()) effectiveXValue = autoTarget.getCard().getManaValue();
                        if (sacCost.trackSacrificedPower()) effectiveXValue = gameQueryService.getEffectivePower(gameData, autoTarget);
                        if (sacCost.trackSacrificedToughness()) effectiveXValue = gameQueryService.getEffectiveToughness(gameData, autoTarget);
                    }
                }
            }
            if (handlePermanentChoiceCost(gameData, player, permanent, effectiveIndex,
                    effectiveXValue, targetId, targetZone, handler)) {
                return;
            }
        }

        boolean nonTargeting = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget();
        completeActivationAndRecord(gameData, player, permanent, ability, abilityEffects,
                effectiveXValue, targetId, targetZone, nonTargeting, effectiveIndex, targetIds, damageAssignments);
    }

    PermanentChoiceCostHandler toPermanentChoiceCostHandler(CardEffect effect, UUID sourcePermanentId, int xValue) {
        PermanentSacrificeAction sacAction = this::sacrificePermanentAsCost;
        PermanentBounceAction bounceAction = this::returnPermanentToHandAsCost;
        if (effect instanceof SacrificeCreatureCost c) return new CreatureSacrificeCostHandler(c, gameQueryService, sacAction, sourcePermanentId);
        if (effect instanceof SacrificeSubtypeCreatureCost c) return new SubtypeSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof SacrificeArtifactCost c) return new ArtifactSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof SacrificePermanentCost c) return new MultiplePermanentSacrificeCostHandler(c, gameQueryService, sacAction, sourcePermanentId);
        if (effect instanceof SacrificeMultiplePermanentsCost c) return new MultiplePermanentSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof ReturnMultiplePermanentsToHandCost c) return new MultiplePermanentReturnToHandCostHandler(c, gameQueryService, bounceAction);
        if (effect instanceof TapCreatureCost c) return new TapCreatureCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService);
        if (effect instanceof TapMultiplePermanentsCost c) return new MultiplePermanentTapCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof TapXPermanentsCost c) return new TapXPermanentsCostHandler(c, xValue, gameQueryService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof CrewCost c) return new CrewCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        return null;
    }

    private boolean handlePermanentChoiceCost(GameData gameData, Player player, Permanent source,
                                               int abilityIndex, int xValue, UUID targetId, Zone targetZone,
                                               PermanentChoiceCostHandler handler) {
        int required = handler.requiredCount();
        if (required <= 0) return false;
        UUID playerId = player.getId();
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
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                playerId, source.getId(), abilityIndex, xValue, targetId, targetZone,
                handler.costEffect(), required));
        playerInputService.beginPermanentChoice(gameData, playerId, validIds,
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

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(context.costEffect(), context.sourcePermanentId(), context.xValue());
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
        }

        handler.validateAndPay(gameData, player, chosen);

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
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilityCostChoice(
                        playerId, context.sourcePermanentId(), context.abilityIndex(), context.xValue(),
                        context.targetId(), context.targetZone(), context.costEffect(), remaining));
                playerInputService.beginPermanentChoice(gameData, playerId, validIds,
                        handler.getPromptMessage(remaining));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        }

        int finalXValue = updatedXValue != null ? updatedXValue : (context.xValue() != null ? context.xValue() : 0);
        boolean nonTargeting = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget();
        completeActivationAndRecord(gameData, player, sourcePermanent, ability, abilityEffects,
                finalXValue, context.targetId(), context.targetZone(), nonTargeting, effectiveIndex);
    }

    private ActivatedAbility resolveAbility(GameData gameData, Permanent permanent, Integer abilityIndex) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities;
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            abilities = new ArrayList<>(staticBonus.grantedActivatedAbilities());
        } else {
            abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
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
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, xValue, targetId, targetZone, nonTargeting, targetIds, damageAssignments);
        recordAbilityActivationUse(gameData, permanent, abilityIndex);
    }

    private void sacrificePermanentAsCost(GameData gameData, Player player, Permanent sacTarget) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(sacTarget)) {
            throw new IllegalStateException("Must sacrifice a permanent you control");
        }
        permanentRemovalService.removePermanentToGraveyard(gameData, sacTarget);
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, sacTarget.getCard());
        String sacLog = player.getUsername() + " sacrifices " + sacTarget.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, sacLog);
    }

    private void returnPermanentToHandAsCost(GameData gameData, Player player, Permanent target) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(target)) {
            throw new IllegalStateException("Must return a permanent you control");
        }
        permanentRemovalService.removePermanentToHand(gameData, target);
        String bounceLog = player.getUsername() + " returns " + target.getCard().getName() + " to hand.";
        gameBroadcastService.logAndBroadcast(gameData, bounceLog);
    }

    private void validateTimingRestrictions(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability) {
        if (ability.getTimingRestriction() != null) {
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
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_WHILE_ATTACKING) {
                if (!permanent.isAttacking()) {
                    throw new IllegalStateException("Activate only if this creature is attacking");
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
            if (ability.getTimingRestriction() == ActivationTimingRestriction.RAID) {
                if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) {
                    throw new IllegalStateException("Raid — activate only if you attacked this turn");
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

        // Subtype count restriction (e.g. "Activate only if you control five or more Vampires")
        if (ability.getRequiredControlledSubtype() != null) {
            int count = gameQueryService.countControlledSubtypePermanents(gameData, playerId, ability.getRequiredControlledSubtype());
            if (count < ability.getRequiredControlledSubtypeCount()) {
                throw new IllegalStateException("Activate only if you control " + ability.getRequiredControlledSubtypeCount()
                        + " or more " + ability.getRequiredControlledSubtype().name() + "s");
            }
        }
    }

    private void validateGraveyardTimingRestrictions(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability.getTimingRestriction() == ActivationTimingRestriction.RAID) {
            if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) {
                throw new IllegalStateException("Raid — activate only if you attacked this turn");
            }
        }
    }

    private void validateAndPayLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int effectiveXValue) {
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
        // Once per turn (twice with AllowExtraLoyaltyActivationEffect, e.g. Oath of Teferi)
        int maxActivations = gameQueryService.hasExtraLoyaltyActivation(gameData, playerId) ? 2 : 1;
        if (permanent.getLoyaltyActivationsThisTurn() >= maxActivations) {
            throw new IllegalStateException("Only one loyalty ability per planeswalker per turn");
        }

        int loyaltyCost;
        if (ability.isVariableLoyaltyCost()) {
            // Variable loyalty cost (-X): player chooses X via xValue, cost is -X
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (effectiveXValue > permanent.getLoyaltyCounters()) {
                throw new IllegalStateException("Not enough loyalty counters");
            }
            loyaltyCost = -effectiveXValue;
        } else {
            loyaltyCost = ability.getLoyaltyCost();
            // For negative loyalty costs, check sufficient loyalty
            if (loyaltyCost < 0 && permanent.getLoyaltyCounters() < Math.abs(loyaltyCost)) {
                throw new IllegalStateException("Not enough loyalty counters");
            }
        }

        // Pay loyalty cost
        permanent.setLoyaltyCounters(permanent.getLoyaltyCounters() + loyaltyCost);
        permanent.setLoyaltyActivationsThisTurn(permanent.getLoyaltyActivationsThisTurn() + 1);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext) {
        payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext, 0);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext, int additionalCost) {
        ManaCost cost = new ManaCost(abilityCost);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        boolean hasRestricted = artifactContext || myrContext;

        // Pay Phyrexian mana first so colored mana is reserved for Phyrexian symbols
        // before generic costs consume it
        int phyrexianLifeCost = 0;
        if (cost.hasPhyrexianMana()) {
            phyrexianLifeCost = cost.payPhyrexianMana(pool);
        }

        if (cost.hasX()) {
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (hasRestricted) {
                if (!cost.canPay(pool, effectiveXValue + additionalCost, artifactContext, myrContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost, artifactContext, myrContext);
            } else {
                if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost);
            }
        } else {
            if (hasRestricted) {
                if (!cost.canPay(pool, additionalCost, artifactContext, myrContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, additionalCost, artifactContext, myrContext);
            } else {
                if (additionalCost > 0) {
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
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - phyrexianLifeCost);
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " pays " + phyrexianLifeCost + " life for Phyrexian mana.");
        }
    }

    private List<Integer> collectDiscardIndices(List<Card> hand, DiscardCardTypeCost cost) {
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (cost.predicate() == null || gameQueryService.matchesCardPredicate(hand.get(i), cost.predicate(), null)) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void beginDiscardCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                        UUID targetId, Zone targetZone, String costLabel, List<Integer> validDiscardIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
                targetZone,
                costLabel
        );
        gameData.interaction.beginCardChoice(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE, playerId, new HashSet<>(validDiscardIndices), null);
        String labelText = costLabel != null ? costLabel + " " : "";
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                validDiscardIndices,
                "Choose a " + labelText + "card to discard as an activation cost."
        ));
    }

    private void payDiscardCost(GameData gameData, Player player, DiscardCardTypeCost cost, Integer discardCardIndex) {
        if (discardCardIndex == null) {
            throw new IllegalStateException("Must choose a card to discard");
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validDiscardIndices = collectDiscardIndices(hand, cost);
        Set<Integer> validSet = new HashSet<>(validDiscardIndices);
        if (!validSet.contains(discardCardIndex)) {
            String costLabel = cost.label() != null ? cost.label() + " " : "";
            throw new IllegalStateException("Must discard a " + costLabel + "card");
        }

        Card discarded = hand.remove((int) discardCardIndex);
        graveyardService.addCardToGraveyard(gameData, player.getId(), discarded);
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
                                               UUID targetId, Zone targetZone, CardType requiredType, List<Integer> validExileIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
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
        exileService.exileCard(gameData, playerId, exiled);

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
        validateNotBlockedByNameLock(gameData, permanent.getCard().getName(), isManaAbility(ability));
    }

    private void validateNotBlockedByStaticAbilityLock(GameData gameData, Permanent permanent) {
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock) {
                        if (gameQueryService.matchesPermanentPredicate(gameData, permanent, lock.predicate())) {
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
                if (!cardName.equals(p.getChosenName())) continue;
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
        for (UUID pid : gameData.orderedPlayerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                if (p.isAttached() && p.getAttachedTo().equals(permanent.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof EnchantedPermanentBecomesTypeEffect landTypeEffect) {
                            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(
                                    landTypeEffect.subtype());
                        }
                        if (effect instanceof EnchantedPermanentBecomesChosenTypeEffect
                                && p.getChosenSubtype() != null) {
                            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(
                                    p.getChosenSubtype());
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean isManaAbilityAt(GameData gameData, UUID playerId, int permanentIndex, Integer abilityIndex) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null || permanentIndex < 0 || permanentIndex >= bf.size() || abilityIndex == null) return false;
        Permanent perm = bf.get(permanentIndex);
        ActivatedAbility ability = resolveAbility(gameData, perm, abilityIndex);
        return isManaAbility(ability);
    }

    private boolean isManaAbility(ActivatedAbility ability) {
        if (ability.isNeedsTarget() || ability.isNeedsSpellTarget() || ability.getLoyaltyCost() != null) {
            return false;
        }
        List<CardEffect> effects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();
        return !effects.isEmpty() && effects.stream().anyMatch(e -> e instanceof ManaProducingEffect);
    }
}




