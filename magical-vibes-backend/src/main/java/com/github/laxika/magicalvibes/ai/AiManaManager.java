package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

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

    private final GameQueryService gameQueryService;

    public AiManaManager(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
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
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(aiPlayerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                boolean isCreature = gameQueryService.isCreature(gameData, perm);
                if (isCreature && perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                    continue;
                }
                if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                    continue;
                }
                // Check for land type overrides (e.g. Evil Presence making a Plains into a Swamp)
                ManaColor overriddenColor = gameQueryService.getOverriddenLandManaColor(gameData, perm);
                if (overriddenColor != null) {
                    virtual.add(overriddenColor, 1);
                    if (isCreature) {
                        virtual.addCreatureMana(overriddenColor, 1);
                    }
                } else if (hasOnTapManaEffects(perm.getCard())) {
                    // Basic lands and permanents with ON_TAP mana effects
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            virtual.add(manaEffect.color(), manaEffect.amount());
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), manaEffect.amount());
                            }
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            virtual.add(ManaColor.COLORLESS, aace.amount());
                            if (isCreature) {
                                virtual.addCreatureMana(ManaColor.COLORLESS, aace.amount());
                            }
                        } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                            // AI treats this as colorless for virtual pool estimation
                            virtual.add(ManaColor.COLORLESS);
                        }
                    }
                } else {
                    // Check activated mana abilities (dual lands, pain lands, utility lands)
                    addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, isCreature, perm, gameData, aiPlayerId);
                }
            }
        }

        return virtual;
    }

    /**
     * Builds a virtual mana pool from non-creature mana sources only (lands, artifacts, etc.).
     * Used to estimate mana available after an alpha strike where all creatures will be
     * tapped from attacking. Includes mana already in the pool plus untapped non-creature
     * permanents with mana abilities.
     */
    public VirtualManaPool buildLandOnlyVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(aiPlayerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                boolean isCreature = gameQueryService.isCreature(gameData, perm);
                // Skip creatures — they will be tapped from attacking
                if (isCreature) {
                    continue;
                }
                if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                    continue;
                }
                ManaColor overriddenColor = gameQueryService.getOverriddenLandManaColor(gameData, perm);
                if (overriddenColor != null) {
                    virtual.add(overriddenColor, 1);
                } else if (hasOnTapManaEffects(perm.getCard())) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            virtual.add(manaEffect.color(), manaEffect.amount());
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            virtual.add(ManaColor.COLORLESS, aace.amount());
                        } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                            virtual.add(ManaColor.COLORLESS);
                        }
                    }
                } else {
                    addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, false, perm, gameData, aiPlayerId);
                }
            }
        }

        return virtual;
    }

    /**
     * Adds mana from activated mana abilities to the virtual pool.
     * For permanents with multiple free-tap mana abilities (e.g. dual lands, pain lands),
     * all possible colors are added but the source can only be tapped once. The total
     * and per-color inflation is recorded on the {@link VirtualManaPool} so
     * {@code canPay} sees the actual realizable mana.
     *
     * @param permanent the permanent on the battlefield (null for hypothetical card evaluation)
     */
    private void addActivatedManaAbilitiesToVirtualPool(Card card, ManaPool virtual, boolean isCreature, Permanent permanent,
                                                        GameData gameData, UUID playerId) {
        EnumMap<ManaColor, Integer> totalByColor = new EnumMap<>(ManaColor.class);
        EnumMap<ManaColor, Integer> maxPerAbilityByColor = new EnumMap<>(ManaColor.class);
        int totalAdded = 0;
        int maxAbilityTotal = 0;

        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            if (!canPayChargeCounterCost(ability, permanent)) {
                continue;
            }
            if (!canMeetTimingRestriction(ability, gameData, playerId, permanent)) {
                continue;
            }

            EnumMap<ManaColor, Integer> abilityByColor = new EnumMap<>(ManaColor.class);
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof AwardManaEffect manaEffect) {
                    abilityByColor.merge(manaEffect.color(), manaEffect.amount(), Integer::sum);
                } else if (effect instanceof AwardAnyColorManaEffect aace) {
                    abilityByColor.merge(ManaColor.COLORLESS, aace.amount(), Integer::sum);
                } else if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                    if (permanent != null) {
                        int count = permanent.getChargeCounters();
                        if (count > 0) {
                            abilityByColor.merge(ManaColor.COLORLESS, count, Integer::sum);
                        }
                    }
                }
            }

            int abilityTotal = 0;
            for (Map.Entry<ManaColor, Integer> e : abilityByColor.entrySet()) {
                ManaColor color = e.getKey();
                int amount = e.getValue();
                virtual.add(color, amount);
                if (isCreature) {
                    virtual.addCreatureMana(color, amount);
                }
                totalByColor.merge(color, amount, Integer::sum);
                maxPerAbilityByColor.merge(color, amount, Integer::max);
                abilityTotal += amount;
            }
            totalAdded += abilityTotal;
            if (abilityTotal > maxAbilityTotal) {
                maxAbilityTotal = abilityTotal;
            }
        }

        // The source can only be tapped once, but we added mana for every ability.
        // Correct the over-counting on the virtual pool:
        //   flexibleOvercount (total)     = totalAdded - maxAbilityTotal
        //   perColorOvercount[c] (each c) = sum of c across abilities - max c in any single ability
        if (virtual instanceof VirtualManaPool vmp) {
            int totalOvercount = totalAdded - maxAbilityTotal;
            if (totalOvercount > 0) {
                vmp.addFlexibleOvercount(totalOvercount);
            }
            for (Map.Entry<ManaColor, Integer> e : totalByColor.entrySet()) {
                int perColorOvercount = e.getValue() - maxPerAbilityByColor.getOrDefault(e.getKey(), 0);
                if (perColorOvercount > 0) {
                    vmp.addPerColorOvercount(e.getKey(), perColorOvercount);
                }
            }
        }
    }

    /**
     * Returns true if an activated ability is a free tap-based mana ability:
     * requires tap, has no mana cost, and produces mana.
     */
    public static boolean isFreeTapManaAbility(ActivatedAbility ability) {
        return ability.isRequiresTap()
                && ability.getManaCost() == null
                && ability.getEffects().stream().anyMatch(e -> e instanceof ManaProducingEffect);
    }

    /**
     * Returns true if the ability's timing restriction is met. If gameData or permanent is null
     * (hypothetical card evaluation), assumes the restriction can be met.
     */
    private boolean canMeetTimingRestriction(ActivatedAbility ability, GameData gameData, UUID playerId, Permanent permanent) {
        if (ability.getTimingRestriction() == null || gameData == null) {
            return true;
        }
        return switch (ability.getTimingRestriction()) {
            case METALCRAFT -> gameQueryService.isMetalcraftMet(gameData, playerId);
            case MORBID -> gameQueryService.isMorbidMet(gameData);
            case ONLY_DURING_YOUR_TURN -> playerId.equals(gameData.activePlayerId);
            case ONLY_DURING_YOUR_UPKEEP -> playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep == TurnStep.UPKEEP;
            case ONLY_WHILE_ATTACKING -> permanent != null && permanent.isAttacking();
            case ONLY_WHILE_CREATURE -> permanent != null && gameQueryService.isCreature(gameData, permanent);
            case POWER_4_OR_GREATER -> permanent != null && gameQueryService.getEffectivePower(gameData, permanent) >= 4;
            case RAID -> gameData.playersDeclaredAttackersThisTurn.contains(playerId);
            case SORCERY_SPEED -> playerId.equals(gameData.activePlayerId)
                    && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                    && gameData.stack.isEmpty();
        };
    }

    /**
     * Returns true if the permanent can pay any charge counter costs required by the ability.
     * If permanent is null (hypothetical card evaluation), assumes costs can be paid.
     */
    private static boolean canPayChargeCounterCost(ActivatedAbility ability, Permanent permanent) {
        if (permanent == null) {
            return true;
        }
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof RemoveChargeCountersFromSourceCost cost) {
                if (permanent.getChargeCounters() < cost.count()) {
                    return false;
                }
            }
        }
        return true;
    }

    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action) {
        tapLandsForCost(gameData, aiPlayerId, manaCostStr, costModifier, action, false);
    }

    void tapLandsForCost(GameData gameData, UUID aiPlayerId, String manaCostStr, int costModifier, ManaTapAction action,
                         boolean skipChoiceSources) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (cost.canPay(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        // Track initial awaiting input so we only bail when a mana ability triggers
        // a NEW input prompt (e.g. color choice), not when we're already awaiting
        // input for something else (e.g. ATTACKER_DECLARATION during attack tax payment).
        AwaitingInput initialAwaitingInput = gameData.interaction.awaitingInputType();
        Set<Permanent> visited = Collections.newSetFromMap(new IdentityHashMap<>());

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
            if (gameData.interaction.awaitingInputType() != initialAwaitingInput) {
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

        AwaitingInput initialAwaitingInput = gameData.interaction.awaitingInputType();
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
            if (gameData.interaction.awaitingInputType() != initialAwaitingInput) {
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

        AwaitingInput initialAwaitingInput = gameData.interaction.awaitingInputType();
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
            if (gameData.interaction.awaitingInputType() != initialAwaitingInput) {
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
     * {@link DealDamageToControllerEffect} side effect (pain lands).
     */
    private static boolean hasManaAbilityWithDamageCost(Card card) {
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof DealDamageToControllerEffect) {
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
                if (effect instanceof AwardManaEffect manaEffect) {
                    pool.add(manaEffect.color(), manaEffect.amount());
                } else if (effect instanceof AwardAnyColorManaEffect aace) {
                    pool.add(ManaColor.COLORLESS, aace.amount());
                } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                    pool.add(ManaColor.COLORLESS);
                }
            }
        } else {
            addActivatedManaAbilitiesToVirtualPool(card, pool, false, null, null, null);
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
            if (effect instanceof AwardManaEffect manaEffect) {
                colors.add(manaEffect.color());
            } else if (effect instanceof AwardAnyColorManaEffect) {
                Collections.addAll(colors, ManaColor.values());
            }
        }
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (isFreeTapManaAbility(ability)) {
                for (CardEffect effect : ability.getEffects()) {
                    if (effect instanceof AwardManaEffect manaEffect) {
                        colors.add(manaEffect.color());
                    } else if (effect instanceof AwardAnyColorManaEffect) {
                        Collections.addAll(colors, ManaColor.values());
                    } else if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                        colors.add(ManaColor.COLORLESS);
                    }
                }
            }
        }
        return colors;
    }

    /**
     * Builds a virtual mana pool excluding mana sources whose activated abilities
     * would trigger an interactive choice (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Used when computing affordable attackers for attack tax, to avoid activating
     * choice-triggering abilities during ATTACKER_DECLARATION.
     */
    public VirtualManaPool buildSafeVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(aiPlayerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                boolean isCreature = gameQueryService.isCreature(gameData, perm);
                if (isCreature && perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                    continue;
                }
                if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                    continue;
                }
                ManaColor overriddenColor = gameQueryService.getOverriddenLandManaColor(gameData, perm);
                if (overriddenColor != null) {
                    virtual.add(overriddenColor, 1);
                    if (isCreature) {
                        virtual.addCreatureMana(overriddenColor, 1);
                    }
                } else if (hasOnTapManaEffects(perm.getCard())) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            virtual.add(manaEffect.color(), manaEffect.amount());
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), manaEffect.amount());
                            }
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            virtual.add(ManaColor.COLORLESS, aace.amount());
                            if (isCreature) {
                                virtual.addCreatureMana(ManaColor.COLORLESS, aace.amount());
                            }
                        } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                            virtual.add(ManaColor.COLORLESS);
                        }
                    }
                } else {
                    // Skip activated mana abilities that would trigger a color choice
                    if (!wouldManaAbilityTriggerChoice(perm.getCard())) {
                        addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, isCreature, perm, gameData, aiPlayerId);
                    }
                }
            }
        }

        return virtual;
    }

    /**
     * Returns true if the card's activated mana abilities would trigger an interactive
     * color choice prompt (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Cards with ON_TAP effects are always safe — they produce mana without choices.
     */
    static boolean wouldManaAbilityTriggerChoice(Card card) {
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof AwardAnyColorManaEffect
                        || effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect
                        || effect instanceof AwardAnyColorManaWithInstantSorceryCopyEffect
                        || effect instanceof AwardFlashbackOnlyAnyColorManaEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /**
     * Returns true if the card has ON_TAP mana-producing effects (basic lands, mana creatures like Llanowar Elves).
     */
    private static boolean hasOnTapManaEffects(Card card) {
        return card.getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect
                        || e instanceof AwardAnyColorChosenSubtypeCreatureManaEffect);
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
            if (!canPayChargeCounterCost(ability, permanent)) {
                continue;
            }
            if (!canMeetTimingRestriction(ability, gameData, playerId, permanent)) {
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
                .anyMatch(e -> e instanceof DealDamageToControllerEffect);
        Map<ManaColor, Integer> coloredCosts = cost.getColoredCosts();

        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof AwardManaEffect award) {
                ManaColor color = award.color();
                int needed = coloredCosts.getOrDefault(color, 0);
                int have = currentPool.get(color);
                if (needed > have) {
                    // This color is needed for a colored cost we can't yet pay
                    return hasSideEffects ? 15 : 20;
                }
                // Can contribute to generic costs
                return hasSideEffects ? 1 : 5;
            }
            if (effect instanceof AwardAnyColorManaEffect) {
                return hasSideEffects ? 1 : 5;
            }
            if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                // Colorless mana - can contribute to generic costs
                return hasSideEffects ? 1 : 5;
            }
        }
        return 0;
    }
}
