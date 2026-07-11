package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds {@link VirtualManaPool}s describing the mana a player could produce right now:
 * the current pool plus every untapped mana source they control. Shared by the AI's
 * planning ({@code AiManaManager}) and by {@code GameBroadcastService}, which uses it to
 * mark hand cards as "castable if you tap your lands" for the MTGO-style casting flow.
 */
@Component
public class PotentialManaService {

    private final GameQueryService gameQueryService;

    public PotentialManaService(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    public VirtualManaPool buildVirtualManaPool(GameData gameData, UUID playerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
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
                            int amount = estimateManaAmount(manaEffect.amount(), perm, gameData);
                            virtual.add(manaEffect.color(), amount);
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), amount);
                            }
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            virtual.add(ManaColor.COLORLESS, aace.amount());
                            if (isCreature) {
                                virtual.addCreatureMana(ManaColor.COLORLESS, aace.amount());
                            }
                        } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                            // Treated as colorless for virtual pool estimation
                            virtual.add(ManaColor.COLORLESS);
                        }
                    }
                } else {
                    // Check activated mana abilities (dual lands, pain lands, utility lands)
                    addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, isCreature, perm, gameData, playerId);
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
    public VirtualManaPool buildLandOnlyVirtualManaPool(GameData gameData, UUID playerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
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
                            virtual.add(manaEffect.color(), estimateManaAmount(manaEffect.amount(), perm, gameData));
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            virtual.add(ManaColor.COLORLESS, aace.amount());
                        } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                            virtual.add(ManaColor.COLORLESS);
                        }
                    }
                } else {
                    addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, false, perm, gameData, playerId);
                }
            }
        }

        return virtual;
    }

    /**
     * Builds a virtual mana pool excluding mana sources whose activated abilities
     * would trigger an interactive choice (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Used when computing affordable attackers for attack tax, to avoid activating
     * choice-triggering abilities during ATTACKER_DECLARATION.
     */
    public VirtualManaPool buildSafeVirtualManaPool(GameData gameData, UUID playerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
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
                            int amount = estimateManaAmount(manaEffect.amount(), perm, gameData);
                            virtual.add(manaEffect.color(), amount);
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), amount);
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
                        addActivatedManaAbilitiesToVirtualPool(perm.getCard(), virtual, isCreature, perm, gameData, playerId);
                    }
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
    public void addActivatedManaAbilitiesToVirtualPool(Card card, ManaPool virtual, boolean isCreature, Permanent permanent,
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
                    int amount = estimateManaAmount(manaEffect.amount(), permanent, gameData);
                    if (amount > 0) {
                        abilityByColor.merge(manaEffect.color(), amount, Integer::sum);
                    }
                } else if (effect instanceof AwardAnyColorManaEffect aace) {
                    abilityByColor.merge(ManaColor.COLORLESS, aace.amount(), Integer::sum);
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
    public boolean canMeetTimingRestriction(ActivatedAbility ability, GameData gameData, UUID playerId, Permanent permanent) {
        if (ability.getTimingRestriction() == null || gameData == null) {
            return true;
        }
        return switch (ability.getTimingRestriction()) {
            case METALCRAFT -> gameQueryService.isMetalcraftMet(gameData, playerId);
            case MORBID -> gameQueryService.isMorbidMet(gameData);
            case OPPONENT_CONTROLS_MORE_LANDS -> gameQueryService.anyOpponentControlsMoreLands(gameData, playerId);
            case ONLY_DURING_YOUR_TURN -> playerId.equals(gameData.activePlayerId);
            case ONLY_DURING_YOUR_UPKEEP -> playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep == TurnStep.UPKEEP;
            case ONLY_WHILE_ATTACKING -> permanent != null && permanent.isAttacking();
            case ONLY_BEFORE_ATTACKERS_DECLARED -> playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep.isBeforeAttackersDeclared();
            case ONLY_DURING_COMBAT -> gameData.currentStep.isCombatPhase();
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
    public static boolean canPayChargeCounterCost(ActivatedAbility ability, Permanent permanent) {
        if (permanent == null) {
            return true;
        }
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof RemoveChargeCountersFromSourceCost cost) {
                if (permanent.getCounterCount(CounterType.CHARGE) < cost.count()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the card's activated mana abilities would trigger an interactive
     * color choice prompt (e.g. AwardAnyColorManaEffect on Birds of Paradise).
     * Cards with ON_TAP effects are always safe — they produce mana without choices.
     */
    public static boolean wouldManaAbilityTriggerChoice(Card card) {
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

    /**
     * Estimates the integer mana quantity an {@link AwardManaEffect} would produce for a
     * virtual mana pool. A flat {@link Fixed} amount is exact; source-relative amounts that can
     * be resolved from the permanent alone — charge counters ({@link CountersOnSource}) and source power
     * ({@link SourcePower}) — are computed directly. Other dynamic amounts (e.g. per-permanent
     * counts) aren't estimated here (they contribute 0); {@code null} permanent/game data
     * (hypothetical card evaluation) yields the fixed value or 0.
     */
    public int estimateManaAmount(DynamicAmount amount, Permanent permanent, GameData gameData) {
        if (amount instanceof Fixed fixed) {
            return fixed.value();
        }
        if (permanent == null || gameData == null) {
            return 0;
        }
        if (amount instanceof CountersOnSource counters) {
            return permanent.getCounterCount(counters.counterType());
        }
        if (amount instanceof SourcePower) {
            return Math.max(0, gameQueryService.getEffectivePower(gameData, permanent));
        }
        return 0;
    }

    /**
     * Returns true if the card has ON_TAP mana-producing effects (basic lands, mana creatures like Llanowar Elves).
     */
    public static boolean hasOnTapManaEffects(Card card) {
        return card.getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect
                        || e instanceof AwardAnyColorChosenSubtypeCreatureManaEffect);
    }
}
