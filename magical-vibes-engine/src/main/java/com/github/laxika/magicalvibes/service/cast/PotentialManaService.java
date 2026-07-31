package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorCreatureSpellManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Builds {@link VirtualManaPool}s describing the mana a player could produce right now:
 * the current pool plus every untapped mana source they control. Shared by the AI's
 * planning ({@code AiManaManager}) and by {@code GameActionAvailabilityService}, which uses it to
 * mark hand cards as "castable if you tap your lands" for the MTGO-style casting flow.
 */
@Component
public class PotentialManaService {

    private final GameQueryService gameQueryService;
    private final CastingCostService castingCostService;

    public PotentialManaService(GameQueryService gameQueryService, CastingCostService castingCostService) {
        this.gameQueryService = gameQueryService;
        this.castingCostService = castingCostService;
    }

    public VirtualManaPool buildVirtualManaPool(GameData gameData, UUID playerId) {
        return buildVirtualManaPool(gameData, playerId, null);
    }

    /**
     * Variant that leaves out one permanent's own mana production. Used when checking whether
     * a {T}-cost activated ability could be paid by tapping mana sources: the ability's source
     * is tapped by the activation itself, so its mana can never help pay that cost.
     */
    public VirtualManaPool buildVirtualManaPool(GameData gameData, UUID playerId, UUID excludedPermanentId) {
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
                if (perm.isTapped() || perm.getId().equals(excludedPermanentId)) {
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
                // Check for land type overrides (e.g. Evil Presence / Lush Growth)
                List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, perm);
                ManaColor overriddenColor = overriddenColors.size() == 1 ? overriddenColors.getFirst() : null;
                ManaColor fixedLandColor = perm.getCard().hasType(CardType.LAND)
                        ? gameQueryService.fixedLandManaColor(gameData, perm)
                        : null;
                Set<ManaColor> twisted = fixedLandColor == null
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        virtual.add(fixedLandColor, amount);
                        if (isCreature) {
                            virtual.addCreatureMana(fixedLandColor, amount);
                        }
                    }
                } else if (!twisted.isEmpty()) {
                    addTwistedManaToVirtualPool(virtual, twisted, 1, isCreature);
                } else if (overriddenColors.size() > 1) {
                    addTwistedManaToVirtualPool(virtual, new LinkedHashSet<>(overriddenColors), 1, isCreature);
                } else if (overriddenColor != null) {
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
                List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, perm);
                ManaColor overriddenColor = overriddenColors.size() == 1 ? overriddenColors.getFirst() : null;
                ManaColor fixedLandColor = perm.getCard().hasType(CardType.LAND)
                        ? gameQueryService.fixedLandManaColor(gameData, perm)
                        : null;
                Set<ManaColor> twisted = fixedLandColor == null
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        virtual.add(fixedLandColor, amount);
                    }
                } else if (!twisted.isEmpty()) {
                    addTwistedManaToVirtualPool(virtual, twisted, 1, false);
                } else if (overriddenColors.size() > 1) {
                    addTwistedManaToVirtualPool(virtual, new LinkedHashSet<>(overriddenColors), 1, false);
                } else if (overriddenColor != null) {
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
                List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, perm);
                ManaColor overriddenColor = overriddenColors.size() == 1 ? overriddenColors.getFirst() : null;
                ManaColor fixedLandColor = perm.getCard().hasType(CardType.LAND)
                        ? gameQueryService.fixedLandManaColor(gameData, perm)
                        : null;
                Set<ManaColor> twisted = fixedLandColor == null
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        virtual.add(fixedLandColor, amount);
                        if (isCreature) {
                            virtual.addCreatureMana(fixedLandColor, amount);
                        }
                    }
                } else if (!twisted.isEmpty()) {
                    // Multi-color Reality Twist prompts; treat like a choice source in the safe pool.
                    if (twisted.size() == 1) {
                        addTwistedManaToVirtualPool(virtual, twisted, 1, isCreature);
                    }
                } else if (overriddenColor != null) {
                    virtual.add(overriddenColor, 1);
                    if (isCreature) {
                        virtual.addCreatureMana(overriddenColor, 1);
                    }
                } else if (overriddenColors.size() > 1) {
                    // Multi-type land override prompts for a color — skip in the safe pool.
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
            if (!canTapForManaNow(ability, permanent, gameData, playerId)) {
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
                } else if (effect instanceof AwardChosenColorManaEffect
                        && permanent != null && permanent.getChosenColor() != null) {
                    abilityByColor.merge(ManaColor.valueOf(permanent.getChosenColor().name()), 1, Integer::sum);
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
     * Returns true if {@code ability} is a mana ability the player could tap {@code permanent} for
     * right now. This is the single predicate every mana-planning path shares — the virtual pool
     * here and the AI's payment planning in {@code AiManaManager} — so a new activation gate is
     * added once instead of in three hand-synced copies. The authority on activation legality
     * remains {@code AbilityActivationService.validateActivationLegality}; anything it rejects and
     * this predicate accepts becomes mana the AI counts on and can never actually produce.
     *
     * @param permanent the permanent on the battlefield, or null for hypothetical card evaluation
     *                  (source-relative gates are then assumed satisfiable)
     */
    public boolean canTapForManaNow(ActivatedAbility ability, Permanent permanent,
                                    GameData gameData, UUID playerId) {
        return isFreeTapManaAbility(ability)
                && canPaySourceCounterCosts(ability, permanent)
                && meetsRequiredSourceCounters(ability, permanent)
                && isUntaxedToActivate(gameData, permanent)
                && canMeetTimingRestriction(ability, gameData, playerId, permanent);
    }

    /**
     * Returns true if no static effect taxes the source's activated abilities (Gloom: activated
     * abilities of white enchantments cost {3} more). A taxed mana ability is not free — the engine
     * demands the tax before it produces anything — so it can never back mana a planner has already
     * counted as available. Dropping the source is the conservative approximation: modelling one
     * that consumes mana before producing it would have to be threaded through every planning path,
     * and the tax normally costs more than the ability yields. What that gives up is a source worth
     * tapping purely to fix a color at break-even.
     * If permanent or gameData is null (hypothetical card evaluation), assumes no tax.
     */
    private boolean isUntaxedToActivate(GameData gameData, Permanent permanent) {
        if (gameData == null || permanent == null) {
            return true;
        }
        return castingCostService.getActivatedAbilityActivationTax(gameData, permanent) == 0;
    }

    /**
     * Returns true if the source carries the counters its ability demands to be activated at all
     * (Pyramid of the Pantheon's "activate only if there are three or more brick counters").
     * If permanent is null (hypothetical card evaluation), assumes the counters can be there.
     */
    public static boolean meetsRequiredSourceCounters(ActivatedAbility ability, Permanent permanent) {
        if (permanent == null || ability.getRequiredSourceCounterType() == null) {
            return true;
        }
        return permanent.getCounterCount(ability.getRequiredSourceCounterType())
                >= ability.getRequiredSourceCounterCount();
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
            case CAST_NONCREATURE_SPELL_THIS_TURN -> gameQueryService.playerCastNoncreatureSpellThisTurn(gameData, playerId);
            case COVEN -> gameQueryService.isCovenMet(gameData, playerId);
            case METALCRAFT -> gameQueryService.isMetalcraftMet(gameData, playerId);
            case MORBID -> gameQueryService.isMorbidMet(gameData);
            case OPPONENT_CONTROLS_FLYING_CREATURE -> gameQueryService.anyOpponentControlsFlyingCreature(gameData, playerId);
            case OPPONENT_CONTROLS_MORE_LANDS -> gameQueryService.anyOpponentControlsMoreLands(gameData, playerId);
            case ONLY_DURING_YOUR_TURN -> playerId.equals(gameData.activePlayerId);
            case ONLY_DURING_YOUR_UPKEEP -> playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep == TurnStep.UPKEEP;
            case ONLY_DURING_ANY_UPKEEP -> gameData.currentStep == TurnStep.UPKEEP;
            case ONLY_DURING_OPPONENTS_UPKEEP -> gameData.currentStep == TurnStep.UPKEEP
                    && !playerId.equals(gameData.activePlayerId);
            case ONLY_WHILE_ATTACKING -> permanent != null && permanent.isAttacking();
            case ONLY_WHILE_ATTACKING_OR_BLOCKING -> permanent != null
                    && (permanent.isAttacking() || permanent.isBlocking());
            case ONLY_BEFORE_ATTACKERS_DECLARED -> playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep.isBeforeAttackersDeclared();
            case BEFORE_ATTACKERS_DECLARED -> gameData.currentStep.isBeforeAttackersDeclared()
                    && gameData.combatPhasesThisTurn <= 1;
            case BEFORE_BLOCKERS_DECLARED -> gameData.currentStep.isBeforeBlockersDeclared()
                    && gameData.combatPhasesThisTurn <= 1;
            case ONLY_DURING_COMBAT -> gameData.currentStep.isCombatPhase();
            case ONLY_BEFORE_END_OF_COMBAT -> gameData.currentStep.isBeforeEndOfCombat();
            case ONLY_DURING_DECLARE_ATTACKERS_IF_ATTACKED -> gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                    && gameQueryService.isPlayerBeingAttacked(gameData, playerId);
            case ONLY_DURING_DECLARE_BLOCKERS -> gameData.currentStep == TurnStep.DECLARE_BLOCKERS;
            case ONLY_DURING_DECLARE_BLOCKERS_IF_BLOCKED -> gameData.currentStep == TurnStep.DECLARE_BLOCKERS
                    && permanent != null && gameQueryService.isBlockedByAnyCreature(gameData, permanent);
            case ONLY_WHILE_CREATURE -> permanent != null && gameQueryService.isCreature(gameData, permanent);
            case POWER_4_OR_GREATER -> permanent != null && gameQueryService.getEffectivePower(gameData, permanent) >= 4;
            case RAID -> gameData.playersDeclaredAttackersThisTurn.contains(playerId);
            case SORCERY_SPEED -> playerId.equals(gameData.activePlayerId)
                    && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                    && gameData.stack.isEmpty();
        };
    }

    /**
     * Returns true if the permanent can pay any source-counter costs required by the ability.
     * If permanent is null (hypothetical card evaluation), assumes costs can be paid.
     */
    private static boolean canPaySourceCounterCosts(ActivatedAbility ability, Permanent permanent) {
        if (permanent == null) {
            return true;
        }
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof RemoveCounterFromSourceCost cost) {
                int available = switch (cost.counterType()) {
                    case SILVER -> 0;
                    case ANY -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                            + permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
                    default -> permanent.getCounterCount(cost.counterType());
                };
                if (available < cost.count()) {
                    return false;
                }
            }
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
                        || effect instanceof AwardAnyColorCreatureSpellManaEffect
                        || effect instanceof AwardAnyColorManaWithInstantSorceryCopyEffect
                        || effect instanceof AwardFlashbackOnlyAnyColorManaEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Amount of mana a land would produce under a fixed-color replacement (Infernal Darkness),
     * preserving quantity while the type is remapped elsewhere.
     */
    private int estimateLandManaAmount(Permanent perm, GameData gameData, ManaColor overriddenColor) {
        if (overriddenColor != null) {
            return 1;
        }
        if (hasOnTapManaEffects(perm.getCard())) {
            int total = 0;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                total += estimateModeledManaAmount(effect, perm, gameData);
            }
            return total;
        }
        int total = 0;
        for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                total += estimateModeledManaAmount(effect, perm, gameData);
            }
            break; // one tap
        }
        return total;
    }

    private int estimateModeledManaAmount(CardEffect effect, Permanent permanent, GameData gameData) {
        if (!(effect instanceof ManaProducingEffect manaEffect) || !manaEffect.modeledByManaEstimator()) {
            return 0;
        }
        DynamicAmount amount = manaEffect.estimatedManaAmount();
        return amount != null
                ? estimateManaAmount(amount, permanent, gameData)
                : manaEffect.estimatedWildcardMana();
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
     * Reality Twist virtual-pool contribution: single remapped color is exact; multiple options
     * are recorded like dual lands (all colors + flexible overcount).
     */
    private static void addTwistedManaToVirtualPool(ManaPool virtual, Set<ManaColor> twisted,
                                                    int amount, boolean isCreature) {
        if (twisted.size() == 1) {
            ManaColor color = twisted.iterator().next();
            virtual.add(color, amount);
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
            return;
        }
        for (ManaColor color : twisted) {
            virtual.add(color, amount);
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
        }
        if (virtual instanceof VirtualManaPool vmp && twisted.size() > 1) {
            vmp.addFlexibleOvercount(amount * (twisted.size() - 1));
            for (ManaColor color : twisted) {
                vmp.addPerColorOvercount(color, amount);
            }
        }
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
