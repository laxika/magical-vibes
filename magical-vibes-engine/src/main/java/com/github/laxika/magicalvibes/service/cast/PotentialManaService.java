package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
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
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
 *
 * <p>Only mana that lands in the plain pool is counted. A source whose mana pays into a restricted
 * bucket — Cavern of Souls' chosen creature type, Ancient Ziggurat's creature spells, the
 * flashback/instant-sorcery/artifact buckets — contributes nothing, because a caller measuring a
 * generic cost against this pool would otherwise be promised mana that cost can never spend.
 * {@code GameActionAvailabilityService.getPotentialPlayableCardIndices} closes that gap the honest
 * way, by unioning with the strictly-affordable indices computed from the real pool.
 */
@Component
public class PotentialManaService {

    /**
     * The pool {@link #canTapForManaNow} measures affordability against. Shared and never mutated:
     * activation legality only reads a pool, and allocating one per ability would land on the MCTS
     * rollout path.
     */
    private static final ManaPool NO_MANA_AVAILABLE = new ManaPool();

    private final GameQueryService gameQueryService;
    /**
     * The authority on activation legality. Injected {@code @Lazy} because the edge closes a
     * construction cycle: {@code AbilityActivationService} reaches back here through the event and
     * view-projection stack (graveyard → log → mutation coordinator → event dispatcher → projection
     * subscriber → view projection factory → {@code GameActionAvailabilityService}). Nothing is
     * called on it during construction, so a deferred proxy costs nothing.
     */
    private final AbilityActivationService abilityActivationService;

