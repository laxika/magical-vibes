package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
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
import java.util.LinkedHashSet;
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

    public AiManaManager(GameQueryService gameQueryService, PotentialManaService potentialManaService) {
        this.gameQueryService = gameQueryService;
        this.potentialManaService = potentialManaService;
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

    @FunctionalInterface
    private interface ManaPaymentRequirement {
        boolean isSatisfied(ManaPool pool);
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

    /**
     * Returns true if {@code ability} is a mana ability the AI could tap {@code permanent} for right
     * now — the shared gate behind both the virtual pool and every payment path, so planned mana is
     * always mana the engine will actually let us produce.
     */
    public boolean canTapForManaNow(ActivatedAbility ability, int abilityIndex, Permanent permanent,
                                    GameData gameData, UUID playerId) {
        return potentialManaService.canTapForManaNow(ability, abilityIndex, permanent, gameData, playerId);
    }

    /**
     * The permanent's abilities in the order the engine indexes them, so an {@code abilityIndex}
     * the AI plans with is the one {@code activateAbility} resolves.
     */
    public List<ActivatedAbility> activatedAbilitiesFor(GameData gameData, Permanent permanent) {
        return potentialManaService.activatedAbilitiesFor(gameData, permanent, permanent.getCard());
    }

    boolean canPayCost(GameData gameData, UUID playerId, String manaCostStr, int costModifier,
                       boolean creaturesOnly, Set<UUID> excludedPermanentIds) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(playerId);
        if (currentPool == null) {
            return false;
        }
        ManaPaymentRequirement requirement = creaturesOnly
                ? pool -> cost.canPayCreatureOnly(pool, costModifier)
                : pool -> cost.canPay(pool, costModifier);
        if (requirement.isSatisfied(currentPool)) {
            return true;
        }
        return findPaymentPlanWithRequirement(gameData, playerId, cost, currentPool,
                false, creaturesOnly, excludedPermanentIds, requirement) != null;
    }

    boolean canPayXCost(GameData gameData, UUID playerId, Card card, String manaCostStr,
                        int xValue, int costModifier, Set<UUID> excludedPermanentIds) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(playerId);
        if (currentPool == null) {
            return false;
        }
        ManaPaymentRequirement requirement = pool -> isXSpellPaid(cost, card, pool, xValue, costModifier);
        if (requirement.isSatisfied(currentPool)) {
            return true;
        }
        return findPaymentPlanWithRequirement(gameData, playerId, cost, currentPool,
                false, false, excludedPermanentIds, requirement) != null;
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
        tapSourcesForAbilityCost(
                gameData, playerId, manaCost, 0, action, sourcePermanentId);
    }

    public void tapSourcesForAbilityCost(
            GameData gameData, UUID playerId, String manaCost, int additionalGenericCost,
            ManaTapAction action, UUID sourcePermanentId) {
        if (manaCost == null) {
            if (additionalGenericCost <= 0) {
                return;
            }
            tapLandsForCost(
                    gameData, playerId, "{" + additionalGenericCost + "}", 0, action, false,
                    sourcePermanentId);
            return;
        }
        tapLandsForCost(
                gameData, playerId, manaCost, additionalGenericCost, action, false,
                sourcePermanentId);
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
        Set<UUID> excludedPermanentIds = excludePermanentId == null
                ? Set.of()
                : Set.of(excludePermanentId);
        tapLandsForCostExcluding(gameData, aiPlayerId, manaCostStr, costModifier, action,
                skipChoiceSources, excludedPermanentIds);
    }

    void tapLandsForCostExcluding(GameData gameData, UUID aiPlayerId, String manaCostStr,
                                  int costModifier, ManaTapAction action, boolean skipChoiceSources,
                                  Set<UUID> excludedPermanentIds) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);
        Set<UUID> excludedIds = excludedPermanentIds == null
                ? Set.of()
                : Set.copyOf(excludedPermanentIds);

        if (cost.canPay(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        ManaPaymentPlan paymentPlan = findPaymentPlan(gameData, aiPlayerId, cost, currentPool,
                costModifier, skipChoiceSources, false, excludedIds);
        if (paymentPlan != null) {
            executePaymentPlan(gameData, aiPlayerId, battlefield, cost, costModifier, action, paymentPlan);
            return;
        }

        // Track the initial interaction kind so we only bail when a mana ability triggers
        // a NEW input prompt (e.g. color choice), not when we're already awaiting
        // input for something else (e.g. attacker declaration during attack tax payment).
        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Permanent p : battlefield) {
            if (excludedIds.contains(p.getId())) {
                visited.add(p);
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
        tapCreaturesForCostExcluding(gameData, aiPlayerId, manaCostStr, costModifier, action, Set.of());
    }

    void tapCreaturesForCostExcluding(GameData gameData, UUID aiPlayerId, String manaCostStr,
                                      int costModifier, ManaTapAction action,
                                      Set<UUID> excludedPermanentIds) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);
        Set<UUID> excludedIds = excludedPermanentIds == null
                ? Set.of()
                : Set.copyOf(excludedPermanentIds);

        if (cost.canPayCreatureOnly(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Permanent p : battlefield) {
            if (excludedIds.contains(p.getId())) {
                visited.add(p);
            }
        }

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
        tapLandsForXSpell(gameData, aiPlayerId, card, card.getManaCost(), xValue, costModifier, action);
    }

    void tapLandsForXSpell(GameData gameData, UUID aiPlayerId, Card card, String manaCostString,
                           int xValue, int costModifier, ManaTapAction action) {
        tapLandsForXSpellExcluding(gameData, aiPlayerId, card, manaCostString, xValue,
                costModifier, action, Set.of());
    }

    void tapLandsForXSpellExcluding(GameData gameData, UUID aiPlayerId, Card card,
                                    String manaCostString, int xValue, int costModifier,
                                    ManaTapAction action, Set<UUID> excludedPermanentIds) {
        ManaCost cost = new ManaCost(manaCostString);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);
        Set<UUID> excludedIds = excludedPermanentIds == null
                ? Set.of()
                : Set.copyOf(excludedPermanentIds);

        if (isXSpellPaid(cost, card, currentPool, xValue, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        Class<?> initialInteractionKind = interactionKind(gameData);
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Permanent p : battlefield) {
            if (excludedIds.contains(p.getId())) {
                visited.add(p);
            }
        }

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
        return cost.canPayWithAdditionalGenericCost(pool, xValue, costModifier);
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
                                            Set<UUID> excludedPermanentIds) {
        return findPaymentPlanWithRequirement(gameData, playerId, cost, currentPool,
                skipChoiceSources, creaturesOnly, excludedPermanentIds,
                pool -> cost.canPay(pool, additionalGenericCost));
    }

    private ManaPaymentPlan findPaymentPlanWithRequirement(
            GameData gameData, UUID playerId, ManaCost cost, ManaPool currentPool,
            boolean skipChoiceSources, boolean creaturesOnly, Set<UUID> excludedPermanentIds,
            ManaPaymentRequirement requirement) {
        List<ManaSourceOptions> sources = collectManaSourceOptions(gameData, playerId,
                skipChoiceSources, creaturesOnly, excludedPermanentIds);
        if (sources.isEmpty()) {
            return null;
        }

        PaymentSearch search = new PaymentSearch();
        searchPaymentPlans(sources, 0, requirement, new ManaPool(currentPool),
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
            boolean leftConsumesAnotherPermanent = consumesAnotherPermanent(gameData, battlefield, left);
            boolean rightConsumesAnotherPermanent = consumesAnotherPermanent(gameData, battlefield, right);
            if (leftConsumesAnotherPermanent != rightConsumesAnotherPermanent) {
                return Boolean.compare(leftConsumesAnotherPermanent, rightConsumesAnotherPermanent);
            }
            Permanent leftPermanent = findPermanent(battlefield, left.permanentId());
            Permanent rightPermanent = findPermanent(battlefield, right.permanentId());
            int leftScore = leftPermanent == null ? Integer.MIN_VALUE
                    : scoreTapCandidate(gameData, leftPermanent, cost, currentPool);
            int rightScore = rightPermanent == null ? Integer.MIN_VALUE
                    : scoreTapCandidate(gameData, rightPermanent, cost, currentPool);
            return Integer.compare(rightScore, leftScore);
        });
        return new ManaPaymentPlan(ordered);
    }

    /**
     * A mana ability that consumes another permanent must run after that permanent has produced
     * its mana. Otherwise the cost can remove a source that is still waiting in the payment plan.
     */
    private boolean consumesAnotherPermanent(GameData gameData, List<Permanent> battlefield,
                                             ManaActivation activation) {
        if (activation.abilityIndex() == null) {
            return false;
        }
        Permanent permanent = findPermanent(battlefield, activation.permanentId());
        if (permanent == null) {
            return false;
        }
        List<ActivatedAbility> abilities = potentialManaService.activatedAbilitiesFor(
                gameData, permanent, permanent.getCard());
        int index = activation.abilityIndex();
        if (index < 0 || index >= abilities.size()) {
            return false;
        }
        return abilities.get(index).getEffects().stream()
                .filter(CostEffect.class::isInstance)
                .map(CostEffect.class::cast)
                .anyMatch(cost -> cost.consumedPermanentFilter() != null);
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
                                    ManaPaymentRequirement requirement, ManaPool pool,
                                    List<ManaActivation> activations, int planCost,
                                    PaymentSearch search) {
        if (++search.visitedNodes > MAX_PAYMENT_SEARCH_NODES || planCost >= search.bestCost) {
            return;
        }
        if (requirement.isSatisfied(pool)) {
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
            searchPaymentPlans(sources, sourceIndex + 1, requirement, nextPool, activations,
                    planCost + option.cost(), search);
            activations.removeLast();
        }

        searchPaymentPlans(sources, sourceIndex + 1, requirement, pool, activations, planCost, search);
    }

    private List<ManaSourceOptions> collectManaSourceOptions(GameData gameData, UUID playerId,
                                                              boolean skipChoiceSources,
                                                              boolean creaturesOnly,
                                                              Set<UUID> excludedPermanentIds) {
        List<ManaSourceOptions> sources = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (Permanent permanent : battlefield) {
            if (permanent.isTapped()
                    || excludedPermanentIds.contains(permanent.getId())
                    || !gameQueryService.canActivateManaAbility(gameData, permanent)) {
                continue;
            }
            boolean creature = gameQueryService.isCreature(gameData, permanent);
            if (creaturesOnly && !creature) {
                continue;
            }
            if (gameQueryService.isSummoningSickForTapCost(gameData, permanent, playerId)) {
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
        Set<ManaColor> replacementColors = effectiveLandManaColors(gameData, permanent);
        if (skipChoiceSources && replacementColors.size() > 1) {
            return List.of(); // Tapping would prompt for which replacement color to add
        }
        boolean printedTapMana = potentialManaService.hasLivePrintedTapMana(gameData, permanent);
        int triggerCost = attachedTapTriggerCost(gameData, permanent);
        List<ManaColor> overriddenLandColors = card.hasType(CardType.LAND)
                ? gameQueryService.getOverriddenLandManaColors(gameData, permanent)
                : List.of();
        int versatilityCost = Math.max(0, (replacementColors.isEmpty()
                ? getProducedColors(card).size()
                : replacementColors.size()) - 1) * 5;
        if (!overriddenLandColors.isEmpty()) {
            ManaActivation activation = new ManaActivation(permanent.getId(), null);
            int optionCost = ACTIVATION_COST + versatilityCost + triggerCost;
            List<ManaOption> options = new ArrayList<>(replacementColors.size());
            for (ManaColor color : replacementColors) {
                options.add(new ManaOption(activation, Map.of(color, 1), optionCost));
            }
            return options;
        }
        if (printedTapMana) {
            return applyLandManaReplacement(replacementColors, manaOptionsForEffects(permanent.getId(), null,
                    card.getEffects(EffectSlot.ON_TAP), triggerCost, versatilityCost, false,
                    permanent, gameData));
        }
        if (skipChoiceSources && wouldManaAbilityTriggerChoice(card)) {
            return List.of();
        }

        List<ManaOption> options = new ArrayList<>();
        List<ActivatedAbility> abilities = potentialManaService.activatedAbilitiesFor(gameData, permanent, card);
        for (int i = 0; i < abilities.size(); i++) {
            ActivatedAbility ability = abilities.get(i);
            if (!potentialManaService.canTapForManaNow(ability, i, permanent, gameData, playerId)) {
                continue;
            }
            boolean painful = ability.getEffects().stream()
                    .anyMatch(e -> e instanceof DealDamageToPlayersEffect dmg
                            && dmg.recipient() == DamageRecipient.CONTROLLER);
            options.addAll(manaOptionsForEffects(permanent.getId(), i,
                    ability.getEffects(), triggerCost, versatilityCost, painful, permanent, gameData));
        }
        return applyLandManaReplacement(replacementColors, options);
    }

    /**
     * Colors a land actually produces after type-changing effects and mana-type replacements:
     * Infernal Darkness / Ritual of Subdual's fixed color, Reality Twist / Naked Singularity's
     * per-basic-type remapping (several colors when several such effects are active — the
     * controller then chooses), or the intrinsic mana ability of a temporarily assigned basic land
     * type. Empty when the printed colors still apply.
     *
     * <p>Mirrors {@code ActivatedAbilityExecutionService} and {@code AbilityActivationService}: a
     * planner that reads only the printed mana effects would tap a Swamp expecting {B} under Naked
     * Singularity, get {W}, and have the cast rejected after the lands are already tapped.
     */
    private Set<ManaColor> effectiveLandManaColors(GameData gameData, Permanent permanent) {
        if (!permanent.getCard().hasType(CardType.LAND)) {
            return Set.of();
        }
        ManaColor fixedColor = gameQueryService.fixedLandManaColor(gameData, permanent);
        if (fixedColor != null) {
            return Set.of(fixedColor);
        }
        if (gameQueryService.basicLandManaProducesAnyColor(gameData, permanent)) {
            return new LinkedHashSet<>(ManaColor.COLORS);
        }
        Set<ManaColor> twistedColors = gameQueryService.twistedLandManaColors(gameData, permanent);
        if (!twistedColors.isEmpty()) {
            return twistedColors;
        }
        List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, permanent);
        return overriddenColors.isEmpty() ? Set.of() : new LinkedHashSet<>(overriddenColors);
    }

    /**
     * Rewrites each planned mana option into the replacement colors: only the mana's type is
     * replaced, never its amount, so every option keeps its total and its activation. One option
     * per replacement color, because the controller picks one for all mana produced.
     */
    private List<ManaOption> applyLandManaReplacement(Set<ManaColor> replacementColors, List<ManaOption> options) {
        if (replacementColors.isEmpty() || options.isEmpty()) {
            return options;
        }
        Set<ManaOption> replaced = new LinkedHashSet<>();
        for (ManaOption option : options) {
            int total = option.output().values().stream().mapToInt(Integer::intValue).sum();
            if (total <= 0) {
                continue;
            }
            for (ManaColor color : replacementColors) {
                replaced.add(new ManaOption(option.activation(), Map.of(color, total), option.cost()));
            }
        }
        return replaced.isEmpty() ? List.of() : new ArrayList<>(replaced);
    }

    /**
     * The mana-output options one activation offers, priced for the payment search. Only mana that
     * lands in the plain pool is modelled: a spend-restricted any-color producer (Cavern of Souls,
     * Ancient Ziggurat, Somberwald Sage) is worth nothing here, because the search adds its output
     * to a plain {@link ManaPool} and asks {@link ManaCost#canPay} — which cannot spend from the
     * restricted bucket the engine actually pays it into. Counting it built plans that tapped every
     * source and still left the cost unpaid, and the AI then sent a cast the engine had to refuse.
     */
    private List<ManaOption> manaOptionsForEffects(UUID permanentId, Integer abilityIndex,
                                                    List<CardEffect> effects, int triggerCost,
                                                    int versatilityCost, boolean painful,
                                                    Permanent permanent, GameData gameData) {
        Map<ManaColor, Integer> fixedOutput = new EnumMap<>(ManaColor.class);
        int anyColorAmount = 0;
        for (CardEffect effect : effects) {
            if (!(effect instanceof ManaProducingEffect mana)) {
                continue;
            }
            if (mana.estimatedManaColor() != null) {
                int amount = potentialManaService.estimateManaAmount(
                        mana.estimatedManaAmount(), permanent, gameData);
                if (amount > 0) {
                    fixedOutput.merge(mana.estimatedManaColor(), amount, Integer::sum);
                }
            } else if (mana.estimatedCountsAllColors()) {
                anyColorAmount += Math.max(1, mana.estimatedWildcardMana());
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
        if (potentialManaService.hasLivePrintedTapMana(gameData, perm)) {
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
            if (gameQueryService.isSummoningSickForTapCost(gameData, perm, aiPlayerId)) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }

            Card card = perm.getCard();
            boolean hasOnTap = potentialManaService.hasLivePrintedTapMana(gameData, perm);
            if (!hasOnTap) {
                if (skipChoiceSources && wouldManaAbilityTriggerChoice(card)) {
                    continue;
                }
                Integer abilityIndex = chooseBestManaAbilityIndex(card, cost, currentPool, perm, gameData, aiPlayerId);
                if (abilityIndex == null) {
                    continue;
                }
            }

            int score = scoreTapCandidate(gameData, perm, cost, currentPool);
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
    private int scoreTapCandidate(GameData gameData, Permanent permanent, ManaCost cost, ManaPool currentPool) {
        Card card = permanent.getCard();
        Set<ManaColor> replacementColors = effectiveLandManaColors(gameData, permanent);
        Set<ManaColor> produced = replacementColors.isEmpty() ? getProducedColors(card) : replacementColors;
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
     * controller-damage side effect (pain lands).
     */
    private static boolean hasManaAbilityWithDamageCost(Card card) {
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof DamageDealingEffect damage && damage.damagesController()) {
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
        return cost.calculateMaxX(pool, costModifier);
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
     * Spend-restricted producers add nothing, matching {@code PotentialManaService}: a land is
     * not worth playing for mana a generic cost could never spend.
     *
     * <p>Pass a {@link VirtualManaPool}: an any-color producer is offered as every color it could
     * make, and only that pool carries the over-count that keeps one tap worth one mana.
     */
    public void addCardManaToPool(Card card, ManaPool pool) {
        if (hasOnTapManaEffects(card)) {
            for (CardEffect effect : card.getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof ManaProducingEffect mp) {
                    if (mp.estimatedManaColor() != null) {
                        pool.add(mp.estimatedManaColor(),
                                potentialManaService.estimateManaAmount(mp.estimatedManaAmount(), null, null));
                    } else if (mp.estimatedCountsAllColors()) {
                        // Every color, not colorless: the point of comparing land plays is which
                        // colored costs each one unlocks, and colorless unlocks none of them.
                        PotentialManaService.addAnyColorManaToVirtualPool(
                                pool, Math.max(1, mp.estimatedWildcardMana()), false);
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
                colors.addAll(ManaColor.COLORS);
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
        List<ActivatedAbility> abilities = potentialManaService.activatedAbilitiesFor(gameData, permanent, card);
        Integer bestIndex = null;
        int bestScore = -1;

        for (int j = 0; j < abilities.size(); j++) {
            ActivatedAbility ability = abilities.get(j);
            if (!potentialManaService.canTapForManaNow(ability, j, permanent, gameData, playerId)) {
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
