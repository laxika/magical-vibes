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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
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

    private static final int MAX_PAYMENT_SEARCH_NODES = 100_000;
    private static final int ACTIVATION_COST = 100;
    private static final int PAIN_MANA_COST = 10_000;
    private static final int ATTACHED_TAP_TRIGGER_COST = 1_000_000;

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

    private record ManaActivation(UUID permanentId, Integer abilityIndex) {}

    private record ManaPaymentPlan(List<ManaActivation> activations) {
        private ManaPaymentPlan {
            activations = List.copyOf(activations);
        }
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

    public void tapSourcesForCost(GameData gameData, UUID playerId, String manaCost,
                                  int additionalGenericCost, ManaTapAction action) {
        tapLandsForCost(gameData, playerId, manaCost, additionalGenericCost, action);
    }

    /**
     * Taps mana sources for an activated ability while reserving its source permanent when
     * paying the cost. A source with a {@code {T}} cost cannot also be tapped for mana.
     */
    public void tapSourcesForAbilityCost(GameData gameData, UUID playerId, String manaCost,
                                         ManaTapAction action, UUID sourcePermanentId) {
        tapLandsForCost(gameData, playerId, manaCost, 0, action, false, sourcePermanentId);
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

        ManaPaymentPlan paymentPlan = findPaymentPlan(gameData, aiPlayerId, cost, currentPool,
                costModifier, skipChoiceSources, false, excludePermanentId);
        if (paymentPlan != null) {
            executePaymentPlan(gameData, aiPlayerId, battlefield, cost, costModifier, action, paymentPlan);
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

    private void executePaymentPlan(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                    ManaCost cost, int additionalGenericCost, ManaTapAction action,
                                    ManaPaymentPlan plan) {
        Class<?> initialInteractionKind = interactionKind(gameData);
        for (ManaActivation activation : plan.activations()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (cost.canPay(pool, additionalGenericCost)) {
                return;
            }
            int index = indexOfPermanent(battlefield, activation.permanentId());
            if (index < 0 || battlefield.get(index).isTapped()) {
                continue;
            }
            action.tap(index, activation.abilityIndex());
            if (interactionKind(gameData) != initialInteractionKind) {
                return;
            }
        }
    }

    private static int indexOfPermanent(List<Permanent> battlefield, UUID permanentId) {
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getId().equals(permanentId)) {
                return i;
            }
        }
        return -1;
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
        if (card.getXColorRestrictions() != null) {
            return cost.canPay(pool, xValue, card.getXColorRestrictions(), costModifier);
        }
        return cost.canPay(pool, xValue + costModifier);
    }

    private record ManaOption(ManaActivation activation, Map<ManaColor, Integer> output, int cost) {}

    private record ManaSourceOptions(List<ManaOption> options) {}

    private static final class PaymentSearch {
        private ManaPaymentPlan bestPlan;
        private int bestCost = Integer.MAX_VALUE;
        private int visitedNodes;
    }

    private ManaPaymentPlan findPaymentPlan(GameData gameData, UUID playerId, ManaCost cost,
                                            ManaPool currentPool, int additionalGenericCost,
                                            boolean skipChoiceSources, boolean creaturesOnly,
                                            UUID excludePermanentId) {
        List<ManaSourceOptions> sources = collectManaSourceOptions(gameData, playerId,
                skipChoiceSources, creaturesOnly, excludePermanentId);
        if (sources.isEmpty()) {
            return null;
        }

        PaymentSearch search = new PaymentSearch();
        searchPaymentPlans(sources, 0, cost, additionalGenericCost, new ManaPool(currentPool),
                new ArrayList<>(), 0, search);
        return orderPaymentPlan(gameData, playerId, cost, currentPool, search.bestPlan);
    }

    private ManaPaymentPlan orderPaymentPlan(GameData gameData, UUID playerId, ManaCost cost,
                                             ManaPool currentPool, ManaPaymentPlan plan) {
        if (plan == null || plan.activations().size() < 2) {
            return plan;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<ManaActivation> ordered = new ArrayList<>(plan.activations());
        ordered.sort((left, right) -> {
            Permanent leftPermanent = findPermanent(battlefield, left.permanentId());
            Permanent rightPermanent = findPermanent(battlefield, right.permanentId());
            int leftScore = leftPermanent == null ? Integer.MIN_VALUE
                    : scoreTapCandidate(leftPermanent.getCard(), cost, currentPool);
            int rightScore = rightPermanent == null ? Integer.MIN_VALUE
                    : scoreTapCandidate(rightPermanent.getCard(), cost, currentPool);
            return Integer.compare(rightScore, leftScore);
        });
        return new ManaPaymentPlan(ordered);
    }

    private static Permanent findPermanent(List<Permanent> battlefield, UUID permanentId) {
        for (Permanent permanent : battlefield) {
            if (permanent.getId().equals(permanentId)) {
                return permanent;
            }
        }
        return null;
    }

    private void searchPaymentPlans(List<ManaSourceOptions> sources, int sourceIndex,
                                    ManaCost cost, int additionalGenericCost, ManaPool pool,
                                    List<ManaActivation> activations, int planCost,
                                    PaymentSearch search) {
        if (++search.visitedNodes > MAX_PAYMENT_SEARCH_NODES || planCost >= search.bestCost) {
            return;
        }
        if (cost.canPay(pool, additionalGenericCost)) {
            search.bestCost = planCost;
            search.bestPlan = new ManaPaymentPlan(activations);
            return;
        }
        if (sourceIndex >= sources.size()) {
            return;
        }

        ManaSourceOptions source = sources.get(sourceIndex);
        for (ManaOption option : source.options()) {
            ManaPool nextPool = new ManaPool(pool);
            option.output().forEach(nextPool::add);
            activations.add(option.activation());
            searchPaymentPlans(sources, sourceIndex + 1, cost, additionalGenericCost,
                    nextPool, activations, planCost + option.cost(), search);
            activations.removeLast();
        }

        searchPaymentPlans(sources, sourceIndex + 1, cost, additionalGenericCost,
                pool, activations, planCost, search);
    }

    private List<ManaSourceOptions> collectManaSourceOptions(GameData gameData, UUID playerId,
                                                              boolean skipChoiceSources,
                                                              boolean creaturesOnly,
                                                              UUID excludePermanentId) {
        List<ManaSourceOptions> sources = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (Permanent permanent : battlefield) {
            if (permanent.isTapped()
                    || permanent.getId().equals(excludePermanentId)
                    || !gameQueryService.canActivateManaAbility(gameData, permanent)) {
                continue;
            }
            boolean creature = gameQueryService.isCreature(gameData, permanent);
            if (creaturesOnly && !creature) {
                continue;
            }
            if (creature && permanent.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
                continue;
            }

            List<ManaOption> options = manaOptionsForPermanent(
                    gameData, playerId, permanent, skipChoiceSources);
            if (!options.isEmpty()) {
                sources.add(new ManaSourceOptions(options));
            }
        }
        return sources;
    }

    private List<ManaOption> manaOptionsForPermanent(GameData gameData, UUID playerId,
                                                      Permanent permanent,
                                                      boolean skipChoiceSources) {
        Card card = permanent.getCard();
        int triggerCost = attachedTapTriggerCost(gameData, permanent);
        int versatilityCost = Math.max(0, getProducedColors(card).size() - 1) * 5;
        if (hasOnTapManaEffects(card)) {
            return manaOptionsForEffects(permanent.getId(), null,
                    card.getEffects(EffectSlot.ON_TAP), triggerCost, versatilityCost, false);
        }
        if (skipChoiceSources && wouldManaAbilityTriggerChoice(card)) {
            return List.of();
        }

        List<ManaOption> options = new ArrayList<>();
        List<ActivatedAbility> abilities = card.getActivatedAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            ActivatedAbility ability = abilities.get(i);
            if (!isFreeTapManaAbility(ability)
                    || !PotentialManaService.canPayChargeCounterCost(ability, permanent)
                    || !potentialManaService.canMeetTimingRestriction(
                            ability, gameData, playerId, permanent)) {
                continue;
            }
            boolean painful = ability.getEffects().stream()
                    .anyMatch(e -> e instanceof DealDamageToPlayersEffect dmg
                            && dmg.recipient() == DamageRecipient.CONTROLLER);
            options.addAll(manaOptionsForEffects(permanent.getId(), i,
                    ability.getEffects(), triggerCost, versatilityCost, painful));
        }
        return options;
    }

    private List<ManaOption> manaOptionsForEffects(UUID permanentId, Integer abilityIndex,
                                                    List<CardEffect> effects, int triggerCost,
                                                    int versatilityCost, boolean painful) {
        Map<ManaColor, Integer> fixedOutput = new EnumMap<>(ManaColor.class);
        int anyColorAmount = 0;
        for (CardEffect effect : effects) {
            if (!(effect instanceof ManaProducingEffect mana)) {
                continue;
            }
            if (mana.estimatedManaColor() != null) {
                int amount = Math.max(1, potentialManaService.estimateManaAmount(
                        mana.estimatedManaAmount(), null, null));
                fixedOutput.merge(mana.estimatedManaColor(), amount, Integer::sum);
            } else if (mana.estimatedCountsAllColors()) {
                anyColorAmount += Math.max(1, mana.estimatedWildcardMana());
            } else if (mana.estimatedWildcardMana() > 0) {
                fixedOutput.merge(ManaColor.COLORLESS, mana.estimatedWildcardMana(), Integer::sum);
            }
        }
        if (fixedOutput.isEmpty() && anyColorAmount <= 0) {
            return List.of();
        }

        int optionCost = ACTIVATION_COST + versatilityCost + triggerCost
                + (painful ? PAIN_MANA_COST : 0);
        ManaActivation activation = new ManaActivation(permanentId, abilityIndex);
        if (anyColorAmount <= 0) {
            return List.of(new ManaOption(activation, Map.copyOf(fixedOutput), optionCost));
        }

        List<ManaOption> options = new ArrayList<>(5);
        for (ManaColor color : EnumSet.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            Map<ManaColor, Integer> output = new EnumMap<>(ManaColor.class);
            output.putAll(fixedOutput);
            output.merge(color, anyColorAmount, Integer::sum);
            options.add(new ManaOption(activation, Map.copyOf(output), optionCost));
        }
        return options;
    }

    private static int attachedTapTriggerCost(GameData gameData, Permanent permanent) {
        int[] cost = {0};
        gameData.forEachPermanent((controllerId, attachment) -> {
            if (attachment.isAttached()
                    && permanent.getId().equals(attachment.getAttachedTo())
                    && !attachment.getCard()
                            .getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED).isEmpty()) {
                cost[0] = ATTACHED_TAP_TRIGGER_COST;
            }
        });
        return cost[0];
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
        if (card.getXColorRestrictions() != null) {
            return cost.calculateMaxX(pool, card.getXColorRestrictions(), costModifier);
        }
        return Math.max(0, cost.calculateMaxX(pool) - costModifier);
    }

    int calculateSmartX(GameData gameData, Card card, UUID targetId, ManaPool virtualPool, int costModifier) {
        return calculateSmartX(gameData, gameData.activePlayerId, card, targetId, virtualPool, costModifier);
    }

    int calculateSmartX(GameData gameData, UUID castingPlayerId, Card card, UUID targetId,
            ManaPool virtualPool, int costModifier) {
        int maxX = calculateMaxAffordableX(card, virtualPool, costModifier);
        maxX = clampByXValueCap(gameData, castingPlayerId, card, maxX);
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
     * Applies a card's cast-time X ceiling ("X can't be greater than …") when it is a controller
     * {@link com.github.laxika.magicalvibes.model.amount.PermanentCount}. Used by AI X selection.
     */
    int clampByXValueCap(GameData gameData, UUID playerId, Card card, int maxX) {
        if (card.getXValueCap() == null || maxX <= 0 || playerId == null) {
            return maxX;
        }
        if (!(card.getXValueCap() instanceof com.github.laxika.magicalvibes.model.amount.PermanentCount pc)
                || pc.scope() != com.github.laxika.magicalvibes.model.amount.CountScope.CONTROLLER) {
            return maxX;
        }
        int cap = 0;
        for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (matchesXCapFilterIntrinsic(p, pc.filter())) {
                cap++;
            }
        }
        return Math.min(maxX, cap);
    }

    private static boolean matchesXCapFilterIntrinsic(Permanent permanent,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        return switch (filter) {
            case com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate ignored ->
                    permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND);
            case com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate has ->
                    permanent.getCard().getSupertypes().contains(has.supertype());
            case com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate all ->
                    all.predicates().stream().allMatch(p -> matchesXCapFilterIntrinsic(permanent, p));
            default -> false;
        };
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