    public PotentialManaService(GameQueryService gameQueryService,
                                @Lazy AbilityActivationService abilityActivationService) {
        this.gameQueryService = gameQueryService;
        this.abilityActivationService = abilityActivationService;
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
                if (gameQueryService.isSummoningSickForTapCost(gameData, perm, playerId)) {
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
                } else if (hasLivePrintedTapMana(gameData, perm)) {
                    // Basic lands and permanents with ON_TAP mana effects
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            int amount = estimateManaAmount(manaEffect.amount(), perm, gameData);
                            virtual.add(manaEffect.color(), amount);
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), amount);
                            }
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            addAnyColorManaToVirtualPool(virtual, aace.amount(), isCreature);
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
                } else if (hasLivePrintedTapMana(gameData, perm)) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            virtual.add(manaEffect.color(), estimateManaAmount(manaEffect.amount(), perm, gameData));
                        } else if (effect instanceof AwardAnyColorManaEffect aace) {
                            addAnyColorManaToVirtualPool(virtual, aace.amount(), false);
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
                if (gameQueryService.isSummoningSickForTapCost(gameData, perm, playerId)) {
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
                } else if (hasLivePrintedTapMana(gameData, perm)) {
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

        int creatureManaAdded = 0;
        int maxCreatureManaOption = 0;

        List<ActivatedAbility> abilities = activatedAbilitiesFor(gameData, permanent, card);
        for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
            ActivatedAbility ability = abilities.get(abilityIndex);
            if (!canTapForManaNow(ability, abilityIndex, permanent, gameData, playerId)) {
                continue;
            }

            for (EnumMap<ManaColor, Integer> abilityByColor : manaOptionsFor(ability, permanent, gameData)) {
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
                if (isCreature) {
                    creatureManaAdded += abilityTotal;
                    maxCreatureManaOption = Math.max(maxCreatureManaOption, abilityTotal);
                }
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
            vmp.addCreatureManaOvercount(creatureManaAdded - maxCreatureManaOption);
        }
    }

    /**
     * The mutually exclusive mana outputs one activation of {@code ability} could yield, as one
     * color→amount map per outcome. Fixed producers have a single outcome; "add one mana of any
     * color" ({@link ManaProducingEffect#estimatedCountsAllColors()}) has five, one per color,
     * which is what lets it pay a colored pip of any color. Booking it as colorless instead would
     * cover generic costs but no colored pip at all, so a Birds of Paradise would never mark a
     * {@code {G}} spell castable. Since the source still taps only once, the caller's over-count
     * bookkeeping collapses the five outcomes back to the single mana it really produces.
     */
    private List<EnumMap<ManaColor, Integer>> manaOptionsFor(ActivatedAbility ability, Permanent permanent,
                                                             GameData gameData) {
        EnumMap<ManaColor, Integer> fixed = new EnumMap<>(ManaColor.class);
        int anyColorAmount = 0;
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof AwardManaEffect manaEffect) {
                int amount = estimateManaAmount(manaEffect.amount(), permanent, gameData);
                if (amount > 0) {
                    fixed.merge(manaEffect.color(), amount, Integer::sum);
                }
            } else if (effect instanceof ManaProducingEffect mana && mana.estimatedCountsAllColors()) {
                anyColorAmount += Math.max(1, mana.estimatedWildcardMana());
            } else if (effect instanceof AwardChosenColorManaEffect
                    && permanent != null && permanent.getChosenColor() != null) {
                fixed.merge(ManaColor.valueOf(permanent.getChosenColor().name()), 1, Integer::sum);
            }
        }
        if (anyColorAmount == 0) {
            return List.of(fixed);
        }
        List<EnumMap<ManaColor, Integer>> options = new ArrayList<>();
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS) {
                continue;
            }
            EnumMap<ManaColor, Integer> option = new EnumMap<>(fixed);
            option.merge(color, anyColorAmount, Integer::sum);
            options.add(option);
        }
        return options;
    }

    /**
     * Adds an ON_TAP "one mana of any color" producer to a virtual pool: every color is offered,
     * and the inflation of the total is recorded so the source still counts as the single mana one
     * tap yields. Mirrors {@link #manaOptionsFor} for the slot that has no per-ability bookkeeping.
     */
    private static void addAnyColorManaToVirtualPool(ManaPool virtual, int amount, boolean isCreature) {
        if (amount <= 0) {
            return;
        }
        int colors = 0;
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS) {
                continue;
            }
            colors++;
            virtual.add(color, amount);
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
        }
        if (virtual instanceof VirtualManaPool vmp) {
            vmp.addFlexibleOvercount(amount * (colors - 1));
            if (isCreature) {
                vmp.addCreatureManaOvercount(amount * (colors - 1));
            }
        }
    }

    /**
     * Whether the permanent's printed {@code ON_TAP} mana is still there to be tapped for. Ability
     * loss strips it even when a later-timestamp grant keeps a mana ability alive (Imprisoned in
     * the Moon on a Forest), and {@code AbilityActivationService.tapPermanent} refuses outright in
     * that state — so the granted ability has to be read through the activated-ability path instead
     * of the printed colour being counted twice over.
     */
    public boolean hasLivePrintedTapMana(GameData gameData, Permanent permanent) {
        return hasOnTapManaEffects(permanent.getCard())
                && !gameQueryService.hasLostAllAbilities(gameData, permanent);
    }

    /**
     * The abilities {@code abilityIndex} is measured against — the same list
     * {@code AbilityActivationService.activateAbility} resolves an index against, so a planner's
     * index and the engine's never name different abilities. Falls back to the printed list for
     * hypothetical card evaluation, where there is no permanent to grant or strip anything.
     */
    public List<ActivatedAbility> activatedAbilitiesFor(GameData gameData, Permanent permanent, Card card) {
        if (gameData == null || permanent == null) {
            return card.getActivatedAbilities();
        }
        return abilityActivationService.getEffectiveActivatedAbilities(gameData, permanent);
    }

    /**
     * Returns true if the ability at {@code abilityIndex} is a mana ability the player could tap
     * {@code permanent} for right now. This is the single predicate every mana-planning path shares
     * — the virtual pool here and the AI's payment planning in {@code AiManaManager} — so a new
     * activation gate is honoured everywhere at once.
     *
     * <p>Legality is not re-derived here: it is asked of
     * {@code AbilityActivationService.validateActivationLegality}, the same check the engine runs
     * before paying any cost. A hand-maintained copy of those rules is what let the AI count mana
     * it could never produce — it knew about timing restrictions and source counters but not about
     * mill/pay-life/sacrifice costs, activation limits, "activate only if" conditions, Sen Triplets
     * or Grand Abolisher, so it tapped its whole board and then sent a cast the engine refused.
     *
     * <p>Affordability is measured against an empty pool. A free tap mana ability has no mana cost,
     * so the only thing that reads the pool is an activation tax (Gloom) — and a taxed source can
     * never back mana a planner has already counted as available, which is exactly the conservative
     * answer an empty pool produces.
     *
     * @param permanent the permanent on the battlefield, or null for hypothetical card evaluation
     *                  (source-relative gates are then assumed satisfiable)
     */
    public boolean canTapForManaNow(ActivatedAbility ability, int abilityIndex, Permanent permanent,
                                    GameData gameData, UUID playerId) {
        if (!isFreeTapManaAbility(ability)) {
            return false;
        }
        if (gameData == null || permanent == null) {
            return true; // hypothetical card evaluation: nothing on the board to gate against
        }
        return abilityActivationService.canActivateAbility(
                gameData, playerId, permanent, abilityIndex, NO_MANA_AVAILABLE);
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
