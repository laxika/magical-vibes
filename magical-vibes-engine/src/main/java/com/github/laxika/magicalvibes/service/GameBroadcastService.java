package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOpponentHandsEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.model.StackEntryView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GameBroadcastService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentViewFactory permanentViewFactory;
    private final StackEntryViewFactory stackEntryViewFactory;
    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CastingCostService castingCostService;
    private final CastingPermissionService castingPermissionService;

    public void broadcastGameState(GameData gameData) {
        // Skip expensive view computation during MCTS simulation (headless session manager discards the result)
        if (gameData.simulation) return;

        List<String> newLogEntries;
        int logSize = gameData.gameLog.size();
        if (logSize > gameData.lastBroadcastedLogSize) {
            newLogEntries = new ArrayList<>(gameData.gameLog.subList(gameData.lastBroadcastedLogSize, logSize));
        } else {
            newLogEntries = List.of();
        }
        gameData.lastBroadcastedLogSize = logSize;

        List<List<PermanentView>> battlefields = getBattlefields(gameData);
        List<StackEntryView> stack = getStackViews(gameData);
        List<List<CardView>> graveyards = getGraveyardViews(gameData);
        List<Integer> deckSizes = getDeckSizes(gameData);
        List<Integer> handSizes = getHandSizes(gameData);
        List<Integer> lifeTotals = getLifeTotals(gameData);
        List<Integer> poisonCounters = getPoisonCounters(gameData);
        UUID priorityPlayerId = gameData.interaction.isAwaitingInput() ? null : gameQueryService.getPriorityPlayerId(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<CardSubtype> playerGranted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, playerId);
            List<CardView> hand = gameData.playerHands.getOrDefault(playerId, List.of())
                    .stream().map(c -> cardViewFactory.create(c, playerGranted)).toList();
            List<CardView> opponentHand = getRevealedOpponentHand(gameData, playerId);
            int mulliganCount = gameData.mulliganCounts.getOrDefault(playerId, 0);
            Map<String, Integer> manaPool = getManaPool(gameData, playerId);
            List<TurnStep> autoStopSteps = gameData.playerAutoStopSteps.containsKey(playerId)
                    ? new ArrayList<>(gameData.playerAutoStopSteps.get(playerId))
                    : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
            List<Integer> playableCardIndices = getPlayableCardIndices(gameData, playerId);
            List<Integer> playableGraveyardLandIndices = getPlayableGraveyardLandIndices(gameData, playerId);
            List<CardView> playableExileCards = getPlayableExileCards(gameData, playerId);
            List<Integer> playableFlashbackIndices = getPlayableFlashbackIndices(gameData, playerId);
            List<List<CardView>> revealedLibraryTopCards = getRevealedLibraryTopCards(gameData, playerId);
            List<CardView> playableLibraryTopCards = getPlayableLibraryTopCards(gameData, playerId);
            int searchTaxCost = getSearchTaxCost(gameData, playerId);

            // Mindslaver: controller sees the controlled player's hand and playable indices
            if (gameData.mindControllerPlayerId != null && playerId.equals(gameData.mindControllerPlayerId)) {
                UUID controlledId = gameData.mindControlledPlayerId;
                if (controlledId != null) {
                    List<CardSubtype> controlledGranted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, controlledId);
                    opponentHand = gameData.playerHands.getOrDefault(controlledId, List.of())
                            .stream().map(c -> cardViewFactory.create(c, controlledGranted)).toList();
                    playableCardIndices = getPlayableCardIndices(gameData, controlledId);
                    playableGraveyardLandIndices = getPlayableGraveyardLandIndices(gameData, controlledId);
                    playableExileCards = getPlayableExileCards(gameData, controlledId);
                    playableFlashbackIndices = getPlayableFlashbackIndices(gameData, controlledId);
                    playableLibraryTopCards = getPlayableLibraryTopCards(gameData, controlledId);
                }
            }

            sessionManager.sendToPlayer(playerId, new GameStateMessage(
                    gameData.status, gameData.activePlayerId, gameData.turnNumber,
                    gameData.currentStep, priorityPlayerId,
                    battlefields, stack, graveyards, deckSizes, handSizes, lifeTotals, poisonCounters,
                    hand, opponentHand, mulliganCount, manaPool, autoStopSteps, playableCardIndices,
                    playableGraveyardLandIndices, playableExileCards, newLogEntries, searchTaxCost,
                    gameData.mindControlledPlayerId, revealedLibraryTopCards, playableFlashbackIndices,
                    playableLibraryTopCards
            ));
        }
    }

    List<StackEntryView> getStackViews(GameData gameData) {
        return gameData.stack.stream().map(entry -> {
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(
                    gameData, entry.getControllerId());
            return stackEntryViewFactory.create(entry, granted);
        }).toList();
    }

    List<List<PermanentView>> getBattlefields(GameData data) {
        List<List<PermanentView>> battlefields = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) {
                battlefields.add(new ArrayList<>());
            } else {
                List<PermanentView> views = new ArrayList<>();
                for (Permanent p : bf) {
                    GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(data, p);
                    // Compute adjusted bonus P/T to account for static base P/T overrides (e.g. Deep Freeze)
                    int adjustedBonusPower = gameQueryService.getEffectivePower(p, bonus) - p.getEffectivePower();
                    int adjustedBonusToughness = gameQueryService.getEffectiveToughness(p, bonus) - p.getEffectiveToughness();
                    List<ActivatedAbility> allGrantedAbilities = new ArrayList<>(bonus.grantedActivatedAbilities());
                    allGrantedAbilities.addAll(p.getTemporaryActivatedAbilities());
                    allGrantedAbilities.addAll(p.getUntilNextTurnActivatedAbilities());
                    views.add(permanentViewFactory.create(p, adjustedBonusPower, adjustedBonusToughness, bonus.keywords(), bonus.animatedCreature(), allGrantedAbilities, bonus.grantedColors(), bonus.grantedSubtypes(), bonus.grantedCardTypes(), bonus.colorOverriding(), bonus.subtypeOverriding(), bonus.landSubtypeOverriding(), bonus.removedKeywords(), bonus.losesAllAbilities() || p.isLosesAllAbilitiesUntilEndOfTurn(), bonus.grantedSupertypes()));
                }
                battlefields.add(views);
            }
        }
        return battlefields;
    }

    List<List<CardView>> getGraveyardViews(GameData data) {
        List<List<CardView>> graveyards = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(data, pid);
            graveyards.add(gy != null ? gy.stream().map(c -> cardViewFactory.create(c, granted)).toList() : new ArrayList<>());
        }
        return graveyards;
    }

    List<Integer> getHandSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> hand = data.playerHands.get(pid);
            sizes.add(hand != null ? hand.size() : 0);
        }
        return sizes;
    }

    List<CardView> getRevealedOpponentHand(GameData gameData, UUID playerId) {
        // Mindslaver: controller always sees the controlled player's hand
        // (handled separately in broadcastGameState — overrides opponentHand for controller)

        boolean reveals = anyPlayerHasPlayWithHandsRevealed(gameData);
        if (!reveals) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) return List.of();
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof RevealOpponentHandsEffect) {
                        reveals = true;
                        break;
                    }
                }
                if (reveals) break;
            }
        }
        if (!reveals) return List.of();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(playerId)) {
                List<CardSubtype> granted = gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(gameData, opponentId);
                return gameData.playerHands.getOrDefault(opponentId, List.of())
                        .stream().map(c -> cardViewFactory.create(c, granted)).toList();
            }
        }
        return List.of();
    }

    private boolean anyPlayerHasPlayWithHandsRevealed(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlayWithHandsRevealedEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    List<List<CardView>> getRevealedLibraryTopCards(GameData data, UUID viewerId) {
        // Determine which players have their top card visible to the viewer.
        // PlayWithTopCardRevealedEffect = publicly revealed to all players.
        // LookAtTopCardOfOwnLibraryEffect / AllowCastFromTopOfLibraryEffect = private,
        //   only visible to the controller.
        Set<UUID> revealedPlayerIds = new HashSet<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlayWithTopCardRevealedEffect) {
                        // Public: visible to all
                        revealedPlayerIds.add(pid);
                    } else if (pid.equals(viewerId) &&
                            (effect instanceof LookAtTopCardOfOwnLibraryEffect
                                    || effect instanceof AllowCastFromTopOfLibraryEffect)) {
                        // Private: only visible to the controller
                        revealedPlayerIds.add(pid);
                    }
                }
                if (revealedPlayerIds.contains(pid)) break;
            }
        }

        List<List<CardView>> result = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            if (revealedPlayerIds.contains(pid)) {
                List<Card> deck = data.playerDecks.get(pid);
                if (deck != null && !deck.isEmpty()) {
                    result.add(List.of(cardViewFactory.create(deck.getFirst())));
                } else {
                    result.add(List.of());
                }
            } else {
                result.add(List.of());
            }
        }
        return result;
    }

    List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    Map<String, Integer> getManaPool(GameData data, UUID playerId) {
        if (playerId == null) {
            return new ManaPool().toMap();
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : new ManaPool().toMap();
    }

    List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            totals.add(gameData.getLife(pid));
        }
        return totals;
    }

    List<Integer> getPoisonCounters(GameData gameData) {
        List<Integer> counters = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            counters.add(gameData.playerPoisonCounters.getOrDefault(pid, 0));
        }
        return counters;
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        return getPlayableCardIndices(gameData, playerId, 0);
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId, int extraConvokeMana) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return playable;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);
        SpellPlayabilityContext ctx = buildSpellPlayabilityContext(gameData, playerId);
        for (int i = 0; i < hand.size(); i++) {
            if (isCardPlayable(gameData, playerId, hand.get(i), pool, extraConvokeMana, 0, ctx)) {
                playable.add(i);
            }
        }

        // MTG rule 601.2b: "as an additional cost, discard a card" — need another card in hand to
        // discard. The spell itself is still in hand here, so at least 2 cards are required.
        playable.removeIf(i -> hand.get(i).getEffects(EffectSlot.SPELL).stream()
                .anyMatch(DiscardCardTypeCost.class::isInstance) && hand.size() < 2);

        return playable;
    }

    /**
     * Pure single-card playability query: could {@code playerId} legally play {@code card} right
     * now if their mana pool were {@code pool}? Applies exactly the checks that decide
     * {@link #getPlayableCardIndices} membership — timing, casting permissions, spell limits,
     * affordability with every cost modifier and alternative-cost route, target availability
     * (CR 601.2c), graveyard-exile additional costs (CR 601.2b) and the legendary-sorcery rule
     * (CR 714.1) — but not the status/awaiting-input/priority gating (callers such as the AI
     * check priority themselves and may evaluate hypothetical pools). Never mutates game state.
     *
     * @param pool                  the pool to check affordability against (the AI passes a
     *                              virtual pool of producible mana)
     * @param additionalGenericCost extra generic mana required (e.g. targeting tax); 0 when unknown
     */
    public boolean isCardPlayable(GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalGenericCost) {
        return isCardPlayable(gameData, playerId, card, pool, 0, additionalGenericCost,
                buildSpellPlayabilityContext(gameData, playerId));
    }

    /** Per-player values shared by every card's playability check; computed once per hand scan. */
    private record SpellPlayabilityContext(boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty,
                                           int landsPlayed, boolean spellLimitReached, boolean cantCastDueToAttack,
                                           Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames,
                                           CastingCostService.CostModifierSnapshot costSnapshot,
                                           List<Permanent> battlefield) {
    }

    private SpellPlayabilityContext buildSpellPlayabilityContext(GameData gameData, UUID playerId) {
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        return new SpellPlayabilityContext(
                playerId.equals(gameData.activePlayerId),
                gameData.currentStep == TurnStep.PRECOMBAT_MAIN || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN,
                gameData.stack.isEmpty(),
                gameData.landsPlayedThisTurn.getOrDefault(playerId, 0),
                spellsCast >= maxSpells,
                castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId),
                castingPermissionService.getRestrictedSpellTypes(gameData, playerId),
                castingPermissionService.getForbiddenCardNames(gameData, playerId),
                castingCostService.buildCostModifierSnapshot(gameData, playerId),
                gameData.playerBattlefields.get(playerId));
    }

    private boolean isCardPlayable(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                   int extraConvokeMana, int additionalGenericCost, SpellPlayabilityContext ctx) {
        boolean landPlayable = card.hasType(CardType.LAND)
                && ctx.isActivePlayer() && ctx.isMainPhase() && ctx.landsPlayed() < 1 && ctx.stackEmpty();
        boolean spellPlayable = isPlayableAsSpell(gameData, playerId, card, pool, extraConvokeMana, additionalGenericCost, ctx);

        // The 601.2c/601.2b/714.1 filters below never apply to land plays
        if (card.hasType(CardType.LAND)) {
            return landPlayable || spellPlayable;
        }
        if (!spellPlayable) {
            return false;
        }

        // MTG rule 601.2c: a spell can't be cast unless a legal set of targets can be chosen for it.
        // Spells whose declared targets are all optional ("up to one/N target …") can always be
        // cast by choosing zero targets, even if no legal target exists (e.g. Stress Dream).
        boolean allTargetsOptional = !card.getSpellTargets().isEmpty() && card.getMinTargets() == 0;
        if (!allTargetsOptional
                && EffectResolution.needsSpellCastTarget(card)
                && !validTargetService.hasValidTargetsForSpell(gameData, card, playerId)) {
            return false;
        }

        // MTG rule 601.2b: can't cast if additional cost requiring N graveyard cards can't be paid
        ExileNCardsFromGraveyardCost exileCost = (ExileNCardsFromGraveyardCost) card.getEffects(EffectSlot.SPELL).stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .findFirst().orElse(null);
        if (exileCost != null) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            long matchingCount = graveyard.stream()
                    .filter(c -> exileCost.requiredType() == null || c.hasType(exileCost.requiredType()))
                    .count();
            if (matchingCount < exileCost.count()) {
                return false;
            }
        }

        // MTG rule 714.1: can't cast a legendary sorcery unless you control a legendary creature or planeswalker
        if (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                && card.hasType(CardType.SORCERY)
                && !castingPermissionService.controlsLegendaryCreatureOrPlaneswalker(gameData, playerId)) {
            return false;
        }

        return true;
    }

    private boolean isPlayableAsSpell(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                      int extraConvokeMana, int additionalGenericCost, SpellPlayabilityContext ctx) {
        if (card.getManaCost() == null) {
            // Card with no mana cost but has alternate cost (e.g. some future cards)
            return castingCostService.canPayAlternateHandCast(gameData, playerId, card)
                    && castingPermissionService.canCastWithTiming(gameData, playerId, card,
                            ctx.isActivePlayer(), ctx.isMainPhase(), ctx.stackEmpty())
                    && !ctx.spellLimitReached() && !ctx.cantCastDueToAttack();
        }
        if (ctx.spellLimitReached() || ctx.cantCastDueToAttack()) {
            return false;
        }
        if (castingPermissionService.isSpellRestricted(card, ctx.restrictedSpellTypes(), ctx.forbiddenCardNames())) {
            return false;
        }
        if (!castingPermissionService.canCastWithTiming(gameData, playerId, card,
                ctx.isActivePlayer(), ctx.isMainPhase(), ctx.stackEmpty())) {
            return false;
        }

        // Alternative zero cost (e.g. Rooftop Storm for Zombie creature spells)
        if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
            return true;
        }

        ManaCost cost = card.getParsedManaCost();
        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card, ctx.costSnapshot())
                + additionalGenericCost;
        boolean isArtifact = card.hasType(CardType.ARTIFACT);
        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
        boolean hasRestrictedRedContext = isArtifact
                || card.hasType(CardType.CREATURE);
        boolean hasKicker = false;
        for (CardEffect e : card.getEffects(EffectSlot.STATIC)) {
            if (e instanceof KickerEffect) { hasKicker = true; break; }
        }
        boolean kickedOnlyGreen = hasKicker && pool.getKickedOnlyGreen() > 0;
        boolean instantSorceryOnlyColorless = (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && (pool.getInstantSorceryOnlyColorless() > 0 || pool.getInstantSorceryOnlyColoredTotal() > 0);
        Set<CardSubtype> subtypeCreatureContext = card.hasType(CardType.CREATURE) ? gameQueryService.getCardSubtypes(card, gameData, playerId) : Set.of();
        boolean hasRestricted = isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen || instantSorceryOnlyColorless || !subtypeCreatureContext.isEmpty();
        boolean canAfford = hasRestricted
                ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext, kickedOnlyGreen, instantSorceryOnlyColorless, subtypeCreatureContext)
                : cost.canPay(pool, additionalCost);
        if (canAfford && card.isRequiresCreatureMana()) {
            canAfford = cost.canPayCreatureOnly(pool, additionalCost);
        }
        if (canAfford) {
            return true;
        }

        if (card.getKeywords().contains(Keyword.CONVOKE)) {
            // Check if castable with convoke: mana pool + untapped creatures >= total cost
            int untappedCreatureCount = 0;
            if (ctx.battlefield() != null) {
                for (Permanent perm : ctx.battlefield()) {
                    if (gameQueryService.isCreature(gameData, perm) && !perm.isTapped()) {
                        untappedCreatureCount++;
                    }
                }
            }
            int convokeCreatures = extraConvokeMana > 0 ? extraConvokeMana : untappedCreatureCount;
            int totalAvailable = pool.getTotal() + convokeCreatures;
            if (totalAvailable >= cost.getManaValue() + additionalCost) {
                return true;
            }
        }

        // Check if castable with sacrifice-for-cost-reduction (e.g. Torgaar)
        SacrificeCreaturesForCostReductionEffect sacReduce = null;
        for (CardEffect e : card.getEffects(EffectSlot.STATIC)) {
            if (e instanceof SacrificeCreaturesForCostReductionEffect s) { sacReduce = s; break; }
        }
        if (sacReduce != null) {
            int creatureCount = 0;
            if (ctx.battlefield() != null) {
                for (Permanent perm : ctx.battlefield()) {
                    if (gameQueryService.isCreature(gameData, perm)) {
                        creatureCount++;
                    }
                }
            }
            int maxReduction = creatureCount * sacReduce.reductionPerCreature();
            if (cost.canPay(pool, additionalCost - maxReduction)) {
                return true;
            }
        }

        // Check if castable with target-subtype cost reduction (e.g. Savage Stomp, Ajani's Response, Brush Off)
        ReduceOwnCastCostIfTargetingControlledPermanentEffect targetReduce = null;
        ReduceOwnCastCostIfTargetingPermanentEffect generalTargetReduce = null;
        ReduceOwnCastCostIfTargetingStackEntryEffect stackTargetReduce = null;
        for (CardEffect e : card.getEffects(EffectSlot.STATIC)) {
            if (e instanceof ReduceOwnCastCostIfTargetingControlledPermanentEffect r) {
                targetReduce = r;
            } else if (e instanceof ReduceOwnCastCostIfTargetingPermanentEffect r) {
                generalTargetReduce = r;
            } else if (e instanceof ReduceOwnCastCostIfTargetingStackEntryEffect r) {
                stackTargetReduce = r;
            }
        }
        if (targetReduce != null && castingCostService.controlsPermanent(gameData, playerId, targetReduce.predicate())) {
            if (cost.canPay(pool, additionalCost - targetReduce.amount())) {
                return true;
            }
        } else if (generalTargetReduce != null
                && castingCostService.battlefieldHasPermanentMatching(gameData, generalTargetReduce.predicate())) {
            if (cost.canPay(pool, additionalCost - generalTargetReduce.amount())) {
                return true;
            }
        } else if (stackTargetReduce != null
                && castingCostService.stackHasMatchingSpell(gameData, stackTargetReduce.predicate())) {
            if (cost.canPay(pool, additionalCost - stackTargetReduce.amount())) {
                return true;
            }
        }

        // Check non-zero alternative cost from battlefield (e.g. Jodah)
        if (castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost)) {
            return true;
        }
        return castingCostService.canPayAlternateHandCast(gameData, playerId, card);
    }

    public List<Integer> getPlayableGraveyardLandIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        boolean canPlayAnyLandsFromGraveyard = castingPermissionService.canPlayLandsFromGraveyard(gameData, playerId);
        boolean hasAnyGraveyardLandPermission = gameData.graveyardPlayPermissions.values().stream()
                .anyMatch(permittedPlayer -> permittedPlayer.equals(playerId));
        if (!canPlayAnyLandsFromGraveyard && !hasAnyGraveyardLandPermission) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        boolean stackEmpty = gameData.stack.isEmpty();

        if (!isActivePlayer || !isMainPhase || landsPlayed >= 1 || !stackEmpty) {
            return playable;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return playable;
        }

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (card.hasType(CardType.LAND)
                    && (canPlayAnyLandsFromGraveyard
                    || castingPermissionService.hasGraveyardPlayPermission(gameData, card.getId(), playerId))) {
                playable.add(i);
            }
        }

        return playable;
    }

    public List<Integer> getPlayableFlashbackIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        // Ashes of the Abhorrent etc.: players can't cast spells from graveyards
        if (!gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.GRAVEYARD)) {
            return playable;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Optional<UUID> graveyardCastSourceId = castingPermissionService.findGraveyardCastSourcePermanentId(gameData, playerId);
        Set<CardType> typesCastFromGraveyard = graveyardCastSourceId
                .map(id -> gameData.permanentTypesCastFromGraveyardThisTurn.getOrDefault(id, Set.of()))
                .orElse(Set.of());

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (spellLimitReached || cantCastDueToAttack) {
                continue;
            }

            var flashback = card.getCastingOption(FlashbackCast.class);
            var graveyardCast = card.getCastingOption(GraveyardCast.class);
            boolean grantedFlashback = flashback.isEmpty()
                    && gameData.cardsGrantedFlashbackUntilEndOfTurn.contains(card.getId());
            boolean emblemFlashback = flashback.isEmpty() && !grantedFlashback
                    && castingPermissionService.hasEmblemGrantedFlashback(gameData, playerId, card);
            boolean grantedHavengulCast = flashback.isEmpty()
                    && !grantedFlashback
                    && !emblemFlashback
                    && card.hasType(CardType.CREATURE)
                    && castingPermissionService.hasHavengulCastPermission(gameData, card, playerId);
            boolean isGrantedGraveyardPlay = flashback.isEmpty()
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedHavengulCast
                    && castingPermissionService.hasGraveyardPlayPermission(gameData, card.getId(), playerId);
            boolean isGraveyardCast = graveyardCast.isPresent()
                    && flashback.isEmpty()
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedHavengulCast
                    && !isGrantedGraveyardPlay
                    && castingPermissionService.isGraveyardCastAvailable(gameData, playerId, graveyardCast.get());

            // Check if this card is castable via a Muldrotha-style graveyard permanent cast effect
            boolean isGrantedGraveyardCast = false;
            if (flashback.isEmpty() && !grantedFlashback && !emblemFlashback && !grantedHavengulCast
                    && !isGrantedGraveyardPlay && !isGraveyardCast
                    && graveyardCastSourceId.isPresent()) {
                // Card must be a non-land permanent type with at least one unused type slot
                isGrantedGraveyardCast = CastingPermissionService.hasUnusedPermanentTypeSlot(card, typesCastFromGraveyard);
            }

            if (flashback.isEmpty() && !grantedFlashback && !emblemFlashback && !grantedHavengulCast && !isGraveyardCast
                    && !isGrantedGraveyardCast && !isGrantedGraveyardPlay) {
                continue;
            }

            boolean isInstantSpeed = card.hasType(CardType.INSTANT);
            boolean canCastTiming = isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
            if (!canCastTiming) {
                continue;
            }

            // GraveyardCast, granted flashback, emblem flashback, granted graveyard cast, and granted
            // graveyard play use the card's mana cost
            String manaCostStr = (isGraveyardCast || grantedFlashback || emblemFlashback || grantedHavengulCast
                    || isGrantedGraveyardCast || isGrantedGraveyardPlay)
                    ? card.getManaCost()
                    : flashback.get().getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
            if (manaCostStr == null) {
                // Flashback with no mana cost — e.g. Group Project's "tap three creatures" cost.
                if (flashback.isPresent() && castingCostService.canPayFlashbackTapCost(gameData, playerId, flashback.get())) {
                    playable.add(i);
                }
                continue;
            }
            ManaCost cost = new ManaCost(manaCostStr);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card);
            // Flashback-only mana (e.g. Altar of the Lost) can be spent on any spell with flashback
            // cast from a graveyard, but not on GraveyardCast-only or Muldrotha-style non-flashback casts.
            boolean cardHasFlashback = flashback.isPresent() || grantedFlashback || emblemFlashback;
            if (cardHasFlashback) {
                if (!cost.canPayFlashback(pool, additionalCost)) {
                    continue;
                }
            } else {
                if (!cost.canPay(pool, additionalCost)) {
                    continue;
                }
            }

            // For GraveyardCast with ExileNCardsFromGraveyardCost, check that enough
            // qualifying cards exist in the graveyard (excluding the card being cast)
            if (isGraveyardCast) {
                ExileNCardsFromGraveyardCost exileNCost = card.getEffects(EffectSlot.SPELL).stream()
                        .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                        .map(ExileNCardsFromGraveyardCost.class::cast)
                        .findFirst().orElse(null);
                if (exileNCost != null) {
                    long availableCards = graveyard.stream()
                            .filter(c -> c != card)
                            .filter(c -> exileNCost.requiredType() == null || c.hasType(exileNCost.requiredType()))
                            .count();
                    if (availableCards < exileNCost.count()) {
                        continue;
                    }
                }
            }

            playable.add(i);
        }

        return playable;
    }

    List<CardView> getPlayableExileCards(GameData gameData, UUID playerId) {
        List<CardView> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttackExile = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        // Collect card IDs castable via AllowCastFromCardsExiledWithSourceEffect
        Set<UUID> castableFromExileWithSource = castingPermissionService.getCastableExiledCardIds(gameData, playerId);
        Set<UUID> anyManaTypeIds = castingPermissionService.getAnyManaTypeExiledCardIds(gameData, playerId);

        // Include player's own exiled cards plus cards from any exile zone castable via source effect
        List<Card> exiledCards = new ArrayList<>(gameData.getPlayerExiledCards(playerId));
        Set<UUID> alreadyIncluded = new HashSet<>();
        for (Card c : exiledCards) alreadyIncluded.add(c.getId());
        for (UUID cardId : castableFromExileWithSource) {
            if (!alreadyIncluded.contains(cardId)) {
                ExiledCardEntry entry = gameData.findExiledCard(cardId);
                if (entry != null) {
                    exiledCards.add(entry.card());
                    alreadyIncluded.add(cardId);
                }
            }
        }
        if (exiledCards.isEmpty()) {
            return playable;
        }

        ManaPool pool = gameData.playerManaPools.get(playerId);

        for (Card card : exiledCards) {
            UUID permittedPlayer = gameData.exilePlayPermissions.get(card.getId());
            boolean hasPermission = (permittedPlayer != null && permittedPlayer.equals(playerId))
                    || castableFromExileWithSource.contains(card.getId());
            boolean hasExileCast = card.getCastingOption(ExileCast.class).isPresent();
            if (!hasPermission && !hasExileCast) {
                continue;
            }

            if (card.hasType(CardType.LAND)) {
                if (isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                    playable.add(cardViewFactory.create(card));
                }
                continue;
            }

            if (card.getManaCost() == null || spellLimitReached || cantCastDueToAttackExile) continue;
            if (castingPermissionService.isSpellRestricted(card, restrictedSpellTypes, forbiddenCardNames)) continue;

            if (castingPermissionService.canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty)) {
                if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
                    playable.add(cardViewFactory.create(card));
                } else {
                    ManaCost cost = card.getParsedManaCost();
                    boolean canAfford;
                    if (anyManaTypeIds.contains(card.getId())) {
                        canAfford = cost.canPayAsGeneric(pool);
                    } else {
                        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card);
                        boolean isArtifact = card.hasType(CardType.ARTIFACT);
                        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
                        boolean hasRestrictedRedContext = isArtifact
                                || card.hasType(CardType.CREATURE);
                        canAfford = (isArtifact || isMyr || hasRestrictedRedContext)
                                ? cost.canPay(pool, additionalCost, isArtifact, isMyr, hasRestrictedRedContext)
                                : cost.canPay(pool, additionalCost);
                        // Check non-zero alternative cost from battlefield (e.g. Jodah)
                        if (!canAfford) {
                            canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost);
                        }
                    }
                    if (canAfford) {
                        playable.add(cardViewFactory.create(card));
                    }
                }
            }
        }

        return playable;
    }

    /**
     * Returns the top card of the player's library as a playable CardView if:
     * - the player has a permanent with AllowCastFromTopOfLibraryEffect
     * - the top card matches one of the castable types
     * - the player can afford and is allowed to cast it
     */
    List<CardView> getPlayableLibraryTopCards(GameData gameData, UUID playerId) {
        List<CardView> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return playable;
        }

        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        // Collect castable types from all AllowCastFromTopOfLibraryEffect on the player's battlefield
        Set<CardType> castableTypes = castingPermissionService.getCastableTypesFromTopOfLibrary(gameData, playerId);
        if (castableTypes.isEmpty()) {
            return playable;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            return playable;
        }

        Card topCard = deck.getFirst();

        // Check if the top card matches any castable type
        boolean matchesType = castableTypes.contains(topCard.getType())
                || topCard.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        if (!matchesType || topCard.getManaCost() == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean stackEmpty = gameData.stack.isEmpty();
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = castingPermissionService.getMaxSpellsPerTurn(gameData, playerId);
        boolean spellLimitReached = spellsCast >= maxSpells;
        boolean cantCastDueToAttack = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Set<CardType> restrictedSpellTypes = castingPermissionService.getRestrictedSpellTypes(gameData, playerId);
        Set<String> forbiddenCardNames = castingPermissionService.getForbiddenCardNames(gameData, playerId);

        if (spellLimitReached || cantCastDueToAttack) return playable;
        if (castingPermissionService.isSpellRestricted(topCard, restrictedSpellTypes, forbiddenCardNames)) return playable;

        if (!castingPermissionService.canCastWithTiming(gameData, playerId, topCard, isActivePlayer, isMainPhase, stackEmpty)) return playable;

        // Check if spell requires a legal target (MTG rule 601.2c)
        if (EffectResolution.needsSpellCastTarget(topCard) && !validTargetService.hasValidTargetsForSpell(gameData, topCard, playerId)) {
            return playable;
        }

        if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, topCard)) {
            playable.add(cardViewFactory.create(topCard));
        } else {
            ManaCost cost = topCard.getParsedManaCost();
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, topCard);
            boolean canAfford = cost.canPay(pool, additionalCost);
            if (!canAfford) {
                canAfford = castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, topCard, pool, additionalCost);
            }
            if (canAfford) {
                playable.add(cardViewFactory.create(topCard));
            }
        }

        return playable;
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        List<CardSubtype> playerGranted = playerId != null
                ? gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(data, playerId)
                : List.of();
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(c -> cardViewFactory.create(c, playerGranted)).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = getManaPool(data, playerId);
        List<TurnStep> autoStopSteps = playerId != null && data.playerAutoStopSteps.containsKey(playerId)
                ? new ArrayList<>(data.playerAutoStopSteps.get(playerId))
                : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        return new JoinGame(
                data.id,
                data.gameName,
                data.status,
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.orderedPlayerIds),
                new ArrayList<>(data.gameLog),
                data.currentStep,
                data.activePlayerId,
                data.turnNumber,
                gameQueryService.getPriorityPlayerId(data),
                hand,
                mulliganCount,
                getDeckSizes(data),
                getHandSizes(data),
                getBattlefields(data),
                manaPool,
                autoStopSteps,
                getLifeTotals(data),
                getPoisonCounters(data),
                getStackViews(data),
                getGraveyardViews(data)
        );
    }

    int getSearchTaxCost(GameData gameData, UUID playerId) {
        int unpaidCount = 0;
        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(playerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantSearchLibrariesEffect) {
                        if (paidSet == null || !paidSet.contains(perm.getId())) {
                            unpaidCount++;
                        }
                    }
                }
            }
        }
        return unpaidCount * 2;
    }

    public void logAndBroadcast(GameData gameData, String logEntry) {
        gameData.gameLog.add(logEntry);
    }

    public void revealOpponentHandToPlayer(GameData gameData, UUID controllerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        List<Card> hand = gameData.playerHands.get(opponentId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = controllerName + " looks at " + opponentName + "'s hand. It is empty.";
            logAndBroadcast(gameData, logEntry);
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            String logEntry = controllerName + " looks at " + opponentName + "'s hand: " + cardNames + ".";
            logAndBroadcast(gameData, logEntry);
        }

        List<CardView> cardViews = (hand == null ? List.<Card>of() : hand).stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new RevealHandMessage(cardViews, opponentName));
    }
}
