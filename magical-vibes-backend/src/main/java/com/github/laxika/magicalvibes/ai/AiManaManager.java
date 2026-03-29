package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.Collections;
import java.util.EnumSet;
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

    public ManaPool buildVirtualManaPool(GameData gameData, UUID aiPlayerId) {
        ManaPool virtual = new ManaPool();

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
     * Adds mana from activated mana abilities to the virtual pool.
     * For permanents with multiple free-tap mana abilities (e.g. dual lands),
     * all possible colors are added but the total is corrected via flexibleOvercount
     * since the permanent can only be tapped once.
     *
     * @param permanent the permanent on the battlefield (null for hypothetical card evaluation)
     */
    private void addActivatedManaAbilitiesToVirtualPool(Card card, ManaPool virtual, boolean isCreature, Permanent permanent,
                                                        GameData gameData, UUID playerId) {
        int manaAbilityCount = 0;
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
            manaAbilityCount++;
            for (CardEffect effect : ability.getEffects()) {
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
                } else if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                    if (permanent != null) {
                        int count = permanent.getChargeCounters();
                        if (count > 0) {
                            virtual.add(ManaColor.COLORLESS, count);
                            if (isCreature) {
                                virtual.addCreatureMana(ManaColor.COLORLESS, count);
                            }
                        }
                    }
                }
            }
        }
        // Each permanent can only be tapped once, but we added mana for all abilities.
        // Track the over-count so canPay uses the correct effective total.
        if (manaAbilityCount > 1) {
            virtual.addFlexibleOvercount(manaAbilityCount - 1);
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
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        if (cost.canPay(currentPool, costModifier)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }

            if (hasOnTapManaEffects(perm.getCard())) {
                action.tap(i, null);
            } else {
                Integer abilityIndex = chooseBestManaAbilityIndex(perm.getCard(), cost, currentPool, perm, gameData, aiPlayerId);
                if (abilityIndex == null) {
                    continue;
                }
                action.tap(i, abilityIndex);
            }

            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (cost.canPay(currentPool, costModifier)) {
                return;
            }
            if (gameData.interaction.isAwaitingInput()) {
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

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }
            if (!gameQueryService.isCreature(gameData, perm)) {
                continue;
            }
            if (perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }

            if (hasOnTapManaEffects(perm.getCard())) {
                action.tap(i, null);
            } else {
                Integer abilityIndex = chooseBestManaAbilityIndex(perm.getCard(), cost, currentPool, perm, gameData, aiPlayerId);
                if (abilityIndex == null) {
                    continue;
                }
                action.tap(i, abilityIndex);
            }

            currentPool = gameData.playerManaPools.get(aiPlayerId);
            if (cost.canPayCreatureOnly(currentPool, costModifier)) {
                return;
            }
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
    }

    void tapLandsForXSpell(GameData gameData, UUID aiPlayerId, Card card, int xValue, int costModifier, ManaTapAction action) {
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayerId);

        boolean alreadyPaid;
        if (card.getXColorRestriction() != null) {
            alreadyPaid = cost.canPay(currentPool, xValue, card.getXColorRestriction(), costModifier);
        } else {
            alreadyPaid = cost.canPay(currentPool, xValue + costModifier);
        }
        if (alreadyPaid) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayerId);
        if (battlefield == null) {
            return;
        }

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }
            if (!gameQueryService.canActivateManaAbility(gameData, perm)) {
                continue;
            }

            if (hasOnTapManaEffects(perm.getCard())) {
                action.tap(i, null);
            } else {
                Integer abilityIndex = chooseBestManaAbilityIndex(perm.getCard(), cost, currentPool, perm, gameData, aiPlayerId);
                if (abilityIndex == null) {
                    continue;
                }
                action.tap(i, abilityIndex);
            }

            currentPool = gameData.playerManaPools.get(aiPlayerId);
            boolean canPayNow;
            if (card.getXColorRestriction() != null) {
                canPayNow = cost.canPay(currentPool, xValue, card.getXColorRestriction(), costModifier);
            } else {
                canPayNow = cost.canPay(currentPool, xValue + costModifier);
            }
            if (canPayNow) {
                return;
            }
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
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
