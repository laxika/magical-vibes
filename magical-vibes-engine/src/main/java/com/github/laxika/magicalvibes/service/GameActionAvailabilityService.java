package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.effect.*;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Pure rules query for actions a player can legally take from the current authoritative state.
 *
 * <p>This service constructs no networking views and performs no mutation. It is shared by
 * engine action validation, AI planning, and the human player-view projector.
 */
@Service
@RequiredArgsConstructor
public class GameActionAvailabilityService {

    private final GameQueryService gameQueryService;
    private final ValidTargetService validTargetService;
    private final CastingCostService castingCostService;
    private final CastingPermissionService castingPermissionService;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * The potential-mana model this service answers playability with. Exposed so AI planning shares
     * the one instance rather than building a second: a planner that disagrees with this service
     * about what can be tapped disagrees with the server about what can be cast.
     */
    public PotentialManaService potentialManaService() {
        return potentialManaService;
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        return getPlayableCardIndices(gameData, playerId, 0);
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId, int extraConvokeMana) {
        return gameQueryService.withQueryScope(gameData,
                () -> getPlayableCardIndices(
                        gameData, playerId, extraConvokeMana, gameData.playerManaPools.get(playerId)));
    }

    public List<Integer> getPlayableForetellIndices(GameData gameData, UUID playerId) {
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()
                || !playerId.equals(gameQueryService.getPriorityPlayerId(gameData))
                || (!playerId.equals(gameData.activePlayerId)
                && !castingCostService.canForetellDuringAnyTurn(gameData, playerId))) {
            return List.of();
        }
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool == null) return List.of();
        ManaCost actionCost = castingCostService.getForetellActionCost(gameData, playerId);
        List<Integer> playable = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (castingCostService.getForetellCost(gameData, playerId, hand.get(i)) != null
                    && actionCost.canPayForForetell(pool)) {
                playable.add(i);
            }
        }
        return playable;
    }

    /**
     * Hand indices castable right now if the player also taps their untapped mana sources
     * (MTGO-style click-to-cast). Checked against the potential pool from
     * {@link PotentialManaService#buildVirtualManaPool}, then unioned with the strictly
     * affordable indices (restricted-bucket mana such as artifact-only colorless isn't
     * carried into the virtual pool, so the strict list isn't always a subset).
     */
    public List<Integer> getPotentialPlayableCardIndices(GameData gameData, UUID playerId, List<Integer> strictIndices) {
        return gameQueryService.withQueryScope(gameData,
                () -> getPotentialPlayableCardIndicesWithinQueryScope(
                        gameData, playerId, strictIndices));
    }

    private List<Integer> getPotentialPlayableCardIndicesWithinQueryScope(
            GameData gameData, UUID playerId, List<Integer> strictIndices) {
        // Same gating as getPlayableCardIndices — skip the virtual-pool build for the
        // player who could not act anyway.
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()
                || !playerId.equals(gameQueryService.getPriorityPlayerId(gameData))) {
            return new ArrayList<>(strictIndices);
        }
        List<Integer> potential = getPlayableCardIndices(gameData, playerId, 0,
                potentialManaService.buildVirtualManaPool(gameData, playerId));
        for (Integer i : strictIndices) {
            if (!potential.contains(i)) {
                potential.add(i);
            }
        }
        potential.sort(Integer::compareTo);
        return potential;
    }

    /** Total mana the player could have available after tapping every untapped mana source. */
    public int getPotentialManaTotal(GameData gameData, UUID playerId) {
        int potential = potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal();
        ManaPool current = gameData.playerManaPools.get(playerId);
        return potential + (current == null ? 0 : current.getAbilityOnlyManaTotal());
    }

    /**
     * Ability indices per battlefield permanent whose mana cost the player could cover after
     * tapping every untapped mana source — the activated-ability counterpart of
     * {@link #getPotentialPlayableCardIndices} for the MTGO-style payment flow. Only mana
     * affordability and the source's own counter gate are checked (the client already gates tap
     * state, summoning sickness and loyalty); abilities without a mana cost are omitted, and X is
     * priced at 0 like the card list does. For a {T}-cost ability the source's own mana production
     * is excluded, since it can't be tapped both for mana and for the ability's tap cost.
     */
    public Map<UUID, List<Integer>> getPotentialPayableAbilityIndices(GameData gameData, UUID playerId) {
        // Same gating as getPotentialPlayableCardIndices — skip the virtual-pool build for the
        // player who could not act anyway.
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()
                || !playerId.equals(gameQueryService.getPriorityPlayerId(gameData))) {
            return Map.of();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return Map.of();
        }
        VirtualManaPool fullPool = potentialManaService.buildVirtualManaPool(gameData, playerId);
        fullPool.promoteAbilityOnlyMana();
        if (gameQueryService.canSpendManaAsAnyColor(gameData, playerId)) {
            fullPool = new VirtualManaPool(fullPool);
            fullPool.setAllManaSpendableAsAnyColor(true);
        }
        Map<UUID, List<Integer>> result = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (perm.isFaceDown()) {
                continue;
            }
            List<ActivatedAbility> abilities = perm.getCard().getActivatedAbilities();
            List<Integer> payable = new ArrayList<>();
            VirtualManaPool poolWithoutSource = null;
            for (int i = 0; i < abilities.size(); i++) {
                ActivatedAbility ability = abilities.get(i);
                if (castingPermissionService.isSplitSecondActive(gameData) && !ability.isManaAbility()) {
                    continue;
                }
                String abilityManaCost = effectiveAbilityManaCost(gameData, playerId, perm, ability);
                if (abilityManaCost == null
                        || !PotentialManaService.meetsRequiredSourceCounters(ability, perm)) {
                    continue;
                }
                ManaPool pool = fullPool;
                if (ability.isRequiresTap()) {
                    if (poolWithoutSource == null) {
                        poolWithoutSource = potentialManaService.buildVirtualManaPool(gameData, playerId, perm.getId());
                        poolWithoutSource.promoteAbilityOnlyMana();
                        if (gameQueryService.canSpendManaAsAnyColor(gameData, playerId)) {
                            poolWithoutSource.setAllManaSpendableAsAnyColor(true);
                        }
                    }
                    pool = poolWithoutSource;
                }
                ManaCost manaCost = new ManaCost(abilityManaCost);
                boolean artifactCtx = gameQueryService.isArtifact(perm);
                boolean myrCtx = perm.getCard().getSubtypes().contains(CardSubtype.MYR);
                boolean powerstoneCtx = pool.getPowerstoneOnlyColorless() > 0;
                Set<CardSubtype> soaCtx = new HashSet<>(perm.getCard().getSubtypes());
                soaCtx.addAll(perm.getTransientSubtypes());
                soaCtx.addAll(perm.getGrantedSubtypes());
                Set<CardSubtype> creatureSourceSoaCtx = gameQueryService.isCreature(gameData, perm)
                        ? soaCtx : Set.of();
                if (manaCost.canPay(pool, 0, artifactCtx, myrCtx, false, false, false, null,
                        soaCtx, false, artifactCtx, false, false, Set.of(), creatureSourceSoaCtx,
                        powerstoneCtx)) {
                    payable.add(i);
                }
            }
            if (!payable.isEmpty()) {
                result.put(perm.getId(), payable);
            }
        }
        return result;
    }

    private String effectiveAbilityManaCost(GameData gameData, UUID playerId, Permanent source,
                                            ActivatedAbility ability) {
        if (ability.isActivatableOnlyByEnchantedPermanentController()) {
            UUID enchantedController = source.isAttached()
                    ? gameQueryService.findPermanentController(gameData, source.getAttachedTo()) : null;
            if (!playerId.equals(enchantedController)) {
                return null;
            }
        }
        if (!ability.isManaCostOfEnchantedPermanent()) {
            return ability.getManaCost();
        }
        if (!source.isAttached()) {
            return null;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        return enchanted == null ? null : enchanted.getCard().getManaCost();
    }

    private List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId, int extraConvokeMana, ManaPool pool) {
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

        SpellPlayabilityContext ctx = buildSpellPlayabilityContext(gameData, playerId);
        for (int i = 0; i < hand.size(); i++) {
            if (isCardPlayable(gameData, playerId, hand.get(i), pool, extraConvokeMana, 0, ctx)) {
                playable.add(i);
            }
        }

        // MTG rule 601.2b: "as an additional cost, discard a card" — need another card in hand
        // (matching the cost's predicate) to discard; the spell itself can never be its own discard.
        // Discard-or-pay-mana (Lightning Axe) stays playable when the mana option is affordable.
        playable.removeIf(i -> {
            Card card = hand.get(i);
            List<Integer> discardable = castingCostService.validDiscardCostIndices(gameData, playerId, card);
            if (discardable == null || !discardable.isEmpty()) {
                return false;
            }
            return !castingCostService.canPayAdditionalSpellCosts(gameData, playerId, card);
        });

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

    public boolean isCardPlayableWithDeclaredTargets(GameData gameData, UUID playerId, Card card,
                                                     ManaPool pool, int additionalGenericCost) {
        return isCardPlayableForFace(gameData, playerId, card, pool, 0, additionalGenericCost,
                buildSpellPlayabilityContext(gameData, playerId), true);
    }

    /** Per-player values shared by every card's playability check; computed once per hand scan. */
    private record SpellPlayabilityContext(boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty,
                                           int landsPlayed, boolean cantCastDueToAttack,
                                           Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames,
                                           CastingCostService.CostModifierSnapshot costSnapshot,
                                           List<Permanent> battlefield) {
    }

    private SpellPlayabilityContext buildSpellPlayabilityContext(GameData gameData, UUID playerId) {
        return new SpellPlayabilityContext(
                playerId.equals(gameData.activePlayerId),
                gameData.currentStep == TurnStep.PRECOMBAT_MAIN || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN,
                gameData.stack.isEmpty(),
                gameData.landsPlayedThisTurn.getOrDefault(playerId, 0),
                castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId),
                castingPermissionService.getRestrictedSpellTypes(gameData, playerId),
                castingPermissionService.getForbiddenCardNames(gameData, playerId),
                castingCostService.buildCostModifierSnapshot(gameData, playerId),
                gameData.playerBattlefields.get(playerId));
    }

    private boolean isCardPlayable(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                   int extraConvokeMana, int additionalGenericCost, SpellPlayabilityContext ctx) {
        if (card.isModalDoubleFaced() && card.getBackFaceCard() != null) {
            return isCardPlayableForFace(gameData, playerId, card, pool, extraConvokeMana,
                    additionalGenericCost, ctx, false)
                    || isCardPlayableForFace(gameData, playerId, card.getBackFaceCard(), pool,
                    extraConvokeMana, additionalGenericCost, ctx, false);
        }
        return isCardPlayableForFace(gameData, playerId, card, pool, extraConvokeMana,
                additionalGenericCost, ctx, false);
    }

    private boolean isCardPlayableForFace(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                          int extraConvokeMana, int additionalGenericCost,
                                          SpellPlayabilityContext ctx, boolean targetsAlreadyDeclared) {
        if (card.getCastingOption(ForetellCast.class).isPresent()
                && pool.getForetellSpellOnlyManaTotal() > 0) {
            pool = pool instanceof VirtualManaPool virtual
                    ? new VirtualManaPool(virtual)
                    : new ManaPool(pool);
            pool.promoteForetellSpellOnlyMana();
        }

        if ((card.getCastingOption(OmenCast.class).isPresent()
                || card.getCastingOption(AdventureCast.class).isPresent()) && card.getBackFaceCard() != null
                && isCardPlayable(gameData, playerId, card.getBackFaceCard(), pool,
                extraConvokeMana, additionalGenericCost, ctx)) {
            return true;
        }
        // Sunglasses of Urza: reflect the "spend white as red" permission for affordability without
        // mutating the caller's pool. Only copy when the player actually has the permission (rare).
        if (gameQueryService.canSpendWhiteManaAsRed(gameData, playerId) && !pool.isWhiteSpendableAsRed()) {
            ManaPool flagged = new ManaPool(pool);
            flagged.setWhiteSpendableAsRed(true);
            pool = flagged;
        }
        // Celestial Dawn: same treatment for the "white as any color, everything else as colorless"
        // permission — it changes affordability in both directions, so it must be reflected here.
        if (gameQueryService.canSpendWhiteManaAsAnyColor(gameData, playerId) && !pool.isWhiteSpendableAsAnyColor()) {
            ManaPool flagged = new ManaPool(pool);
            flagged.setWhiteSpendableAsAnyColor(true);
            pool = flagged;
        }
        if (gameQueryService.canSpendWhiteManaAsAnyColorUntilEndOfTurn(gameData, playerId)
                && !pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool flagged = pool instanceof VirtualManaPool virtual
                    ? new VirtualManaPool(virtual) : new ManaPool(pool);
            flagged.setWhiteSpendableAsAnyColorWithoutRestriction(true);
            pool = flagged;
        }
        if (gameQueryService.canSpendManaAsAnyColor(gameData, playerId) && !pool.isAllManaSpendableAsAnyColor()) {
            ManaPool flagged = pool instanceof VirtualManaPool virtual
                    ? new VirtualManaPool(virtual) : new ManaPool(pool);
            flagged.setAllManaSpendableAsAnyColor(true);
            pool = flagged;
        }
        if (card.hasType(CardType.CREATURE) && pool.getCreatureSpellOrAbilityManaTotal() > 0) {
            pool = pool instanceof VirtualManaPool virtual
                    ? new VirtualManaPool(virtual)
                    : new ManaPool(pool);
            pool.promoteCreatureSpellOrAbilityMana();
        }
        if ((card.hasType(CardType.CREATURE) || card.hasType(CardType.ENCHANTMENT))
                && pool.getCreatureOrEnchantmentSpellOnlyManaTotal() > 0) {
            pool = pool instanceof VirtualManaPool virtual
                    ? new VirtualManaPool(virtual)
                    : new ManaPool(pool);
            pool.promoteCreatureOrEnchantmentSpellOnlyMana();
        }
        boolean landPlayable = card.hasType(CardType.LAND)
                && ctx.isActivePlayer() && ctx.isMainPhase()
                && ctx.landsPlayed() < gameData.getMaxLandsThisTurn(playerId) && ctx.stackEmpty()
                && !gameData.playersCantPlayLandsThisTurn.contains(playerId)
                && !castingPermissionService.isLandPlayFromHandRestricted(gameData, playerId)
                && !castingPermissionService.isLandPlayRestricted(gameData, playerId)
                && !castingPermissionService.isLandPlayForbiddenByChosenName(gameData, card);
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
        List<CardEffect> targetingSpellEffects = EffectResolution.resolveEffects(
                card.getEffects(EffectSlot.SPELL), false, null);
        boolean needsSpellCastTarget = EffectResolution.needsSpellCastTarget(
                targetingSpellEffects, card.isAura(), card.isEnchantPlayer());
        Integer maxXValue = maxAnnounceableX(card, pool);
        boolean externalXCanBeZero = maxXValue == null
                && card.hasXScaledTargets()
                && card.getEffectiveMinTargets(0) == 0;
        boolean allTargetsOptional = !card.getSpellTargets().isEmpty()
                && (card.getMinTargets() == 0
                || maxXValue != null && card.getEffectiveMinTargets(maxXValue) == 0
                || externalXCanBeZero);
        if (!targetsAlreadyDeclared && !allTargetsOptional && needsSpellCastTarget) {
            boolean hasValidTarget = validTargetService.hasValidTargetsForSpell(
                    gameData, card, playerId, maxXValue);
            if (!hasValidTarget && canAffordKickerCost(gameData, playerId, card, pool, additionalGenericCost)) {
                int kickerXValue = maxXValue != null ? maxXValue : 0;
                hasValidTarget = card.getEffectiveMinTargets(kickerXValue, true) == 0
                        || validTargetService.hasValidTargetsForSpell(
                        gameData, card, playerId, maxXValue, true);
            }
            if (!hasValidTarget) {
                return false;
            }
        }

        // MTG rule 601.2b: can't cast if additional cost requiring N graveyard cards can't be paid
        ExileNCardsFromGraveyardCost exileCost = (ExileNCardsFromGraveyardCost) card.getEffects(EffectSlot.SPELL).stream()
                .filter(ExileNCardsFromGraveyardCost.class::isInstance)
                .findFirst().orElse(null);
        if (exileCost != null) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            long matchingCount = graveyard.stream()
                    .filter(c -> (exileCost.requiredType() == null || c.hasType(exileCost.requiredType()))
                            && (exileCost.predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(c, exileCost.predicate(), null)))
                    .count();
            if (matchingCount < exileCost.count()) {
                return false;
            }
        }

        SacrificePermanentAndReturnTargetCardsFromGraveyardEffect sacrificeAndReturnEffect =
                card.getEffects(EffectSlot.SPELL).stream()
                        .filter(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::isInstance)
                        .map(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::cast)
                        .findFirst().orElse(null);
        if (sacrificeAndReturnEffect != null) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
            long matchingCount = graveyard.stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, sacrificeAndReturnEffect.returnFilter(), card.getId()))
                    .count();
            if (matchingCount < sacrificeAndReturnEffect.targetCount()) {
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

    /**
     * The largest X this player could announce for an {@code {X}} spell paid from {@code pool}. X is
     * announced in CR 601.2b, before targets are chosen in CR 601.2c, so a target filter that reads X
     * (Killing Glare's "creature with power X or less") has to be evaluated at that maximum —
     * evaluating it at X = 0 would report a castable spell as unplayable. Returns {@code null} for
     * spells with no {@code {X}} in their cost, which leaves the filter at its X = 0 default.
     *
     * <p>Cast-cost modifiers are ignored here, which can only make the pre-check more permissive;
     * cast-time validation still enforces the real X.</p>
     */
    private Integer maxAnnounceableX(Card card, ManaPool pool) {
        ManaCost cost = card.getParsedManaCost();
        return cost != null && cost.hasX() ? cost.calculateMaxX(pool) : null;
    }

    private boolean canAffordKickerCost(GameData gameData, UUID playerId, Card card,
                                        ManaPool pool, int additionalGenericCost) {
        KickerEffect kicker = card.getEffects(EffectSlot.STATIC).stream()
                .filter(KickerEffect.class::isInstance)
                .map(KickerEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (kicker == null) {
            return false;
        }
        if (kicker.hasLifeCost()
                && (!gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData)
                || gameData.getLife(playerId) < kicker.lifeCost().effectiveAmount(gameData.getLife(playerId)))) {
            return false;
        }
        if (kicker.hasSacrificeCost()
                && gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .noneMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, kicker.sacrificePredicate()))) {
            return false;
        }
        if (kicker.hasReturnCost()
                && gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .noneMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, kicker.returnPredicate()))) {
            return false;
        }
        if (!kicker.hasManaCost()) {
            return true;
        }
        if (card.getManaCost() == null) {
            return false;
        }

        String combinedManaCost = card.getManaCost() + kicker.cost();
        if (additionalGenericCost > 0) {
            combinedManaCost += "{" + additionalGenericCost + "}";
        }
        ManaCost totalCost = new ManaCost(combinedManaCost);
        int kickerXValue = totalCost.hasX() ? totalCost.calculateMaxX(pool) : 0;
        if (kicker.xUsesEachColorAtMostOnce() && kicker.hasXColorRestriction() && totalCost.hasX()) {
            int maxByColor = totalCost.calculateMaxX(pool, kicker.xColorRestrictions(), 0);
            int maxDistinct = (int) kicker.xColorRestrictions().stream()
                    .filter(color -> pool.get(color) > 0)
                    .count();
            kickerXValue = Math.min(maxByColor, maxDistinct);
        }
        boolean isArtifact = card.hasType(CardType.ARTIFACT);
        boolean powerstoneContext = isArtifact && pool.getPowerstoneOnlyColorless() > 0;
        boolean isMyr = gameQueryService.cardHasSubtype(card, CardSubtype.MYR, gameData, playerId);
        boolean hasRestrictedRedContext = isArtifact || card.hasType(CardType.CREATURE);
        boolean kickedOnlyGreen = pool.getKickedOnlyGreen() > 0;
        boolean instantSorceryOnlyColorless = (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && (pool.getInstantSorceryOnlyColorless() > 0 || pool.getInstantSorceryOnlyColoredTotal() > 0);
        Set<CardSubtype> subtypeCreatureContext = card.hasType(CardType.CREATURE)
                ? gameQueryService.getCardSubtypes(card, gameData, playerId) : Set.of();
        Set<CardSubtype> subtypeOrLegendaryCreatureContext = card.hasType(CardType.CREATURE)
                ? (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                || card.hasKeyword(Keyword.CHANGELING))
                ? EnumSet.allOf(CardSubtype.class) : subtypeCreatureContext
                : Set.of();
        Set<CardSubtype> subtypeSpellOrAbilityContext = new HashSet<>(
                gameQueryService.getCardSubtypes(card, gameData, playerId));
        Set<CardSubtype> subtypeSpellOnlyContext = new HashSet<>(subtypeSpellOrAbilityContext);
        if (!gameQueryService.getEffectiveCardColors(gameData, card).isEmpty()) {
            subtypeSpellOrAbilityContext.remove(CardSubtype.ELDRAZI);
        }
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext =
                new HashSet<>();
        if (card.hasType(CardType.PLANESWALKER)) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells());
        }
        if (subtypeSpellOrAbilityContext.contains(CardSubtype.ELEMENTAL)
                || (card.hasType(CardType.PLANESWALKER)
                && subtypeSpellOrAbilityContext.contains(CardSubtype.CHANDRA))) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells(
                    CardSubtype.ELEMENTAL, CardSubtype.CHANDRA));
        }
        Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext = subtypeCreatureContext;
        boolean creatureSpellOnly = card.hasType(CardType.CREATURE);
        boolean legendarySpellOnly = card.getSupertypes().contains(CardSupertype.LEGENDARY);
        boolean manaValueAtLeastFour = card.getManaValue() >= 4;
        ManaPool paymentPool = pool;
        if (!subtypeOrLegendaryCreatureContext.isEmpty()
                && pool.getSubtypeOrLegendaryCreatureManaTotal(subtypeOrLegendaryCreatureContext) > 0) {
            ManaPool promoted = new ManaPool(pool);
            for (ManaColor color : ManaColor.values()) {
                promoted.add(color, pool.getSubtypeOrLegendaryCreatureManaForColor(
                        subtypeOrLegendaryCreatureContext, color));
            }
            paymentPool = promoted;
        }
        boolean hasRestricted = isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen
                || instantSorceryOnlyColorless || creatureSpellOnly || legendarySpellOnly || manaValueAtLeastFour
                || !subtypeCreatureContext.isEmpty() || !subtypeSpellOrAbilityContext.isEmpty()
                || !subtypeSpellOnlyContext.isEmpty()
                || !subtypeOrPlaneswalkerSpellContext.isEmpty()
                || !subtypeCreatureSourceSpellOrAbilityContext.isEmpty()
                || !subtypeOrLegendaryCreatureContext.isEmpty()
                || powerstoneContext;
        return hasRestricted
                ? totalCost.canPay(paymentPool, kickerXValue, isArtifact, isMyr, hasRestrictedRedContext, kickedOnlyGreen,
                instantSorceryOnlyColorless, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnly, false, legendarySpellOnly, manaValueAtLeastFour,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext,
                subtypeSpellOnlyContext)
                : totalCost.canPay(pool, kickerXValue);
    }

    /**
     * The mana costs this card could actually be cast for. Normally just its printed cost; a modal
     * spell whose modes declare their own totals (a split card with fuse — CR 709.3, CR 702.102c)
     * adds one candidate per mode, and keeps the printed cost only when some mode declares none.
     */
    private static List<ManaCost> castableCosts(Card card) {
        ManaCost printed = card.getParsedManaCost();
        ChooseOneEffect modal = card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst().orElse(null);
        if (modal == null || modal.options().stream().allMatch(o -> o.manaCost() == null)) {
            return List.of(printed);
        }
        List<ManaCost> costs = new ArrayList<>();
        boolean anyUsesPrintedCost = false;
        for (ChooseOneEffect.ChooseOneOption option : modal.options()) {
            if (option.manaCost() == null) {
                anyUsesPrintedCost = true;
            } else {
                costs.add(new ManaCost(option.manaCost()));
            }
        }
        if (anyUsesPrintedCost) {
            costs.add(printed);
        }
        return costs;
    }

    private boolean isPlayableAsSpell(GameData gameData, UUID playerId, Card card, ManaPool pool,
                                      int extraConvokeMana, int additionalGenericCost, SpellPlayabilityContext ctx) {
        if (card.isCastOnlyFromGraveyard()) {
            return false;
        }
        if (castingPermissionService.isSpellCastingFromHandRestricted(gameData, playerId)) {
            return false;
        }
        if (castingPermissionService.isAdditionalNonPhyrexianSpellRestricted(gameData, playerId, card)) {
            return false;
        }
        if (card.getManaCost() == null) {
            // Card with no mana cost but has alternate cost (e.g. some future cards)
            return (castingCostService.canPayAlternateHandCast(gameData, playerId, card)
                    || castingCostService.canAffordWebSlingingCost(
                    gameData, playerId, card, pool, additionalGenericCost))
                    && castingPermissionService.canCastWithTiming(gameData, playerId, card,
                            ctx.isActivePlayer(), ctx.isMainPhase(), ctx.stackEmpty())
                    && !castingPermissionService.isSpellLimitReached(gameData, playerId, card)
                    && !ctx.cantCastDueToAttack();
        }
        if (castingPermissionService.isSpellLimitReached(gameData, playerId, card)
                || ctx.cantCastDueToAttack()) {
            return false;
        }
        if (castingPermissionService.isSpellRestricted(gameData, playerId, card, ctx.restrictedSpellTypes(), ctx.forbiddenCardNames())) {
            return false;
        }
        if (castingPermissionService.isNoncreatureSpellCastRestricted(gameData, playerId, card)) {
            return false;
        }
        if (castingPermissionService.isOpponentsManaValueSpellCastRestricted(gameData, playerId, card)) {
            return false;
        }
        if (castingPermissionService.isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) {
            return false;
        }
        if (!castingPermissionService.canCastWithTiming(gameData, playerId, card,
                ctx.isActivePlayer(), ctx.isMainPhase(), ctx.stackEmpty())) {
            return false;
        }
        if (!castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, card)) {
            return false;
        }
        if (!castingPermissionService.canCastWithCastCondition(gameData, playerId, card)) {
            return false;
        }

        // Alternative zero cost (e.g. Rooftop Storm for Zombie creature spells)
        if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, playerId, card)) {
            return true;
        }

        if (castingCostService.canPaySharedColorDiscardAlternativeCostFromBattlefield(gameData, playerId, card)) {
            return true;
        }

        // A split card with fuse offers several mutually exclusive costs (each half, plus the fused
        // total) — it is castable if any one of them is payable, so every candidate is tried below.
        List<ManaCost> candidateCosts = castableCosts(card);
        int additionalCost = castingCostService.getCastCostModifier(gameData, playerId, card, ctx.costSnapshot())
                + additionalGenericCost;
        if (castingCostService.hasTargetBasedCostIncrease(card)) {
            ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                    gameData, card, playerId, List.of());
            if (validTargets != null) {
                additionalCost += castingCostService.getMinimumTargetBasedCostIncrease(
                        gameData, card, validTargets.validPermanentIds());
            }
        }
        int delveReduction = castingCostService.maximumDelveReduction(
                gameData, playerId, card, 0, additionalCost);
        int effectiveAdditionalCost = additionalCost - delveReduction;
        // Vizier of the Menagerie: eligible spells can be paid with mana of any type.
        if (castingPermissionService.canSpendAnyManaTypeToCast(gameData, playerId, card)
                && candidateCosts.stream()
                .map(c -> castingCostService.applyColoredManaCostReductions(
                        gameData, playerId, card, c, ctx.costSnapshot(), false))
                .anyMatch(c -> c.canPayAsGeneric(pool, 0, effectiveAdditionalCost))) {
            return true;
        }
        boolean isArtifact = card.hasType(CardType.ARTIFACT);
        boolean powerstoneContext = isArtifact && pool.getPowerstoneOnlyColorless() > 0;
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
        Set<CardSubtype> subtypeOrLegendaryCreatureContext = card.hasType(CardType.CREATURE)
                ? (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                || card.hasKeyword(Keyword.CHANGELING))
                ? EnumSet.allOf(CardSubtype.class) : subtypeCreatureContext
                : Set.of();
        // Spell-or-ability restricted mana (e.g. Smokebraider) can pay for any spell of the matching subtype.
        Set<CardSubtype> subtypeSpellOrAbilityContext = new HashSet<>(
                gameQueryService.getCardSubtypes(card, gameData, playerId));
        Set<CardSubtype> subtypeSpellOnlyContext = new HashSet<>(subtypeSpellOrAbilityContext);
        if (!gameQueryService.getEffectiveCardColors(gameData, card).isEmpty()) {
            subtypeSpellOrAbilityContext.remove(CardSubtype.ELDRAZI);
        }
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext =
                new HashSet<>();
        if (card.hasType(CardType.PLANESWALKER)) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells());
        }
        if (subtypeSpellOrAbilityContext.contains(CardSubtype.ELEMENTAL)
                || (card.hasType(CardType.PLANESWALKER)
                && subtypeSpellOrAbilityContext.contains(CardSubtype.CHANDRA))) {
            subtypeOrPlaneswalkerSpellContext.add(new ManaRestriction.SubtypeOrPlaneswalkerSpells(
                    CardSubtype.ELEMENTAL, CardSubtype.CHANDRA));
        }
        Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext = subtypeCreatureContext;
        // Creature-spell-only mana (e.g. Ancient Ziggurat) can pay for any creature spell.
        boolean creatureSpellOnly = card.hasType(CardType.CREATURE);
        // Legendary-spell-only mana (Untaidake, the Cloud Keeper) can pay for any legendary spell.
        boolean legendarySpellOnly = card.getSupertypes().contains(CardSupertype.LEGENDARY);
        boolean manaValueAtLeastFour = card.getManaValue() >= 4;
        ManaPool paymentPool = pool;
        if (!subtypeOrLegendaryCreatureContext.isEmpty()
                && pool.getSubtypeOrLegendaryCreatureManaTotal(subtypeOrLegendaryCreatureContext) > 0) {
            ManaPool promoted = new ManaPool(pool);
            for (ManaColor color : ManaColor.values()) {
                promoted.add(color, pool.getSubtypeOrLegendaryCreatureManaForColor(
                        subtypeOrLegendaryCreatureContext, color));
            }
            paymentPool = promoted;
        }
        boolean hasRestricted = isArtifact || isMyr || hasRestrictedRedContext || kickedOnlyGreen || instantSorceryOnlyColorless || creatureSpellOnly || legendarySpellOnly || manaValueAtLeastFour
                || !subtypeCreatureContext.isEmpty() || !subtypeSpellOrAbilityContext.isEmpty()
                || !subtypeSpellOnlyContext.isEmpty()
                || !subtypeOrPlaneswalkerSpellContext.isEmpty()
                || !subtypeCreatureSourceSpellOrAbilityContext.isEmpty()
                || !subtypeOrLegendaryCreatureContext.isEmpty() || powerstoneContext;
        for (ManaCost cost : candidateCosts) {
            cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card, cost, ctx.costSnapshot(), false);
            boolean canAfford = hasRestricted
                    ? cost.canPayWithAdditionalGenericCost(paymentPool, 0, effectiveAdditionalCost,
                    isArtifact, isMyr, hasRestrictedRedContext, kickedOnlyGreen,
                instantSorceryOnlyColorless, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnly, false, legendarySpellOnly, manaValueAtLeastFour,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext)
                    : cost.canPayWithAdditionalGenericCost(paymentPool, 0, effectiveAdditionalCost);
            if (canAfford && card.isRequiresCreatureMana()) {
                canAfford = cost.canPayCreatureOnly(pool, effectiveAdditionalCost);
            }
            if (canAfford) {
                return true;
            }
        }
        ManaCost cost = card.getParsedManaCost();

        if (card.getKeywords().contains(Keyword.CONVOKE)
                || hasSpellCastingAbilityGrant(gameData, playerId, card, Keyword.CONVOKE, Zone.HAND)) {
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
            if (totalAvailable >= cost.getManaValue() + effectiveAdditionalCost) {
                return true;
            }
        }

        if (card.getKeywords().contains(Keyword.IMPROVISE)
                || hasSpellCastingAbilityGrant(gameData, playerId, card, Keyword.IMPROVISE, Zone.HAND)) {
            int untappedArtifactCount = 0;
            if (ctx.battlefield() != null) {
                for (Permanent perm : ctx.battlefield()) {
                    if (gameQueryService.isArtifact(gameData, perm) && !perm.isTapped()) {
                        untappedArtifactCount++;
                    }
                }
            }
            int improviseArtifacts = extraConvokeMana > 0 ? extraConvokeMana : untappedArtifactCount;
            List<ManaColor> contributions = Collections.nCopies(improviseArtifacts, null);
            if (cost.canPayWithConvoke(pool, effectiveAdditionalCost, contributions)) {
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
        ReduceOwnCastCostIfTargetingPermanentEffect targetReduce = null;
        GraveyardCardTargetCostReductionEffect graveyardTargetReduce = null;
        ReduceOwnCastCostIfTargetingStackEntryEffect stackTargetReduce = null;
        for (CardEffect e : card.getEffects(EffectSlot.STATIC)) {
            if (e instanceof ReduceOwnCastCostIfTargetingPermanentEffect r) {
                targetReduce = r;
            } else if (e instanceof GraveyardCardTargetCostReductionEffect r) {
                graveyardTargetReduce = r;
            } else if (e instanceof ReduceOwnCastCostIfTargetingStackEntryEffect r) {
                stackTargetReduce = r;
            }
        }
        if (targetReduce != null && (targetReduce.controlledByCaster()
                ? castingCostService.controlsPermanent(gameData, playerId, targetReduce.predicate())
                : castingCostService.battlefieldHasPermanentMatching(gameData, targetReduce.predicate()))) {
            if (cost.canPay(pool, additionalCost - targetReduce.amount())) {
                return true;
            }
        } else if (graveyardTargetReduce != null
                && hasMatchingGraveyardTarget(gameData, card, playerId, graveyardTargetReduce.predicate())) {
            if (cost.canPay(pool, additionalCost - graveyardTargetReduce.amount())) {
                return true;
            }
        } else if (stackTargetReduce != null
                && castingCostService.stackHasMatchingSpell(gameData, playerId, stackTargetReduce.predicate())) {
            if (cost.canPay(pool, additionalCost - stackTargetReduce.amount())) {
                return true;
            }
        }

        if (castingCostService.hasBattlefieldTargetBasedCastCostReduction(gameData, playerId)) {
            ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                    gameData, card, playerId, List.of());
            for (UUID targetId : validTargets.validPermanentIds()) {
                int reduction = castingCostService.computeTargetBasedCostReduction(
                        gameData, playerId, card, List.of(targetId));
                if (reduction > 0 && cost.canPay(pool, additionalCost - reduction)) {
                    return true;
                }
            }
        }

        if ((card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && castingCostService.hasPerTargetCastCostReduction(gameData, playerId, card)) {
            ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                    gameData, card, playerId, List.of());
            int maximumTargetCount = Math.min(validTargets.maxTargets(), validTargets.validPermanentIds().size());
            if (maximumTargetCount > 0) {
                List<UUID> qualifyingTargets = validTargets.validPermanentIds().subList(0, maximumTargetCount);
                int perTargetReduction = castingCostService.computeTargetBasedCostReduction(
                        gameData, playerId, card, qualifyingTargets);
                if (perTargetReduction > 0 && cost.canPay(pool, additionalCost - perTargetReduction)) {
                    return true;
                }
            }
        }

        if ((card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && castingCostService.hasEnchantedPlayerCastCostReduction(gameData, playerId)) {
            ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                    gameData, card, playerId, List.of());
            for (UUID targetPlayerId : validTargets.validPlayerIds()) {
                int reduction = castingCostService.computeTargetBasedCostReduction(
                        gameData, playerId, card, List.of(targetPlayerId));
                if (reduction > 0 && cost.canPayWithAdditionalGenericCost(
                        pool, 0, additionalCost - reduction)) {
                    return true;
                }
            }
        }

        // Check non-zero alternative cost from battlefield (e.g. Jodah)
        if (castingCostService.canAffordAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost)) {
            return true;
        }
        if (castingCostService.canAffordWebSlingingCost(gameData, playerId, card, pool, additionalCost)) {
            return true;
        }
        return castingCostService.canPayAlternateHandCast(gameData, playerId, card);
    }

    private boolean hasMatchingGraveyardTarget(GameData gameData, Card card, UUID playerId,
                                               com.github.laxika.magicalvibes.model.filter.CardPredicate predicate) {
        for (UUID targetId : validTargetService.computeValidTargetsForSpell(gameData, card, playerId, List.of())
                .validGraveyardCardIds()) {
            Card target = gameQueryService.findCardInGraveyardById(gameData, targetId);
            if (target != null && predicateEvaluationService.matchesCardPredicate(target, predicate, null)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpellCastingAbilityGrant(GameData gameData, UUID playerId, Card card, Keyword ability) {
        return hasSpellCastingAbilityGrant(gameData, playerId, card, ability, Zone.HAND);
    }

    private boolean hasSpellCastingAbilityGrant(GameData gameData, UUID playerId, Card card,
                                                Keyword ability, Zone sourceZone) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
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

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return playable;
        }

        boolean canPlayAnyLandsFromGraveyard = castingPermissionService.canPlayLandsFromGraveyard(gameData, playerId);
        boolean hasAnyGraveyardLandPermission = gameData.graveyardPlayPermissions.values().stream()
                .anyMatch(permittedPlayer -> permittedPlayer.equals(playerId));
        boolean hasMayhemLandPermission = graveyard.stream()
                .anyMatch(card -> card.hasType(CardType.LAND)
                        && card.getCastingOption(GraveyardCast.class)
                        .map(option -> castingPermissionService.isGraveyardCastAvailable(gameData, playerId, card, option))
                        .orElse(false));
        if (!canPlayAnyLandsFromGraveyard && !hasAnyGraveyardLandPermission && !hasMayhemLandPermission) {
            return playable;
        }
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        boolean stackEmpty = gameData.stack.isEmpty();

        if (!isActivePlayer || !isMainPhase || landsPlayed >= gameData.getMaxLandsThisTurn(playerId) || !stackEmpty
                || gameData.playersCantPlayLandsThisTurn.contains(playerId)
                || gameData.playersCantPlayFromGraveyardsThisTurn.contains(playerId)
                || castingPermissionService.isLandPlayRestricted(gameData, playerId)
                || castingPermissionService.isLandPlayFromGraveyardRestricted(gameData, playerId)) {
            return playable;
        }

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            boolean hasMayhemPermission = card.getCastingOption(GraveyardCast.class)
                    .map(option -> castingPermissionService.isGraveyardCastAvailable(gameData, playerId, card, option))
                    .orElse(false);
            if (card.hasType(CardType.LAND)
                    && !castingPermissionService.isLandPlayForbiddenByChosenName(gameData, card)
                    && (canPlayAnyLandsFromGraveyard
                    || castingPermissionService.hasGraveyardPlayPermission(gameData, card, playerId)
                    || hasMayhemPermission)) {
                playable.add(i);
            }
        }

        return playable;
    }

    public boolean canPlayGraveyardLand(GameData gameData, UUID playerId, Card card, UUID graveyardOwnerId) {
        if (gameData.status != GameStatus.RUNNING || gameData.interaction.isAwaitingInput()) {
            return false;
        }
        if (!playerId.equals(gameQueryService.getPriorityPlayerId(gameData))) {
            return false;
        }
        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        if (!isActivePlayer || !isMainPhase || landsPlayed >= gameData.getMaxLandsThisTurn(playerId)
                || !gameData.stack.isEmpty()
                || gameData.playersCantPlayLandsThisTurn.contains(playerId)
                || gameData.playersCantPlayFromGraveyardsThisTurn.contains(playerId)
                || castingPermissionService.isLandPlayRestricted(gameData, playerId)
                || castingPermissionService.isLandPlayFromGraveyardRestricted(gameData, playerId)
                || !card.hasType(CardType.LAND)
                || castingPermissionService.isLandPlayForbiddenByChosenName(gameData, card)) {
            return false;
        }
        boolean hasPermission = playerId.equals(graveyardOwnerId)
                ? castingPermissionService.canPlayLandsFromGraveyard(gameData, playerId)
                : false;
        return hasPermission || castingPermissionService.hasGraveyardPlayPermission(gameData, card, playerId);
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
        if (gameData.playersCantPlayFromGraveyardsThisTurn.contains(playerId)) {
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
        boolean cantCastDueToAttack = castingPermissionService.isPlayerPreventedFromCasting(gameData, playerId);
        Optional<UUID> graveyardCastSourceId = castingPermissionService.findGraveyardCastSourcePermanentId(gameData, playerId);
        Set<CardType> typesCastFromGraveyard = graveyardCastSourceId
                .map(id -> gameData.permanentTypesCastFromGraveyardThisTurn.getOrDefault(id, Set.of()))
                .orElse(Set.of());

        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (cantCastDueToAttack) {
                continue;
            }
            if (!card.hasType(CardType.LAND)
                    && !gameQueryService.canCastSpellFromZone(gameData, card, Zone.GRAVEYARD, playerId)) {
                continue;
            }

            var flashback = card.effectiveFlashbackCast();
            if (flashback.isPresent()
                    && !castingPermissionService.canUseFlashback(gameData, playerId, flashback.get())) {
                flashback = Optional.empty();
            }
            var disturb = card.getCastingOption(DisturbCast.class);
            Card castHalf = flashback.isPresent() ? card.graveyardCastHalf() : card;
            if (castingPermissionService.isSpellLimitReached(gameData, playerId, castHalf)) {
                continue;
            }
            var harmonize = card.getCastingOption(HarmonizeCast.class);
            var graveyardCast = card.getCastingOption(GraveyardCast.class);
            if (graveyardCast.isEmpty()) {
                graveyardCast = castingPermissionService.findMayhemCastOption(gameData, playerId, card);
            }
            boolean isDisturb = disturb.isPresent() && flashback.isEmpty();
            boolean grantedHarmonize = harmonize.isEmpty() && flashback.isEmpty() && !isDisturb
                    && gameData.cardsGrantedHarmonizeUntilEndOfTurn.contains(card.getId());
            boolean isHarmonize = (harmonize.isPresent() && flashback.isEmpty() && !isDisturb) || grantedHarmonize;
            boolean grantedFlashback = flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && gameData.cardsGrantedFlashbackUntilEndOfTurn.contains(card.getId());
            boolean emblemFlashback = flashback.isEmpty() && !isDisturb && !isHarmonize && !grantedFlashback
                    && castingPermissionService.hasGrantedFlashback(gameData, playerId, card);
            boolean grantedGraveyardCardCast = flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && castingPermissionService.hasGrantedGraveyardCardCastPermission(gameData, card, playerId);
            boolean isGrantedGraveyardPlay = flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && castingPermissionService.hasGraveyardPlayPermission(gameData, card, playerId);
            boolean isGraveyardCast = graveyardCast.isPresent()
                    && flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay
                    && castingPermissionService.isGraveyardCastAvailable(gameData, playerId, card, graveyardCast.get());

            // Check if this card is castable via a Muldrotha-style graveyard permanent cast effect
            boolean isGrantedGraveyardCast = false;
            if (flashback.isEmpty() && !isDisturb && !isHarmonize && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay && !isGraveyardCast
                    && graveyardCastSourceId.isPresent()) {
                // Card must be a non-land permanent type with at least one unused type slot
                isGrantedGraveyardCast = CastingPermissionService.hasUnusedPermanentTypeSlot(card, typesCastFromGraveyard);
            }

            Optional<CastingPermissionService.FilteredGraveyardPermission> filteredGraveyardPermission =
                    flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay
                    && !isGraveyardCast
                    && !isGrantedGraveyardCast
                    ? castingPermissionService.findFilteredGraveyardPermission(gameData, playerId, card)
                    : Optional.empty();
            boolean isGrantedCyclingGraveyardCast = filteredGraveyardPermission.isPresent();

            boolean isJumpStart = card.getCastingOption(JumpStartCast.class).isPresent()
                    && flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay
                    && !isGraveyardCast
                    && !isGrantedGraveyardCast
                    && !isGrantedCyclingGraveyardCast
                    && !gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty();

            // Retrace (CR 702.81): castable from the graveyard for its normal mana cost if the
            // player has a land card in hand to discard as the additional cost.
            boolean isRetrace = card.getCastingOption(Retrace.class).isPresent()
                    && flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay
                    && !isGraveyardCast
                    && !isGrantedGraveyardCast
                    && !isGrantedCyclingGraveyardCast
                    && !isJumpStart
                    && gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                            .anyMatch(c -> c.hasType(CardType.LAND));

            boolean isMayCastTopInstantOrSorcery = flashback.isEmpty()
                    && !isDisturb
                    && !isHarmonize
                    && !grantedFlashback
                    && !emblemFlashback
                    && !grantedGraveyardCardCast
                    && !isGrantedGraveyardPlay
                    && !isGraveyardCast
                    && !isGrantedGraveyardCast
                    && !isGrantedCyclingGraveyardCast
                    && !isJumpStart
                    && !isRetrace
                    && castingPermissionService.canCastTopInstantOrSorceryFromGraveyard(gameData, playerId, card);

            if (flashback.isEmpty() && !isDisturb && !isHarmonize && !grantedFlashback && !emblemFlashback && !grantedGraveyardCardCast && !isGraveyardCast
                    && !isGrantedGraveyardCast && !isGrantedGraveyardPlay && !isJumpStart && !isRetrace
                    && !isGrantedCyclingGraveyardCast && !isMayCastTopInstantOrSorcery) {
                continue;
            }

            if (!castingPermissionService.canCastWithTiming(gameData, playerId, castHalf,
                    isActivePlayer, isMainPhase, stackEmpty)) {
                continue;
            }

            // A GraveyardCast may override the normal mana cost with an alternate one paid instead
            // (e.g. Worldheart Phoenix's "by paying {W}{U}{B}{R}{G}").
            String graveyardAlternateManaCost = isGraveyardCast
                    ? graveyardCast.map(GraveyardCast::alternateManaCost).orElse(null)
                    : null;
            // GraveyardCast, granted flashback, emblem flashback, granted graveyard cast, and granted
            // graveyard play use the card's mana cost
            String manaCostStr;
            if (graveyardAlternateManaCost != null) {
                manaCostStr = graveyardAlternateManaCost;
            } else if (isDisturb) {
                manaCostStr = disturb.get().getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
            } else if (isHarmonize) {
                manaCostStr = harmonize.map(h -> h.getCost(ManaCastingCost.class)
                                .map(ManaCastingCost::manaCost).orElse(null))
                        .orElse(castHalf.getManaCost() != null ? castHalf.getManaCost() : card.getManaCost());
            } else if (isGraveyardCast || grantedFlashback || emblemFlashback || grantedGraveyardCardCast
                    || isGrantedGraveyardCast || isGrantedGraveyardPlay || isRetrace
                    || isJumpStart || isGrantedCyclingGraveyardCast || isMayCastTopInstantOrSorcery) {
                manaCostStr = castHalf.getManaCost() != null ? castHalf.getManaCost() : card.getManaCost();
            } else {
                manaCostStr = flashback.get().getCost(ManaCastingCost.class).map(ManaCastingCost::manaCost).orElse(null);
            }
            if (manaCostStr == null) {
                // Flashback with no mana cost — e.g. Group Project's "tap three creatures" cost.
                if (flashback.isPresent()
                        && castingCostService.canPayFlashbackLifeCost(gameData, playerId, flashback.get())
                        && castingCostService.canPayFlashbackPermanentCosts(
                        gameData, playerId, flashback.get())) {
                // Flashback with no mana cost — e.g. Group Project's tap cost or Dread Return's
                // sacrifice cost.
                    playable.add(i);
                }
                continue;
            }
            if (flashback.isPresent()
                    && !castingCostService.canPayFlashbackLifeCost(gameData, playerId, flashback.get())) {
                continue;
            }
            if (flashback.isPresent()
                    && (!flashback.get().getCosts(SacrificePermanentsCost.class).isEmpty()
                    || !flashback.get().getCosts(SacrificeXPermanentsCastingCost.class).isEmpty())
                    && !castingCostService.canPayFlashbackSacrificeCost(gameData, playerId, flashback.get())) {
                continue;
            }
            ManaPool pool = gameData.playerManaPools.get(playerId);
            boolean cardHasFlashback = flashback.isPresent() || grantedFlashback || emblemFlashback;
            ManaCost cost = castingCostService.applyColoredManaCostReductions(
                    gameData, playerId, card, new ManaCost(manaCostStr), cardHasFlashback);
            int additionalCost = castingCostService.getCastCostModifier(
                    gameData, playerId, card, cardHasFlashback, 0, Zone.GRAVEYARD);
            // Flashback-only mana and graveyard-only mana are exposed only for their matching
            // graveyard cast paths.
            boolean canPayMana = isDisturb
                    ? cost.canPayForDisturbFromGraveyard(pool, 0, additionalCost)
                    : cardHasFlashback
                    ? cost.canPayFlashbackFromGraveyard(pool, additionalCost)
                    : cost.canPayFromGraveyard(pool, additionalCost);
            if (isHarmonize) {
                canPayMana = canPayMana || gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .filter(permanent -> !permanent.isTapped() && gameQueryService.isCreature(gameData, permanent))
                        .mapToInt(permanent -> Math.max(0, gameQueryService.getEffectivePower(gameData, permanent)))
                        .anyMatch(power -> cost.canPayFromGraveyard(pool, 0, additionalCost - power));
            }
            if (!canPayMana) {
                continue;
            }

            if (isGrantedCyclingGraveyardCast) {
                int escapeExileCount = filteredGraveyardPermission.get().permission().additionalGraveyardExileCount();
                long availableCards = graveyard.stream().filter(c -> c != card).count();
                if (availableCards < escapeExileCount) {
                    continue;
                }
            }

            if (flashback.isPresent() && !castingCostService.canPayFlashbackPermanentCosts(
                    gameData, playerId, flashback.get())) {
                continue;
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
                            .filter(c -> (exileNCost.requiredType() == null || c.hasType(exileNCost.requiredType()))
                                    && (exileNCost.predicate() == null
                                    || predicateEvaluationService.matchesCardPredicate(c, exileNCost.predicate(), null)))
                            .count();
                    if (availableCards < exileNCost.count()) {
                        continue;
                    }
                }
            }

            // Aftermath / flashback halves may carry additional cast costs (e.g. Finish's
            // sacrifice a creature) on the cast half, not the parent split card.
            if (!castingCostService.canPayAdditionalSpellCostsFromGraveyard(gameData, playerId, castHalf)) {
                continue;
            }

            playable.add(i);
        }

        return playable;
    }
}
