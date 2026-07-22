package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.ability.cost.ArtifactSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CreatureSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentReturnToHandCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.SequencePermanentSacrificeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentUntapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentBounceAction;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentChoiceCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PermanentSacrificeAction;
import com.github.laxika.magicalvibes.service.ability.cost.SacrificeXPermanentsCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCreatureCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapXPermanentsCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapTwoSharingCreatureTypeCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.CrewCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.RemoveCounterFromCreatureCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.PutCounterOnCreatureCostHandler;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaActivation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.PendingGraveyardAbilityActivation;
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
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FreeCyclingEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.RevealTwoCardsSharingColorCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapXPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapTwoCreaturesSharingTypeCost;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final CastingCostService castingCostService;
    private final TargetLegalityService targetLegalityService;
    private final ActivatedAbilityExecutionService activatedAbilityExecutionService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileService exileService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

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
     * The mana quantity of an {@code ON_TAP} {@link AwardManaEffect}. Tap-for-mana is always a flat
     * ({@link Fixed}) amount — basic lands and mana creatures — so there is no evaluation context to
     * build here; a non-fixed amount (which never occurs in an {@code ON_TAP} slot) contributes 0.
     */
    private static int onTapManaAmount(AwardManaEffect effect) {
        return effect.amount() instanceof Fixed fixed ? fixed.value() : 0;
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
        // Check for land type override (e.g. Evil Presence making a land into a Swamp)
        ManaColor overriddenManaColor = getOverriddenLandManaColor(gameData, permanent);
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty() && overriddenManaColor == null) {
            throw new IllegalStateException("Permanent has no tap effects");
        }
        if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)
                && !gameQueryService.canActivateCreatureAbilitiesAsThoughHaste(gameData, playerId)) {
            throw new IllegalStateException("Creature has summoning sickness");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        // Serra Bestiary: tapping for mana is a {T} ability, so it is locked too.
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateTapAbilitiesEffect.class)) {
            throw new IllegalStateException("Tap abilities of " + permanent.getCard().getName() + " can't be activated (Serra Bestiary)");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent);

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        EnumMap<ManaColor, Integer> poolBefore = snapshotPoolColors(manaPool);
        EnumMap<ManaColor, Integer> creatureManaBefore = snapshotCreatureManaColors(manaPool);
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);
        // Mana Reflection: tapping a permanent for mana produces twice as much of that mana (2^count).
        int manaMultiplier = gameQueryService.manaProductionMultiplier(gameData, playerId);
        if (overriddenManaColor != null) {
            // Land type is overridden — produce the new basic land type's mana instead of original
            manaPool.add(overriddenManaColor, manaMultiplier);
        } else {
            // Damping Sphere replacement: if a land is tapped for two or more mana, it produces {C} instead.
            boolean dampingReplacement = false;
            if (permanent.getCard().hasType(CardType.LAND) && isDampingManaReplacementActiveOnTap(gameData)) {
                int totalMana = 0;
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        totalMana += onTapManaAmount(awardMana);
                    }
                }
                if (totalMana >= 2) {
                    dampingReplacement = true;
                    manaPool.add(ManaColor.COLORLESS, manaMultiplier);
                }
            }
            if (!dampingReplacement) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect awardMana) {
                        int amount = onTapManaAmount(awardMana) * manaMultiplier;
                        manaPool.add(awardMana.color(), amount);
                        if (isCreatureSource) {
                            manaPool.addCreatureMana(awardMana.color(), amount);
                        }
                    }
                }
            }
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.playerTaps(player.getUsername(), permanent.getCard()));

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
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
        List<StackEntry> deferred = List.of();
        if (gameData.stack.size() > stackBeforeTriggers) {
            deferred = new ArrayList<>(
                    gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()));
            gameData.stack.subList(stackBeforeTriggers, gameData.stack.size()).clear();
            gameData.pendingManaAbilityTriggers.addAll(deferred);
        }

        recordRevertableManaActivation(gameData, playerId, permanent, poolBefore, creatureManaBefore, deferred);

        gameBroadcastService.broadcastGameState(gameData);
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
                playerId, permanent.getId(), manaAdded, creatureManaAdded, List.copyOf(deferredTriggers)));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} reverts their mana ability activations", gameData.id, player.getUsername());
        }
        gameBroadcastService.broadcastGameState(gameData);
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
        ManaColor overriddenManaColor = getOverriddenLandManaColor(gameData, permanent);
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty() && overriddenManaColor == null) {
            throw new IllegalStateException("Land has no mana ability");
        }

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        if (overriddenManaColor != null) {
            manaPool.add(overriddenManaColor, 1);
            manaPool.addSpellOnlyMana(overriddenManaColor, 1);
        } else {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof AwardManaEffect awardMana) {
                    int amount = onTapManaAmount(awardMana);
                    manaPool.add(awardMana.color(), amount);
                    manaPool.addSpellOnlyMana(awardMana.color(), amount);
                }
            }
        }

        gameBroadcastService.logAndBroadcast(gameData,
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

        gameBroadcastService.broadcastGameState(gameData);
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

        gameData.playerLifeTotals.put(playerId, life - 1);
        gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, 1);

        String logEntry = player.getUsername() + " pays 1 life to add {C} (Channel).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} pays 1 life for colorless mana", gameData.id, player.getUsername());

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
        // Overwhelming Splendor: sacrifice abilities are never mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, null);

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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " sacrifices " , permanent.getCard(), "."));
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
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, null, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, null, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null, null, targetIds, damageAssignments, null);
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
        // Spell-only mana (Piracy) can't pay ability costs — hide it for the duration of this activation.
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (pool != null) {
            pool.setWhiteSpendableAsRed(gameQueryService.canSpendWhiteManaAsRed(gameData, player.getId()));
        }
        Map<ManaColor, Integer> withheldSpellOnlyMana = pool != null ? pool.withdrawSpellOnlyMana() : Map.of();
        try {
            activateGraveyardAbilityImpl(gameData, player, graveyardCardIndex, abilityIndex,
                    xValue != null ? xValue : 0, targetId);
        } finally {
            if (pool != null && !withheldSpellOnlyMana.isEmpty()) {
                pool.restoreSpellOnlyMana(withheldSpellOnlyMana);
            }
        }
    }

    private void activateGraveyardAbilityImpl(GameData gameData, Player player, int graveyardCardIndex,
                                              Integer abilityIndex, int xValue, UUID targetId) {
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
        targetLegalityService.validateActivatedAbilityTargeting(
                gameData, playerId, ability, abilityEffects, targetId, null, card, xValue);

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

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);

        // Identify permanent-choice costs (e.g. return lands to hand)
        List<PermanentChoiceCostHandler> permanentChoiceCosts = ability.getEffects().stream()
                .map(e -> toPermanentChoiceCostHandler(e, null, 0))
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
        if (exileNGraveyardCost != null
                && matchingGraveyardExileCandidates(graveyard, exileNGraveyardCost.requiredType(), card).size()
                        < exileNGraveyardCost.count()) {
            String typeName = graveyardExileFilterLabel(exileNGraveyardCost.requiredType(), null);
            throw new IllegalStateException("Not enough " + typeName + "cards in graveyard to exile (need "
                    + exileNGraveyardCost.count() + ")");
        }

        // Discard-card(s) activation cost (Eternalize—{cost}, Discard a card / Haunted Dead Discard two):
        // validate up front that enough legal cards exist before paying any cost, so an unpayable
        // discard makes activation illegal without side effects (CR 602.2a).
        DiscardCardTypeCost discardCardTypeCost = ability.getEffects().stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null
                && collectDiscardIndices(gameData.playerHands.get(playerId), discardCardTypeCost, xValue).size()
                < discardCardTypeCost.count()) {
            throw new IllegalStateException("No valid card to discard for the activation cost");
        }

        // Pay mana cost. Static effects (Embalmer's Tools) can make a matching graveyard card's
        // ability cost {N} less to activate; the reduction is floored to the generic portion so the
        // cost never drops below its colored requirements, then threaded through as a negative
        // additional generic cost.
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            int reduction = Math.min(
                    castingCostService.getGraveyardActivatedAbilityCostReduction(gameData, playerId, card),
                    new ManaCost(abilityCost).getGenericCost());
            payManaCost(gameData, playerId, abilityCost, xValue, false, false, null, -reduction);
        }

        // Pay the exile-N-cards-from-graveyard cost
        if (exileNGraveyardCost != null) {
            payGraveyardExileNCost(gameData, player, exileNGraveyardCost, card);
        }

        // Pay the exile-this-card cost (Embalm / Eternalize). Exiling the source now — before the
        // ability is put on the stack — prevents the same graveyard card from being activated twice.
        if (ability.getEffects().stream().anyMatch(ExileSelfFromGraveyardCost.class::isInstance)) {
            graveyard.remove(card);
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
            exileService.exileCard(gameData, playerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
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
        if (discardCardTypeCost != null) {
            List<Integer> validDiscardIndices = collectDiscardIndices(gameData.playerHands.get(playerId), discardCardTypeCost, xValue);
            gameData.pendingGraveyardAbilityActivation = new PendingGraveyardAbilityActivation(
                    playerId, card, ability, xValue, targetId, discardCardTypeCost.count());
            String labelText = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
            String prompt = discardCardTypeCost.count() > 1
                    ? "Choose a " + labelText + "card to discard as an activation cost ("
                    + discardCardTypeCost.count() + " remaining)."
                    : "Choose a " + labelText + "card to discard as an activation cost.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    playerId, validDiscardIndices, prompt));
            return;
        }

        completeGraveyardAbilityActivation(gameData, player, card, ability, xValue, targetId);
    }

    /**
     * The graveyard-activated abilities a card offers from the given owner's graveyard: its own
     * printed graveyard abilities plus any granted to owned creature cards by static effects on the
     * battlefield (e.g. Sedris, the Traitor King grants unearth {2}{B}). Granted abilities are
     * appended after the card's own so indices stay aligned with the client's card view.
     */
    private List<ActivatedAbility> effectiveGraveyardAbilities(GameData gameData, Card card, UUID ownerId) {
        List<ActivatedAbility> abilities = new ArrayList<>(card.getGraveyardActivatedAbilities());
        if (card.hasType(CardType.CREATURE)) {
            abilities.addAll(gameQueryService.computeGrantedGraveyardAbilitiesForOwnedCreatureCard(gameData, ownerId));
        }
        return abilities;
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
        ActivatedAbility ability = effectiveGraveyardAbilities(gameData, card, playerId).get(idx);

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

        completeGraveyardAbilityActivation(gameData, player, card, ability, 0, null);
    }

    private void completeGraveyardAbilityActivation(GameData gameData, Player player, Card card,
                                                    ActivatedAbility ability, int xValue, UUID targetId) {
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
                snapshotEffects,
                xValue,
                targetId,
                Map.of()
        );
        stackEntry.setTargetFilter(ability.getTargetFilter());
        gameData.stack.add(stackEntry);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " activates " , card, "'s ability from the graveyard."));
        log.info("Game {} - {} activates {}'s graveyard ability", gameData.id, player.getUsername(), card.getName());

        // "Whenever you activate an eternalize or embalm ability, draw a card" (Vizier of the
        // Anointed). Fired after the ability is on the stack so the draw trigger lands above it.
        if (ability.isEmbalmOrEternalize()) {
            triggerCollectionService.checkControllerActivatesEternalizeOrEmbalmTriggers(gameData, playerId);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
    }

    /**
     * Activates an ability of a card in the player's hand (e.g. Reinforce N—{cost}:
     * "{cost}, Discard this card: Put N +1/+1 counters on target creature."). Discarding
     * the source card is an intrinsic part of the activation cost.
     *
     * <p>Validates targeting before any cost is paid (CR 601.2c), pays the mana cost, discards
     * the source card to the graveyard, then pushes the ability onto the stack.</p>
     */
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
        List<ActivatedAbility> abilities = card.getHandActivatedAbilities();
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no hand-activated ability");
        }

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        List<CardEffect> abilityEffects = ability.getEffects();
        int effectiveXValue = xValue != null ? xValue : 0;

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);

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

        // Pay mana cost (throws before mutating the pool if it can't be afforded). A static effect
        // may replace a cycling ability's mana cost with {0} (New Perspectives, CR 118.9): the card
        // being cycled still counts toward the hand-size condition, so check the current hand size
        // before it is discarded below as part of the cost.
        String abilityCost = ability.getManaCost();
        if (abilityCost != null && cyclingCostReplacedWithZero(gameData, playerId, ability, hand.size())) {
            abilityCost = null;
        }
        if (abilityCost != null) {
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, false, false);
        }

        // Pay the "discard this card" cost intrinsic to a hand-activated ability
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

        // Push the ability onto the stack (cost effects are not part of the resolution snapshot)
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (!(effect instanceof CostEffect)) {
                snapshotEffects.add(effect);
            }
        }
        // A spell/ability-on-stack target (e.g. a cycling counter trigger) must carry Zone.STACK so
        // the ability resolves against the stack instead of fizzling as a missing permanent target.
        Zone targetZone = ability.isNeedsSpellTarget() ? Zone.STACK : null;
        gameData.stack.add(new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                card,
                playerId,
                card.getName() + "'s ability",
                snapshotEffects,
                effectiveXValue,
                targetId,
                null,
                Map.of(),
                targetZone,
                List.of(),
                List.of()
        ));

        // Discard triggers fire after the ability is on the stack (CR 601.2h), so a "whenever you
        // cycle or discard" trigger (e.g. Curator of Mysteries) lands above the cycling draw and
        // resolves first.
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " activates " , card, "'s ability from their hand."));
        log.info("Game {} - {} activates {}'s hand ability", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
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
        List<ActivatedAbility> abilities = card.getHandActivatedAbilities();
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Card has no hand-activated ability");
        }

        int idx = abilityIndex != null ? abilityIndex : 0;
        if (idx < 0 || idx >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(idx);
        List<CardEffect> abilityEffects = ability.getEffects();

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);

        // Validate targeting before any cost is paid — an illegal activation rewinds cleanly (CR 601.2c)
        targetLegalityService.validateMultiTargetGraveyardAbility(gameData, playerId, abilityEffects, graveyardCardIds);

        // Pay mana cost (throws before mutating the pool if it can't be afforded)
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            payManaCost(gameData, playerId, abilityCost, 0, false, false);
        }

        // Pay the "discard this card" cost intrinsic to a hand-activated ability
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
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);

        // Push the ability onto the stack with its graveyard targets (cost effects are not resolved)
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (!(effect instanceof CostEffect)) {
                snapshotEffects.add(effect);
            }
        }
        gameData.stack.add(new StackEntry(
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
        ));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " activates " , card, "'s ability from their hand."));
        log.info("Game {} - {} activates {}'s hand ability targeting graveyards", gameData.id, player.getUsername(), card.getName());

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
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
            PendingAbilityActivation pending = gameData.pendingAbilityActivation;
            String costLabel = pending.discardCostLabel();
            String labelText = costLabel != null ? costLabel + " " : "";
            sessionManager.sendToPlayer(player.getId(), InteractionPromptMessage.cardIndexPick(
                    new ArrayList<>(cardChoice.validIndices()),
                    "Choose a " + labelText + "card to discard as an activation cost.", false
            ));
            return;
        }

        PendingAbilityActivation pending = gameData.pendingAbilityActivation;
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        ActivatedAbility ability = resolveAbility(gameData, source, pending.abilityIndex());
        DiscardCardTypeCost discardCost = ability.getEffects().stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElseThrow();

        gameData.interaction.clearAwaitingInput();
        payDiscardCost(gameData, player, discardCost, cardIndex, pending.xValue());

        int remaining = pending.remainingDiscards() - 1;
        if (remaining > 0) {
            List<Integer> validDiscardIndices = collectDiscardIndices(
                    gameData.playerHands.get(player.getId()), discardCost, pending.xValue());
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
                    remaining
            );
            String labelText = pending.discardCostLabel() != null ? pending.discardCostLabel() + " " : "";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    player.getId(), validDiscardIndices,
                    "Choose a " + labelText + "card to discard as an activation cost ("
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
                pending.xValue(),
                pending.targetId(),
                pending.targetZone(),
                -1,
                null,
                null,
                null,
                source
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
        DiscardCardTypeCost discardCost = ability.getEffects().stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElseThrow();

        if (cardChoice.validIndices() == null || !cardChoice.validIndices().contains(cardIndex)) {
            // Invalid index — re-prompt the discard cost choice.
            String labelText = discardCost.label() != null ? discardCost.label() + " " : "";
            String prompt = pending.remainingDiscards() > 1
                    ? "Choose a " + labelText + "card to discard as an activation cost ("
                    + pending.remainingDiscards() + " remaining)."
                    : "Choose a " + labelText + "card to discard as an activation cost.";
            sessionManager.sendToPlayer(player.getId(), InteractionPromptMessage.cardIndexPick(
                    new ArrayList<>(cardChoice.validIndices()), prompt, false));
            return;
        }

        gameData.interaction.clearAwaitingInput();
        payDiscardCost(gameData, player, discardCost, cardIndex, pending.xValue());

        int remaining = pending.remainingDiscards() - 1;
        if (remaining > 0) {
            List<Integer> validDiscardIndices = collectDiscardIndices(
                    gameData.playerHands.get(player.getId()), discardCost, pending.xValue());
            if (validDiscardIndices.size() < remaining) {
                gameData.pendingGraveyardAbilityActivation = null;
                throw new IllegalStateException("No valid card to discard for the activation cost");
            }
            gameData.pendingGraveyardAbilityActivation = new PendingGraveyardAbilityActivation(
                    pending.playerId(), card, ability, pending.xValue(), pending.targetId(), remaining);
            String labelText = discardCost.label() != null ? discardCost.label() + " " : "";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardCostChoice(
                    pending.playerId(), validDiscardIndices,
                    "Choose a " + labelText + "card to discard as an activation cost ("
                            + remaining + " remaining)."));
            return;
        }

        gameData.pendingGraveyardAbilityActivation = null;
        completeGraveyardAbilityActivation(gameData, player, card, ability, pending.xValue(), pending.targetId());
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
                null,
                null,
                source
        );
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource) {
        // Spell-only mana (e.g. tapped via Piracy) can't pay ability costs — hide it for the duration of
        // this activation (including the affordability check) so it is neither counted nor spent, then
        // restore it afterward. Re-entrant callbacks (discard/exile cost) call this method afresh, so each
        // pass withdraws and restores symmetrically.
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (pool != null) {
            // Refresh the "spend white as red" permission (Sunglasses of Urza) so this ability's cost
            // affordability check and payment honor it.
            pool.setWhiteSpendableAsRed(gameQueryService.canSpendWhiteManaAsRed(gameData, player.getId()));
        }
        Map<ManaColor, Integer> withheldSpellOnlyMana = pool != null ? pool.withdrawSpellOnlyMana() : Map.of();
        try {
            activateAbilityInternalImpl(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone,
                    discardCardIndex, exileGraveyardCardIndex, targetIds, damageAssignments, preResolvedSource);
        } finally {
            if (pool != null && !withheldSpellOnlyMana.isEmpty()) {
                pool.restoreSpellOnlyMana(withheldSpellOnlyMana);
            }
        }
    }

    private void activateAbilityInternalImpl(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetId, Zone targetZone, Integer discardCardIndex, Integer exileGraveyardCardIndex,
                                         List<UUID> targetIds, Map<UUID, Integer> damageAssignments, Permanent preResolvedSource) {
        int effectiveXValue = xValue != null ? xValue : 0;

        UUID playerId = player.getId();
        Permanent permanent = preResolvedSource != null
                ? preResolvedSource
                : resolveActivationSource(gameData, playerId, permanentIndex, abilityIndex);
        if (permanent == null) {
            throw new IllegalStateException("Invalid permanent index");
        }
        List<ActivatedAbility> abilities = getEffectiveActivatedAbilities(gameData, permanent);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Permanent has no activated ability");
        }

        int effectiveIndex = effectiveAbilityIndex(abilityIndex);
        ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
        List<CardEffect> abilityEffects = ability.getEffects();
        String abilityCost = ability.getManaCost();

        // Compute targeting tax from effects like Kopala, Warden of Waves (feeds the mana affordability check)
        int targetingTax = castingCostService.getTargetingSubtypeTax(gameData, playerId, targetId, targetIds);

        // Add any static ability-activation tax on this source (e.g. Gloom: white enchantments' abilities cost {3} more)
        targetingTax += castingCostService.getActivatedAbilityActivationTax(gameData, permanent);

        // Apply per-counter generic cost reduction (e.g. Diary of Dreams: costs {1} less per page counter).
        // Threaded through the additional-generic-cost path as a negative value, floored so the generic
        // portion of the cost never drops below zero; feeds both the affordability check and payment.
        if (abilityCost != null) {
            for (CardEffect e : abilityEffects) {
                if (e instanceof ReduceActivationCostPerCounterEffect reduce) {
                    int genericCost = new ManaCost(abilityCost).getGenericCost();
                    int reduction = Math.min(
                            permanent.getCounterCount(reduce.counterType()) * reduce.reductionPerCounter(),
                            genericCost);
                    targetingTax -= reduction;
                }
            }
        }

        // All state-based legality checks, shared with the AI's dry-run query. Nothing is mutated
        // until every check (including targeting below) has passed, so an illegal activation
        // rewinds cleanly with no cost paid (CR 602.2b/601.2c).
        // discardCardIndex < 0 means the interactive path already paid all required discards — skip
        // the discard-hand check so the re-entry does not fail after the cards left the hand.
        validateActivationLegality(gameData, playerId, permanent, ability, effectiveIndex, effectiveXValue,
                gameData.playerManaPools.get(playerId), targetingTax,
                discardCardIndex != null && discardCardIndex < 0);

        // Validate spell target for abilities that counter spells
        if (ability.isNeedsSpellTarget()) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetId, ability.getTargetFilter(), playerId, permanent);
        }

        UUID sourceId = permanent.getId();
        final int xValueForCost = effectiveXValue;
        List<PermanentChoiceCostHandler> permanentChoiceCosts = abilityEffects.stream()
                .map(e -> toPermanentChoiceCostHandler(e, sourceId, xValueForCost))
                .filter(Objects::nonNull)
                .toList();

        // For regular targeting abilities, validate legality before costs are paid (CR 602.2b/601.2c).
        if (ability.isMultiTarget() || (ability.getMaxTargets() > 1 && targetIds != null)) {
            targetLegalityService.validateMultiTargetAbility(gameData, playerId, ability,
                    targetIds != null ? targetIds : List.of(), permanent.getCard());
        } else if (targetZone == Zone.GRAVEYARD && targetIds != null && !targetIds.isEmpty()) {
            targetLegalityService.validateMultiTargetGraveyardAbility(gameData, playerId, abilityEffects, targetIds);
        } else {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetId, targetZone, permanent.getCard(), effectiveXValue);
        }

        // Pay the loyalty cost only now that full legality, including targets, is confirmed
        // (CR 601.2: an illegal activation rewinds with no cost paid)
        if (ability.getLoyaltyCost() != null) {
            payLoyaltyCost(gameData, playerId, permanent, ability, effectiveXValue);
        }

        Optional<PayLifeCost> payLifeCost = abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst();

        ExileCardFromGraveyardCost exileGraveyardCost = abilityEffects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileGraveyardCost != null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, exileGraveyardCost.requiredType(),
                    exileGraveyardCost.requiredSubtype());
            if (exileGraveyardCardIndex == null) {
                beginGraveyardExileCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                        exileGraveyardCost.requiredType(), exileGraveyardCost.requiredSubtype(), validExileIndices);
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

        DiscardCardTypeCost discardCardTypeCost = abilityEffects.stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Integer> validDiscardIndices = collectDiscardIndices(hand, discardCardTypeCost, effectiveXValue);
            if (discardCardIndex == null) {
                if (validDiscardIndices.size() < discardCardTypeCost.count()) {
                    String costLabel = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
                    throw new IllegalStateException("Must discard a " + costLabel + "card to activate ability");
                }
                beginDiscardCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetId, targetZone,
                        discardCardTypeCost.label(), validDiscardIndices, discardCardTypeCost.count());
                return;
            }
        }

        Optional<RemoveCounterFromSourceCost> removeCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveCounterFromSourceCost)
                .map(e -> (RemoveCounterFromSourceCost) e)
                .findFirst();

        Optional<MillControllerCost> millControllerCost = abilityEffects.stream()
                .filter(e -> e instanceof MillControllerCost)
                .map(e -> (MillControllerCost) e)
                .findFirst();

        boolean discardHandCost = abilityEffects.stream().anyMatch(e -> e instanceof DiscardHandCost);

        Optional<RemoveChargeCountersFromSourceCost> removeChargeCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveChargeCountersFromSourceCost)
                .map(e -> (RemoveChargeCountersFromSourceCost) e)
                .findFirst();

        // Validate X for Prototype Portal-style abilities here rather than in the shared legality
        // check alone: when the same ability's exile cost just imprinted a card (re-entry after the
        // graveyard choice), the imprint only exists at this point.
        validateImprintedCopyXValue(gameData, permanent, abilityEffects, effectiveXValue);

        // Pay mana cost (including targeting tax if applicable)
        if (abilityCost != null) {
            boolean artifactContext = gameQueryService.isArtifact(permanent);
            boolean myrContext = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            Set<CardSubtype> subtypeSpellOrAbilityContext = effectiveSubtypes(permanent);
            payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext, subtypeSpellOrAbilityContext, targetingTax);
        } else if (targetingTax > 0) {
            // No base mana cost but targeting tax applies — pay generic mana for the tax
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost taxCost = new ManaCost("{" + targetingTax + "}");
            taxCost.pay(pool);
        }

        // Pay life cost
        if (payLifeCost.isPresent()) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            int amount = payLifeCost.get().effectiveAmount(currentLife);
            gameData.playerLifeTotals.put(playerId, currentLife - amount);
        }

        // discardCardIndex < 0 means the interactive path already paid all required discards.
        if (discardCardTypeCost != null && discardCardIndex != null && discardCardIndex >= 0) {
            payDiscardCost(gameData, player, discardCardTypeCost, discardCardIndex, effectiveXValue);
        }

        if (exileGraveyardCost != null) {
            payGraveyardExileCost(gameData, player, exileGraveyardCost.requiredType(),
                    exileGraveyardCost.requiredSubtype(), exileGraveyardCardIndex);
        }

        // Pay exile-N-cards-from-graveyard cost by exiling the front N cards (Immortal Coil).
        ExileNCardsFromGraveyardCost exileNGraveyardCostToPay = abilityEffects.stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .map(ExileNCardsFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileNGraveyardCostToPay != null) {
            graveyardService.exileCardsFromGraveyard(gameData, playerId, exileNGraveyardCostToPay.count());
        }

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
                        removedPlus = remaining;
                        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - remaining);
                    }
                }
                case SILVER -> throw new IllegalStateException("Silver counters are not on permanents");
                default -> permanent.setCounterCount(ct, permanent.getCounterCount(ct) - count);
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    player.getUsername() + " removes " + counterWord + " from ", permanent.getCard(), "."));
        }

        // Pay remove-charge-counter cost
        if (removeChargeCost.isPresent()) {
            int required = removeChargeCost.get().count();
            permanent.setCounterCount(CounterType.CHARGE, permanent.getCounterCount(CounterType.CHARGE) - required);
            String counterLog = player.getUsername() + " removes " + required + " charge counter(s) from " + permanent.getCard().getName()
                    + " (" + permanent.getCounterCount(CounterType.CHARGE) + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text(player.getUsername() + " removes " + required + " charge counter(s) from ")
                    .card(permanent.getCard())
                    .text(" (" + permanent.getCounterCount(CounterType.CHARGE) + " remaining).")
                    .build());
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
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placedCount);
                placed = true;
            } else if (!gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
                // Vizier of Remedies reduces the -1/-1 counters put on as a cost (e.g. Devoted Druid).
                placedCount = gameQueryService.reduceMinusOneMinusOneCounters(gameData, permanent, placedCount);
                if (placedCount > 0) {
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + placedCount);
                    placed = true;
                }
            }
            if (placed) {
                String counterLabel = String.format("%+d/%+d", c.powerModifier(), c.toughnessModifier());
                String counterWord = placedCount == 1 ? "a " + counterLabel + " counter" : placedCount + " " + counterLabel + " counters";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " puts " + counterWord + " on ", permanent.getCard(), "."));
            }
        }

        // Pay mill-controller cost
        if (millControllerCost.isPresent()) {
            graveyardService.resolveMillPlayer(gameData, playerId, millControllerCost.get().count());
        }

        // Pay discard-your-hand cost
        if (discardHandCost) {
            payDiscardHandCost(gameData, player);
        }

        // Pay discard-a-card-at-random cost
        if (abilityEffects.stream().anyMatch(e -> e instanceof DiscardRandomCardCost)) {
            payRandomDiscardCost(gameData, player);
        }

        // Pay reveal-two-color-sharing-cards cost: reveal a qualifying pair (cards stay in hand)
        if (abilityEffects.stream().anyMatch(e -> e instanceof RevealTwoCardsSharingColorCost)) {
            List<Card> pair = colorSharingPair(gameData.playerHands.get(playerId));
            if (pair != null) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .text(player.getUsername() + " reveals ")
                        .card(pair.get(0))
                        .text(" and ")
                        .card(pair.get(1))
                        .text(" as a cost.")
                        .build());
            }
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
            // Remember the auto-tapped creature so ChosenPermanentPower can read its power at
            // resolution (Impelled Giant). Only the single-valid-choice case auto-pays here;
            // multi-choice payment records the pick in completeActivatedAbilityCostChoice.
            if (handler.costEffect() instanceof TapCreatureCost tapCost && tapCost.trackTappedCreaturePower()) {
                List<UUID> tapChoiceIds = handler.getValidChoiceIds(gameData, playerId);
                if (tapChoiceIds.size() == 1) {
                    permanent.setChosenPermanentId(tapChoiceIds.getFirst());
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
        return toPermanentChoiceCostHandler(effect, sourcePermanentId, xValue, List.of());
    }

    PermanentChoiceCostHandler toPermanentChoiceCostHandler(CardEffect effect, UUID sourcePermanentId, int xValue,
                                                            List<UUID> chosenSoFar) {
        PermanentSacrificeAction sacAction = this::sacrificePermanentAsCost;
        PermanentBounceAction bounceAction = this::returnPermanentToHandAsCost;
        if (effect instanceof SacrificeCreatureCost c) return new CreatureSacrificeCostHandler(c, gameQueryService, sacAction, sourcePermanentId);
        if (effect instanceof SacrificeArtifactCost c) return new ArtifactSacrificeCostHandler(c, gameQueryService, sacAction);
        if (effect instanceof SacrificePermanentCost c) return new MultiplePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction, sourcePermanentId);
        if (effect instanceof SacrificeMultiplePermanentsCost c) return new MultiplePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction);
        if (effect instanceof SacrificePermanentsSequenceCost c) return new SequencePermanentSacrificeCostHandler(c, predicateEvaluationService, sacAction, chosenSoFar);
        if (effect instanceof ReturnMultiplePermanentsToHandCost c) return new MultiplePermanentReturnToHandCostHandler(c, predicateEvaluationService, bounceAction);
        if (effect instanceof TapCreatureCost c) return new TapCreatureCostHandler(c, gameQueryService, predicateEvaluationService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof TapMultiplePermanentsCost c) return new MultiplePermanentTapCostHandler(c, predicateEvaluationService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof UntapMultiplePermanentsCost c) return new MultiplePermanentUntapCostHandler(c, predicateEvaluationService, gameBroadcastService, sourcePermanentId);
        if (effect instanceof TapXPermanentsCost c) return new TapXPermanentsCostHandler(c, xValue, predicateEvaluationService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof SacrificeXPermanentsCost c) return new SacrificeXPermanentsCostHandler(c, xValue, predicateEvaluationService, sacAction);
        if (effect instanceof TapTwoCreaturesSharingTypeCost c) return new TapTwoSharingCreatureTypeCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService, chosenSoFar);
        if (effect instanceof CrewCost c) return new CrewCostHandler(c, gameQueryService, gameBroadcastService, triggerCollectionService, sourcePermanentId);
        if (effect instanceof RemoveCounterFromControlledCreatureCost c) return new RemoveCounterFromCreatureCostHandler(c, gameQueryService, gameBroadcastService);
        if (effect instanceof PutCounterOnControlledCreatureCost c) return new PutCounterOnCreatureCostHandler(c, gameQueryService, gameBroadcastService);
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

        PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(context.costEffect(), context.sourcePermanentId(), context.xValue(), context.chosenSoFar());
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
            if (sacCost.trackSacrificedColorSymbols() != null) {
                var mc = chosen.getCard().getParsedManaCost();
                updatedXValue = mc != null ? mc.countColorSymbols(sacCost.trackSacrificedColorSymbols()) : 0;
            }
        }

        handler.validateAndPay(gameData, player, chosen);

        // Remember the tapped creature so ChosenPermanentPower reads its power at resolution (Impelled Giant).
        if (context.costEffect() instanceof TapCreatureCost tapCost && tapCost.trackTappedCreaturePower()) {
            sourcePermanent.setChosenPermanentId(chosenPermanentId);
        }

        int remaining = context.remaining() - handler.lastPaymentWeight();
        // Costs whose valid choices depend on prior picks (e.g. tap two creatures sharing a type)
        // need the just-paid permanent threaded into the handler for the remaining choices.
        List<UUID> chosenSoFar = new ArrayList<>(context.chosenSoFar());
        chosenSoFar.add(chosenPermanentId);
        handler = toPermanentChoiceCostHandler(context.costEffect(), context.sourcePermanentId(), context.xValue(), chosenSoFar);
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
                        context.targetId(), context.targetZone(), context.costEffect(), remaining, chosenSoFar));
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

    /**
     * Returns the complete list of activated abilities currently available on a permanent: its own
     * printed abilities (unless it has lost all abilities), abilities granted by static effects,
     * and temporary/until-next-turn abilities. The list order defines the {@code abilityIndex}
     * used by {@code activateAbility}.
     */
    public List<ActivatedAbility> getEffectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities;
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            // Permanent has lost all its own abilities; only static-granted abilities remain
            abilities = new ArrayList<>(staticBonus.grantedActivatedAbilities());
        } else {
            abilities = new ArrayList<>(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        return abilities;
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
        List<Permanent> own = gameData.playerBattlefields.get(activatorId);
        if (own != null && permanentIndex >= 0 && permanentIndex < own.size()) {
            return own.get(permanentIndex);
        }
        int idx = abilityIndex != null ? abilityIndex : 0;
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
        return null;
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
     * X is assumed to be 0. Mana affordability is checked against {@code manaPool}, which may be
     * hypothetical (the AI passes the pool of mana it could produce). Never mutates game state.
     */
    public boolean canActivateAbility(GameData gameData, UUID playerId, Permanent permanent,
                                      int abilityIndex, ManaPool manaPool) {
        try {
            ActivatedAbility ability = resolveAbility(gameData, permanent, abilityIndex);
            validateActivationLegality(gameData, playerId, permanent, ability, abilityIndex, 0, manaPool, 0);
            return true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return false;
        }
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
                manaPool, additionalGenericCost, false);
    }

    /**
     * @param discardCostAlreadyPaid when true, skip the discard-hand size check (interactive path
     *                               already paid the discard(s) before re-entering activation)
     */
    public void validateActivationLegality(GameData gameData, UUID playerId, Permanent permanent,
                                           ActivatedAbility ability, int abilityIndex, int xValue,
                                           ManaPool manaPool, int additionalGenericCost,
                                           boolean discardCostAlreadyPaid) {
        List<CardEffect> abilityEffects = ability.getEffects();

        // Sen Triplets: a player locked out this turn can't activate any ability.
        if (gameData.playersCantActivateAbilitiesThisTurn.contains(playerId)) {
            throw new IllegalStateException("You can't activate abilities this turn");
        }

        // Pithing Needle check: block non-mana activated abilities of the chosen name
        validateNotBlockedByPithingNeedle(gameData, permanent, ability);

        // Arrest check: block all activated abilities of enchanted creature
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName() + " can't be activated (Arrest)");
        }
        validateNotBlockedByStaticAbilityLock(gameData, permanent);

        // Overwhelming Splendor: the enchanted player may activate only mana / loyalty abilities
        validateEnchantedPlayerAbilityRestriction(gameData, playerId, ability);

        // Activation timing restrictions (e.g. "Activate only during your upkeep")
        validateTimingRestrictions(gameData, playerId, permanent, ability);
        validateActivationLimitPerTurn(gameData, permanent, ability, abilityIndex);

        // Loyalty ability restrictions (the cost itself is paid after target legality is confirmed)
        if (ability.getLoyaltyCost() != null) {
            validateLoyaltyCost(gameData, playerId, permanent, ability, xValue);
        }

        // Tap requirement
        if (ability.isRequiresTap()) {
            // Serra Bestiary: only activated abilities with {T} in their costs are locked.
            if (gameQueryService.hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateTapAbilitiesEffect.class)) {
                throw new IllegalStateException("Tap abilities of " + permanent.getCard().getName() + " can't be activated (Serra Bestiary)");
            }
            if (permanent.isTapped()) {
                throw new IllegalStateException("Permanent is already tapped");
            }
            if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)
                    && !gameQueryService.canActivateCreatureAbilitiesAsThoughHaste(gameData, playerId)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        // Untap requirement ({Q}): the permanent must be tapped, and creatures obey the same
        // summoning-sickness restriction as {T} (CR 302.6).
        if (ability.isRequiresUntap()) {
            if (!permanent.isTapped()) {
                throw new IllegalStateException("Permanent is not tapped");
            }
            if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)
                    && !gameQueryService.canActivateCreatureAbilitiesAsThoughHaste(gameData, playerId)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        // Permanent-choice costs (sacrifice, tap others, crew, ...) need enough valid choices
        UUID sourceId = permanent.getId();
        for (CardEffect effect : abilityEffects) {
            PermanentChoiceCostHandler handler = toPermanentChoiceCostHandler(effect, sourceId, xValue);
            if (handler != null) {
                handler.validateCanPay(gameData, playerId);
            }
        }

        // Pay-life cost
        Optional<PayLifeCost> payLifeCost = abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst();
        if (payLifeCost.isPresent()) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            int needed = payLifeCost.get().effectiveAmount(life);
            if (life < needed) {
                throw new IllegalStateException("Not enough life to pay (need " + needed + ", have " + life + ")");
            }
        }

        // Mana affordability (CR 602.2b — checked before entering interactive cost choices)
        String abilityCost = ability.getManaCost();
        if (abilityCost != null) {
            ManaCost preCheck = new ManaCost(abilityCost);
            boolean artifactCtx = gameQueryService.isArtifact(permanent);
            boolean myrCtx = permanent.getCard().getSubtypes().contains(CardSubtype.MYR);
            Set<CardSubtype> soaCtx = effectiveSubtypes(permanent);
            if (preCheck.hasX()) {
                if (!preCheck.canPay(manaPool, xValue + additionalGenericCost, artifactCtx, myrCtx, false, false, false, null, soaCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            } else {
                if (!preCheck.canPay(manaPool, additionalGenericCost, artifactCtx, myrCtx, false, false, false, null, soaCtx)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
            }
        } else if (additionalGenericCost > 0) {
            // No base mana cost but targeting tax applies — validate player can pay the tax
            if (manaPool.getTotal() < additionalGenericCost) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
        }

        // Exile-from-graveyard cost needs at least one valid card
        ExileCardFromGraveyardCost exileGraveyardCost = abilityEffects.stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .findFirst()
                .orElse(null);
        if (exileGraveyardCost != null
                && collectGraveyardIndicesForType(gameData.playerGraveyards.get(playerId), exileGraveyardCost.requiredType(),
                        exileGraveyardCost.requiredSubtype()).isEmpty()) {
            String typeName = graveyardExileFilterLabel(exileGraveyardCost.requiredType(), exileGraveyardCost.requiredSubtype());
            throw new IllegalStateException("No " + typeName + "card in graveyard to exile");
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
            if (gy == null || gy.size() < exileNGraveyardCost.count()) {
                throw new IllegalStateException("Not enough cards in graveyard to exile (need "
                        + exileNGraveyardCost.count() + ")");
            }
        }

        // Discard cost needs enough valid cards in hand (skipped when already paid interactively)
        if (!discardCostAlreadyPaid) {
            DiscardCardTypeCost discardCardTypeCost = abilityEffects.stream()
                    .filter(DiscardCardTypeCost.class::isInstance)
                    .map(DiscardCardTypeCost.class::cast)
                    .findFirst()
                    .orElse(null);
            if (discardCardTypeCost != null
                    && collectDiscardIndices(gameData.playerHands.get(playerId), discardCardTypeCost, xValue).size()
                    < discardCardTypeCost.count()) {
                String costLabel = discardCardTypeCost.label() != null ? discardCardTypeCost.label() + " " : "";
                throw new IllegalStateException("Must discard a " + costLabel + "card to activate ability");
            }
        }

        // Random-discard cost needs at least one card in hand
        if (abilityEffects.stream().anyMatch(e -> e instanceof DiscardRandomCardCost)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                throw new IllegalStateException("Must have a card to discard at random to activate ability");
            }
        }

        // Reveal-two-color-sharing-cards cost needs a qualifying pair in hand
        if (abilityEffects.stream().anyMatch(e -> e instanceof RevealTwoCardsSharingColorCost)
                && colorSharingPair(gameData.playerHands.get(playerId)) == null) {
            throw new IllegalStateException("Must reveal two cards that share a color to activate ability");
        }

        // Remove-counter cost availability
        Optional<RemoveCounterFromSourceCost> removeCounterCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveCounterFromSourceCost)
                .map(e -> (RemoveCounterFromSourceCost) e)
                .findFirst();
        if (removeCounterCost.isPresent()) {
            int required = removeCounterCost.get().count();
            CounterType ct = removeCounterCost.get().counterType();
            int available = switch (ct) {
                case SILVER -> 0; // Silver counters are on exiled cards, not permanents
                case ANY -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
                default -> permanent.getCounterCount(ct);
            };
            if (available < required) {
                throw new IllegalStateException("Not enough counters to remove (need " + required + ", have " + available + ")");
            }
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

        // Remove-charge-counter cost availability
        Optional<RemoveChargeCountersFromSourceCost> removeChargeCost = abilityEffects.stream()
                .filter(e -> e instanceof RemoveChargeCountersFromSourceCost)
                .map(e -> (RemoveChargeCountersFromSourceCost) e)
                .findFirst();
        if (removeChargeCost.isPresent()) {
            int required = removeChargeCost.get().count();
            if (permanent.getCounterCount(CounterType.CHARGE) < required) {
                throw new IllegalStateException("Not enough charge counters (need " + required + ", have " + permanent.getCounterCount(CounterType.CHARGE) + ")");
            }
        }

        // Imprinted-copy X requirement — unless this same ability's exile cost sets the imprint
        // during payment, in which case the check runs after that cost (validateImprintedCopyXValue)
        if (exileGraveyardCost == null || !exileGraveyardCost.imprintOnSource()) {
            validateImprintedCopyXValue(gameData, permanent, abilityEffects, xValue);
        }
    }

    /**
     * Validates X for Prototype Portal-style abilities. Per ruling: "You may not activate the
     * second ability if no card has been exiled with Prototype Portal." X is defined by the exiled
     * card's mana value (not chosen freely), so no imprint = can't activate.
     */
    private void validateImprintedCopyXValue(GameData gameData, Permanent permanent, List<CardEffect> abilityEffects, int effectiveXValue) {
        CreateTokenCopyOfImprintedCardEffect imprintedCopyEffect = abilityEffects.stream()
                .filter(CreateTokenCopyOfImprintedCardEffect.class::isInstance)
                .map(CreateTokenCopyOfImprintedCardEffect.class::cast)
                .findFirst().orElse(null);
        if (imprintedCopyEffect != null && !imprintedCopyEffect.exileAtEndStep()) {
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " sacrifices " , sacTarget.getCard(), "."));
    }

    private void returnPermanentToHandAsCost(GameData gameData, Player player, Permanent target) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(target)) {
            throw new IllegalStateException("Must return a permanent you control");
        }
        permanentRemovalService.removePermanentToHand(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " returns " , target.getCard(), " to hand."));
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
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during your turn, before attackers are declared");
                }
                if (!gameData.currentStep.isBeforeAttackersDeclared()) {
                    throw new IllegalStateException("This ability can only be activated before attackers are declared");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_COMBAT) {
                if (!gameData.currentStep.isCombatPhase()) {
                    throw new IllegalStateException("This ability can only be activated during combat");
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
                            && predicateEvaluationService.matchesCardPredicate(card, ability.getRequiredGraveyardCardPredicate(), null)) {
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

    private void validateGraveyardTimingRestrictions(GameData gameData, UUID playerId, ActivatedAbility ability) {
        validateHandSizeRestrictions(gameData, playerId, ability);
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
    }

    /**
     * Validates all loyalty-ability activation rules without paying anything, returning the
     * (possibly negative) loyalty delta the activation will apply.
     */
    private int validateLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int effectiveXValue) {
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

    private void payLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability, int effectiveXValue) {
        int loyaltyCost = validateLoyaltyCost(gameData, playerId, permanent, ability, effectiveXValue);
        permanent.setCounterCount(CounterType.LOYALTY, permanent.getCounterCount(CounterType.LOYALTY) + loyaltyCost);
        permanent.setLoyaltyActivationsThisTurn(permanent.getLoyaltyActivationsThisTurn() + 1);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext) {
        payManaCost(gameData, playerId, abilityCost, effectiveXValue, artifactContext, myrContext, null, 0);
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue, boolean artifactContext, boolean myrContext, Set<CardSubtype> subtypeSpellOrAbilityContext, int additionalCost) {
        ManaCost cost = new ManaCost(abilityCost);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        boolean hasSubtypeSoa = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        boolean hasRestricted = artifactContext || myrContext || hasSubtypeSoa;

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
                if (!cost.canPay(pool, effectiveXValue + additionalCost, artifactContext, myrContext, false, false, false, null, subtypeSpellOrAbilityContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost, artifactContext, myrContext, false, false, false, null, subtypeSpellOrAbilityContext);
            } else {
                if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, effectiveXValue + additionalCost);
            }
        } else {
            if (hasRestricted) {
                if (!cost.canPay(pool, additionalCost, artifactContext, myrContext, false, false, false, null, subtypeSpellOrAbilityContext)) {
                    throw new IllegalStateException("Not enough mana to activate ability");
                }
                cost.pay(pool, additionalCost, artifactContext, myrContext, false, false, false, null, subtypeSpellOrAbilityContext);
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
            int currentLife = gameData.getLife(playerId);
            gameData.playerLifeTotals.put(playerId, currentLife - phyrexianLifeCost);
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " pays " + phyrexianLifeCost + " life for Phyrexian mana."));
        }
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

    private List<Integer> collectDiscardIndices(List<Card> hand, DiscardCardTypeCost cost, int xValue) {
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (cost.manaValueEqualsX() && card.getManaValue() != xValue) {
                continue;
            }
            if (cost.predicate() == null || predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), null)) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void beginDiscardCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                        UUID targetId, Zone targetZone, String costLabel, List<Integer> validDiscardIndices,
                                        int remainingDiscards) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
                targetZone,
                costLabel,
                remainingDiscards
        );
        String labelText = costLabel != null ? costLabel + " " : "";
        String prompt = remainingDiscards > 1
                ? "Choose a " + labelText + "card to discard as an activation cost ("
                + remainingDiscards + " remaining)."
                : "Choose a " + labelText + "card to discard as an activation cost.";
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.DiscardCostChoice(
                playerId, validDiscardIndices, prompt));
    }

    private void payDiscardCost(GameData gameData, Player player, DiscardCardTypeCost cost, Integer discardCardIndex, int xValue) {
        if (discardCardIndex == null) {
            throw new IllegalStateException("Must choose a card to discard");
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validDiscardIndices = collectDiscardIndices(hand, cost, xValue);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " discards " , discarded, " as an activation cost."));
        log.info("Game {} - {} discards {} as activation cost", gameData.id, player.getUsername(), discarded.getName());
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
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        String logEntry = player.getUsername() + " discards their hand (" + discarded.size()
                + " card" + (discarded.size() != 1 ? "s" : "") + ") as an activation cost.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} discards hand of {} cards as activation cost", gameData.id, player.getUsername(), discarded.size());
    }

    private void payRandomDiscardCost(GameData gameData, Player player) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        Card discarded = hand.remove(ThreadLocalRandom.current().nextInt(hand.size()));
        graveyardService.addCardToGraveyard(gameData, playerId, discarded);
        gameData.discardCausedByOpponent = false;
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " discards " , discarded, " at random as an activation cost."));
        log.info("Game {} - {} discards {} at random as activation cost", gameData.id, player.getUsername(), discarded.getName());
    }

    private List<Integer> collectGraveyardIndicesForType(List<Card> graveyard, CardType requiredType, CardSubtype requiredSubtype) {
        List<Integer> validIndices = new ArrayList<>();
        if (graveyard == null) {
            return validIndices;
        }
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            boolean typeMatch = requiredType == null || card.getType() == requiredType;
            boolean subtypeMatch = requiredSubtype == null || card.getSubtypes().contains(requiredSubtype);
            if (typeMatch && subtypeMatch) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    /**
     * Collects the cards in {@code graveyard} that can pay an {@link ExileNCardsFromGraveyardCost} of
     * {@code requiredType} (null = any type), excluding {@code sourceCard}. Uses {@code hasType} so an
     * artifact creature counts as an artifact card. The source is excluded so a graveyard-activated
     * ability that returns itself (Salvage Titan) never exiles the very card it means to bring back.
     */
    private List<Card> matchingGraveyardExileCandidates(List<Card> graveyard, CardType requiredType, Card sourceCard) {
        List<Card> candidates = new ArrayList<>();
        if (graveyard == null) {
            return candidates;
        }
        for (Card card : graveyard) {
            if (card == sourceCard) {
                continue;
            }
            if (requiredType == null || card.hasType(requiredType)) {
                candidates.add(card);
            }
        }
        return candidates;
    }

    private void payGraveyardExileNCost(GameData gameData, Player player, ExileNCardsFromGraveyardCost cost, Card sourceCard) {
        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> candidates = matchingGraveyardExileCandidates(graveyard, cost.requiredType(), sourceCard);
        if (candidates.size() < cost.count()) {
            throw new IllegalStateException("Not enough cards in graveyard to exile");
        }
        List<Card> toExile = new ArrayList<>(candidates.subList(0, cost.count()));
        graveyard.removeAll(toExile);
        graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
        for (Card exiled : toExile) {
            exileService.exileCard(gameData, playerId, exiled);
        }
        String typeName = graveyardExileFilterLabel(cost.requiredType(), null);
        String logEntry = player.getUsername() + " exiles " + toExile.size() + " " + typeName
                + "card" + (toExile.size() != 1 ? "s" : "") + " from graveyard as an activation cost.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} {}cards from graveyard as activation cost",
                gameData.id, player.getUsername(), toExile.size(), typeName);
    }

    private String graveyardExileFilterLabel(CardType requiredType, CardSubtype requiredSubtype) {
        if (requiredSubtype != null) {
            return requiredSubtype.getDisplayName() + " ";
        }
        if (requiredType != null) {
            return requiredType.name().toLowerCase() + " ";
        }
        return "";
    }

    private void beginGraveyardExileCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                               UUID targetId, Zone targetZone, CardType requiredType, CardSubtype requiredSubtype,
                                               List<Integer> validExileIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetId,
                targetZone,
                null
        );
        String typeName = graveyardExileFilterLabel(requiredType, requiredSubtype);
        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.GraveyardExileCostChoice(
                playerId, validExileIndices,
                "Choose a " + typeName + "card from your graveyard to exile as an activation cost."));
    }

    private void payGraveyardExileCost(GameData gameData, Player player, CardType requiredType, CardSubtype requiredSubtype,
                                       Integer exileCardIndex) {
        if (exileCardIndex == null) {
            throw new IllegalStateException("Must choose a card to exile from graveyard");
        }

        UUID playerId = player.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Integer> validExileIndices = collectGraveyardIndicesForType(graveyard, requiredType, requiredSubtype);
        Set<Integer> validSet = new HashSet<>(validExileIndices);
        if (!validSet.contains(exileCardIndex)) {
            String typeName = graveyardExileFilterLabel(requiredType, requiredSubtype);
            throw new IllegalStateException("Must exile a " + typeName + "card from your graveyard");
        }

        Card exiled = graveyard.remove((int) exileCardIndex);
        graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
        exileService.exileCard(gameData, playerId, exiled);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " exiles " , exiled, " from graveyard as an activation cost."));
        log.info("Game {} - {} exiles {} from graveyard as activation cost", gameData.id, player.getUsername(), exiled.getName());
    }

    private void clearPendingAbilityActivation(GameData gameData) {
        gameData.pendingAbilityActivation = null;
        gameData.interaction.clearAwaitingInput();
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

    private void validateNotBlockedByStaticAbilityLock(GameData gameData, Permanent permanent) {
        // Detain / Edifice of Authority: a floating lock forbids activating this permanent's
        // activated abilities (mana abilities included; triggered abilities are unaffected).
        if (gameQueryService.isLockedFromActivatingAbilities(gameData, permanent.getId())) {
            throw new IllegalStateException("Activated abilities of " + permanent.getCard().getName()
                    + " can't be activated (detained)");
        }
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, lock.predicate())) {
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
     * target, no loyalty cost, and at least one mana-producing (non-cost) effect.
     */
    public static boolean isManaAbility(ActivatedAbility ability) {
        if (ability.isNeedsTarget() || ability.isNeedsSpellTarget() || ability.getLoyaltyCost() != null) {
            return false;
        }
        List<CardEffect> effects = ability.getEffects().stream()
                .filter(e -> !(e instanceof CostEffect))
                .toList();
        return !effects.isEmpty() && effects.stream().anyMatch(e -> e instanceof ManaProducingEffect);
    }
}




