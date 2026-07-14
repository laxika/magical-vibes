package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;

import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shared mana management logic for AI: virtual mana pool calculation,
 * land tapping, and X-cost spell management.
 */
public class AiManaManager {

    private final GameQueryService gameQueryService;
    private final PotentialManaService potentialManaService;

    public AiManaManager(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
        this.potentialManaService = new PotentialManaService(gameQueryService);
    }

    /**
     * Callback for tapping a permanent for mana. When abilityIndex is null,
     * uses the basic tapPermanent path (for ON_TAP effects). When non-null,
     * activates the specific activated ability at that index.
     */
    @FunctionalInterface
    public interface ManaTapAction {
        void tap(int permanentIndex, Integer abilityIndex);
    }

    public VirtualManaPool buildVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        return potentialManaService.buildVirtualManaPool(gameData, aiPlayerId);
    }

    /**
     * Builds a virtual mana pool from non-creature mana sources only (lands, artifacts, etc.).
     * Used to estimate mana available after an alpha strike where all creatures will be
     * tapped from attacking. Includes mana already in the pool plus untapped non-creature
     * permanents with mana abilities.
     */
    public VirtualManaPool buildLandOnlyVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        return potentialManaService.buildLandOnlyVirtualManaPool(gameData, aiPlayerId);
    }

    /**
     * Returns true if an activated ability is a free tap-based mana ability:
     * requires tap, has no mana cost, and produces mana.
     */
    public static boolean isFreeTapManaAbility(ActivatedAbility ability) {
        return PotentialManaService.isFreeTapManaAbility(ability);
    }

    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action) {
        tapLandsForCost(gameData, aiPlayerId, manaCostStr, costModifier, action, false);
    }

    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action,
                         boolean skipChoiceSources) {
        tapLandsForCost(gameData, aiPlayerId, manaCostStr, costModifier, action, skipChoiceSources, null);
    }

    /**
     * @param excludePermanentId a permanent that must never be used as a mana source, or null —
     *                           e.g. the source of a {T}-ability whose mana cost is being paid
     *                           (tapping it for mana would make its own ability unactivatable)
     */
    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action,
                         boolean skipChoiceSources, UUID excludePermanentId) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (cost.canPay(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        // Track the initial interaction kind so we only bail when a mana ability triggers
        // a NEW input prompt (e.g. color choice), not when we're already awaiting
        // input for something else (e.g. attacker declaration during attack tax payment).
        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        if (excludePermanentId != null) {
            for (Permanent p : battlefield) {
                if (p.getId().equals(excludePermanentId)) {
                    visited.add(p);
                }
            }
        }

        while (true) {
            int index = pickBestTapIndex(gameData, aiPlayerId, battlefield, cost, currentPool,
                    skipChoiceSources, false, visited);
            if (index < 0) {
                return;
            }
            visited.add(battlefield.get(index));
            if (!tapCandidate(gameData, aiPlayerId, battlefield, index, cost, currentPool, action)) {
                continue;
            }
            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (cost.canPay(currentPool, costModifier)) {
                return;
            }
            if (interactionKind(gameData) != initialInteractionKind) {
                return;
            }
        }
    }

    void tapCreaturesForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (cost.canPayCreatureOnly(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        while (true) {
            int index = pickBestTapIndex(gameData, aiPlayerId, battlefield, cost, currentPool,
                    false, true, visited);
            if (index < 0) {
                return;
            }
            visited.add(battlefield.get(index));
            if (!tapCandidate(gameData, aiPlayerId, battlefield, index, cost, currentPool, action)) {
                continue;
            }
            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (cost.canPayCreatureOnly(currentPool, costModifier)) {
                return;
            }
            if (interactionKind(gameData) != initialInteractionKind) {
                return;
            }
        }
    }

    void tapLandsForXSpell(GameData gameData, UUID aiPlayerId, Card card, int xValue, int costModifier, ManaTapAction action) {
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (isXSpellPaid(cost, card, currentPool, xValue, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        while (true) {
            int index = pickBestTapIndex(gameData, aiPlayerId, battlefield, cost, currentPool,
                    false, false, visited);
            if (index < 0) {
                return;
            }
            visited.add(battlefield.get(index));
            if (!tapCandidate(gameData, aiPlayerId, battlefield, index, cost, currentPool, action)) {
                continue;
            }
            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (isXSpellPaid(cost, card, currentPool, xValue, costModifier)) {
                return;
            }
            if (interactionKind(gameData) != initialInteractionKind) {
                return;
            }
        }
    }

    private static boolean isXSpellPaid(ManaCost cost, Card card, ManaPool pool, int xValue, int costModifier) {
        if (card.getXColorRestriction() != null) {
            return cost.canPay(pool, xValue, card.getXColorRestriction(), costModifier);
        }
        return cost.canPay(pool, xValue + costModifier);
    }

    /**
     * Taps the permanent at {@code index} for mana. Returns {@code true} if the tap
     * action ran, {@code false} if the candidate is no longer usable (e.g. a mana
     * ability that would trigger a color choice under {@code skipChoiceSources}).
     * The method adjusts for the edge case where the tap action removes the permanent
     * from the battlefield (e.g. SacrificeSelfCost) — callers re-query the battlefield
     * list each iteration so index invalidation is not a concern here.
     */
    private boolean tapCandidate(GameData gameData, UUID aiPlayerId, List<Permanent> battlefield,
                                 int index, ManaCost cost, ManaPool currentPool, ManaTapAction action) {
        Permanent perm = battlefield.get(index);
        Card card = perm.getCard();
        if (hasOnTapManaEffects(card)) {
            action.tap(index, null);
            return true;
        }
        Integer abilityIndex = chooseBestManaAbilityIndex(card, cost, currentPool, perm, gameData, aiPlayerId);
        if (abilityIndex == null) {
            return false;
        }
        action.tap(index, abilityIndex);
        return true;
    }

    /**
     * Picks the battlefield index of the best untapped mana source to tap next,
     * prioritizing sources that produce a color still unmet by the current cost.
     * Permanents in {@code visited} are skipped (already picked in a previous iteration)
     * so the loop never retargets the same source twice in one tapping pass.
     * Returns -1 if no usable candidate remains.
     */
    private int pickBestTapIndex(GameData gameData, UUID aiPlayerId, List<Permanent> battlefield,
                                  ManaCost cost, ManaPool currentPool,
                                  boolean skipChoiceSources, boolean creaturesOnly,
                                  Set<Permanent> visited) {
        int bestIndex = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (visited.contains(perm)) {
                continue;
            }
            if (perm.isTapped()) {
                continue;
            }
            boolean isCreature = gameQueryService.isCreature(gameData, perm);
            if (creaturesOnly && !isCreature) {
                continue;
            }
            if (isCreature && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }

            Card card = perm.getCard();
            boolean hasOnTap = hasOnTapManaEffects(card);
            if (!hasOnTap) {
                if (skipChoiceSources && wouldManaAbilityTriggerChoice(card)) {
                    continue;
                }
                Integer abilityIndex = chooseBestManaAbilityIndex(card, cost, currentPool, perm, gameData, aiPlayerId);
                if (abilityIndex == null) {
                    continue;
                }
            }

            int score = scoreTapCandidate(card, cost, currentPool);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Scores a tap candidate based on how well its produced colors match the unmet
     * colored requirements of the cost. Higher scores tap earlier. Priorities:
     * <ol>
     *     <li>Candidates that can produce an unmet colored requirement rank highest.
     *         Among those, more specialized sources (fewer possible colors) win so
     *         versatile dual/pain lands are saved for later demand.</li>
     *     <li>When all colored needs are met, any source is fine for generic cost;
     *         we still prefer specialized ones to preserve flexibility.</li>
     *     <li>Candidates that cannot help an unmet color are last-resort.</li>
     * </ol>
     * Sources with side-effects (e.g. pain land damage) get a small penalty.
     */
    private int scoreTapCandidate(Card card, ManaCost cost, ManaPool currentPool) {
        Set<ManaColor> produced = getProducedColors(card);
        Map<ManaColor, Integer> coloredCosts = cost.getColoredCosts();

        boolean helpsUnmet = false;
        boolean anyUnmet = false;
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int unmet = entry.getValue() - currentPool.get(entry.getKey());
            if (unmet > 0) {
                anyUnmet = true;
                if (produced.contains(entry.getKey())) {
                    helpsUnmet = true;
                }
            }
        }

        int versatilityPenalty = Math.max(0, produced.size() - 1) * 5;
        int score;
        if (anyUnmet && helpsUnmet) {
            score = 100 - versatilityPenalty;
        } else if (anyUnmet) {
            // Unmet colored demand exists but this source can't help — save for generic/later.
            score = 10 - versatilityPenalty / 5;
        } else {
            // All colored demand met — any source works for generic. Prefer specialized.
            score = 50 - versatilityPenalty;
        }

        if (hasManaAbilityWithDamageCost(card)) {
            score -= 2;
        }
        return score;
    }

    /**
     * Returns true if any free-tap mana ability on this card has a
     * {@link DealDamageToPlayersEffect} CONTROLLER side effect (pain lands).
     */
    private static boolean hasManaAbilityWithDamageCost(Card card) {
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.CONTROLLER) {
                    return true;
                }
            }
        }
        return false;
    }

    int calculateMaxAffordableX(Card card, ManaPool pool, int costModifier) {
        ManaCost cost = new ManaCost(card.getManaCost());
        if (card.getXColorRestriction() != null) {
            return cost.calculateMaxX(pool, card.getXColorRestriction(), costModifier);
        }
        return Math.max(0, cost.calculateMaxX(pool) - costModifier);
    }

    int calculateSmartX(GameData gameData, Card card, UUID targetId, ManaPool virtualPool, int costModifier) {
        int maxX = calculateMaxAffordableX(card, virtualPool, costModifier);
        if (maxX <= 0) {
            return 0;
        }

        // For requiresManaValueEqualsX spells (e.g. Postmortem Lunge), X must match the
        // graveyard target's mana value — pick X = target's mana value if affordable.
        if (targetId != null) {
            for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
                if (effect instanceof ReturnCardFromGraveyardEffect rge && rge.requiresManaValueEqualsX()) {
                    Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
                    if (graveyardCard != null) {
                        int manaValue = graveyardCard.getManaValue();
                        return manaValue >= 1 && manaValue <= maxX ? manaValue : 0;
                    }
                    break;
                }
            }
        }

        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                int toughness = gameQueryService.getEffectiveToughness(gameData, target);
                return Math.min(toughness, maxX);
            }
        }

        return maxX;
    }

    /**
     * Adds the mana that a card would produce if it were an untapped permanent
     * on the battlefield. Used by the Hard AI to compare different land play options.
     */
    public void addCardManaToPool(Card card, ManaPool pool) {
        if (hasOnTapManaEffects(card)) {
            for (CardEffect effect : card.getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof ManaProducingEffect mp) {
                    if (mp.estimatedManaColor() != null) {
                        pool.add(mp.estimatedManaColor(),
                                potentialManaService.estimateManaAmount(mp.estimatedManaAmount(), null, null));
                    } else if (mp.estimatedWildcardMana() > 0) {
                        pool.add(ManaColor.COLORLESS, mp.estimatedWildcardMana());
                    }
                }
            }
        } else {
            potentialManaService.addActivatedManaAbilitiesToVirtualPool(card, pool, false, null, null, null);
        }
    }

    /**
     * Returns the set of mana colors that a card can produce via tap or
     * activated mana abilities. Used for color coverage tiebreaking when
     * choosing which land to play.
     */
    public Set<ManaColor> getProducedColors(Card card) {
        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        for (CardEffect effect : card.getEffects(EffectSlot.ON_TAP)) {
            addEstimatedColors(effect, colors);
        }
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (isFreeTapManaAbility(ability)) {
                for (CardEffect effect : ability.getEffects()) {
                    addEstimatedColors(effect, colors);
                }
            }
        }
        return colors;
    }

    /**
     * Adds the colors an effect contributes to a card's producible-color set per the lightweight
     * mana estimator: a fixed single color, or all five colors for a plain any-color producer.
     * Special-routing producers contribute nothing (the estimator ignores them).
     */
    private static void addEstimatedColors(CardEffect effect, Set<ManaColor> colors) {
        if (effect instanceof ManaProducingEffect mp) {
            if (mp.estimatedManaColor() != null) {
                colors.add(mp.estimatedManaColor());
            } else if (mp.estimatedCountsAllColors()) {
                Collections.addAll(colors, ManaColor.values());
            }
        }
    }

    /**
     * Builds a virtual mana pool excluding mana sources whose activated abilities
     * would trigger an interactive choice (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Used when computing affordable attackers for attack tax, to avoid activating
     * choice-triggering abilities during ATTACKER_DECLARATION.
     */
    public VirtualManaPool buildSafeVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        return potentialManaService.buildSafeVirtualManaPool(gameData, aiPlayerId);
    }

    /**
     * Returns true if the card's activated mana abilities would trigger an interactive
     * color choice prompt (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Cards with ON_TAP effects are always safe — they produce mana without choices.
     */
    static boolean wouldManaAbilityTriggerChoice(Card card) {
        return PotentialManaService.wouldManaAbilityTriggerChoice(card);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /**
     * Returns true if the card has ON_TAP mana-producing effects (basic lands, mana creatures like Llanowar Elves).
     */
    private static boolean hasOnTapManaEffects(Card card) {
        return PotentialManaService.hasOnTapManaEffects(card);
    }

    /**
     * Chooses the best activated mana ability index for a permanent, prioritizing
     * colors needed for the spell's colored costs. Returns null if no usable free-tap
     * mana ability exists (including when charge counter costs cannot be paid).
     */
    private Integer chooseBestManaAbilityIndex(Card card, ManaCost cost, ManaPool currentPool, Permanent permanent,
                                                GameData gameData, UUID playerId) {
        List<ActivatedAbility> abilities = card.getActivatedAbilities();
        Integer bestIndex = null;
        int bestScore = -1;

        for (int j = 0; j < abilities.size(); j++) {
            ActivatedAbility ability = abilities.get(j);
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            if (!PotentialManaService.canPayChargeCounterCost(ability, permanent)) {
                continue;
            }
            if (!potentialManaService.canMeetTimingRestriction(ability, gameData, playerId, permanent)) {
                continue;
            }

            int score = scoreManaAbility(ability, cost, currentPool);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = j;
            }
        }
        return bestIndex;
    }

    /**
     * Scores a mana ability based on how useful its produced color is for the current spell.
     * Higher score = more useful. Prioritizes colors needed for colored costs,
     * prefers abilities without side effects (e.g. pain land damage).
     */
    private static int scoreManaAbility(ActivatedAbility ability, ManaCost cost, ManaPool currentPool) {
        boolean hasSideEffects = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.CONTROLLER);
        Map<ManaColor, Integer> coloredCosts = cost.getColoredCosts();

        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof ManaProducingEffect mp) {
                ManaColor color = mp.estimatedManaColor();
                if (color != null) {
                    int needed = coloredCosts.getOrDefault(color, 0);
                    int have = currentPool.get(color);
                    if (needed > have) {
                        // This color is needed for a colored cost we can't yet pay
                        return hasSideEffects ? 15 : 20;
                    }
                    // Can contribute to generic costs
                    return hasSideEffects ? 1 : 5;
                }
                if (mp.estimatedCountsAllColors()) {
                    return hasSideEffects ? 1 : 5;
                }
            }
        }
        return 0;
    }

    /** The active interaction kind (record class), or {@code null} when none is active. */
    private static Class<?> interactionKind(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        return active == null ? null : active.getClass();
    }
}
