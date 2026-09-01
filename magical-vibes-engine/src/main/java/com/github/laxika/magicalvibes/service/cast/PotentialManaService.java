package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
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
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandler;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
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

    /**
     * Deferred because the handler graph includes logging services that reach view projection and
     * therefore this service again. The registry is only consulted while answering a mana query.
     */
    private final ManaAbilityEffectHandlerRegistry manaAbilityEffectHandlerRegistry;

    public PotentialManaService(GameQueryService gameQueryService,
                                @Lazy AbilityActivationService abilityActivationService) {
        this(gameQueryService, abilityActivationService, null);
    }

    @Autowired
    public PotentialManaService(GameQueryService gameQueryService,
                                @Lazy AbilityActivationService abilityActivationService,
                                @Lazy ManaAbilityEffectHandlerRegistry manaAbilityEffectHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.abilityActivationService = abilityActivationService;
        this.manaAbilityEffectHandlerRegistry = manaAbilityEffectHandlerRegistry;
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
                virtual.addSnowManaTag(color, current.getSnowMana(color));
                virtual.addBasicLandManaTag(color, current.getBasicLandMana(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
                virtual.addAbilityOnlyMana(color, current.getAbilityOnlyMana(color));
                virtual.addLandAbilityOnlyMana(color, current.getLandAbilityOnlyMana(color));
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
                boolean anyColorReplacement = perm.getCard().hasType(CardType.LAND)
                        && fixedLandColor == null
                        && gameQueryService.basicLandManaProducesAnyColor(gameData, perm);
                Set<ManaColor> twisted = fixedLandColor == null && !anyColorReplacement
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        addManaToVirtualPool(virtual, gameData, perm, fixedLandColor, amount);
                        if (isCreature) {
                            virtual.addCreatureMana(fixedLandColor, amount);
                        }
                    }
                } else if (anyColorReplacement) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        addAnyColorManaToVirtualPool(virtual, amount, isCreature,
                                isBasicLandSource(gameData, perm));
                    }
                } else if (!twisted.isEmpty()) {
                    addTwistedManaToVirtualPool(virtual, twisted, 1, isCreature,
                            isBasicLandSource(gameData, perm));
                } else if (overriddenColors.size() > 1) {
                    addTwistedManaToVirtualPool(virtual, new LinkedHashSet<>(overriddenColors), 1, isCreature,
                            isBasicLandSource(gameData, perm));
                } else if (overriddenColor != null) {
                    addManaToVirtualPool(virtual, gameData, perm, overriddenColor, 1);
                    if (isCreature) {
                        virtual.addCreatureMana(overriddenColor, 1);
                    }
                } else if (hasLivePrintedTapMana(gameData, perm)) {
                    // Basic lands and permanents with ON_TAP mana effects
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            int amount = estimateManaAmount(manaEffect.amount(), perm, gameData);
                            addManaToVirtualPool(virtual, gameData, perm, manaEffect.color(), amount);
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), amount);
                            }
                        } else if (effect instanceof AwardAnyColorManaEffect anyColor
                                && anyColor.restriction() == ManaSpendRestriction.NONE) {
                            // A spend restriction keeps the mana out of the ordinary pool a cost draws from.
                            addAnyColorManaToVirtualPool(virtual, estimateManaAmount(anyColor.amount(), perm, gameData), isCreature);
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
                virtual.addSnowManaTag(color, current.getSnowMana(color));
                virtual.addBasicLandManaTag(color, current.getBasicLandMana(color));
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
                boolean anyColorReplacement = perm.getCard().hasType(CardType.LAND)
                        && fixedLandColor == null
                        && gameQueryService.basicLandManaProducesAnyColor(gameData, perm);
                Set<ManaColor> twisted = fixedLandColor == null && !anyColorReplacement
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        addManaToVirtualPool(virtual, gameData, perm, fixedLandColor, amount);
                    }
                } else if (anyColorReplacement) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        addAnyColorManaToVirtualPool(virtual, amount, false,
                                isBasicLandSource(gameData, perm));
                    }
                } else if (!twisted.isEmpty()) {
                    addTwistedManaToVirtualPool(virtual, twisted, 1, false,
                            isBasicLandSource(gameData, perm));
                } else if (overriddenColors.size() > 1) {
                    addTwistedManaToVirtualPool(virtual, new LinkedHashSet<>(overriddenColors), 1, false,
                            isBasicLandSource(gameData, perm));
                } else if (overriddenColor != null) {
                    addManaToVirtualPool(virtual, gameData, perm, overriddenColor, 1);
                } else if (hasLivePrintedTapMana(gameData, perm)) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            addManaToVirtualPool(virtual, gameData, perm, manaEffect.color(),
                                    estimateManaAmount(manaEffect.amount(), perm, gameData));
                        } else if (effect instanceof AwardAnyColorManaEffect anyColor
                                && anyColor.restriction() == ManaSpendRestriction.NONE) {
                            addAnyColorManaToVirtualPool(virtual, estimateManaAmount(anyColor.amount(), perm, gameData), false);
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
     * Returns whether tapping this source for mana would open a choice interaction. Combat-cost
     * payment must remain inside its existing declaration prompt, so the AI excludes these sources
     * rather than replacing that prompt before the declaration is submitted.
     */
    public boolean wouldTapForManaOpenChoice(GameData gameData, Permanent permanent) {
        if (hasAttachedManaChoiceTrigger(gameData, permanent)) {
            return true;
        }

        List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, permanent);
        ManaColor fixedLandColor = permanent.getCard().hasType(CardType.LAND)
                ? gameQueryService.fixedLandManaColor(gameData, permanent)
                : null;
        if (fixedLandColor == null && permanent.getCard().hasType(CardType.LAND)) {
            if (gameQueryService.basicLandManaProducesAnyColor(gameData, permanent)) {
                return true;
            }
            Set<ManaColor> twisted = gameQueryService.twistedLandManaColors(gameData, permanent);
            if (twisted.size() > 1 || (twisted.isEmpty() && overriddenColors.size() > 1)) {
                return true;
            }
        }
        if (fixedLandColor != null || overriddenColors.size() == 1) {
            return false;
        }

        Card card = permanent.getCard();
        if (hasLivePrintedTapMana(gameData, permanent)) {
            return card.getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(PotentialManaService::manaEffectOpensChoice);
        }
        return wouldManaAbilityTriggerChoice(activatedAbilitiesFor(gameData, permanent, card));
    }

    private static boolean hasAttachedManaChoiceTrigger(GameData gameData, Permanent manaSource) {
        boolean[] found = {false};
        gameData.forEachPermanent((controllerId, attachment) -> {
            if (found[0] || !attachment.isAttached()
                    || !manaSource.getId().equals(attachment.getAttachedTo())) {
                return;
            }
            found[0] = attachment.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).stream()
                    .filter(AddManaOnEnchantedLandTapEffect.class::isInstance)
                    .map(AddManaOnEnchantedLandTapEffect.class::cast)
                    .map(AddManaOnEnchantedLandTapEffect::mana)
                    .anyMatch(PotentialManaService::manaEffectOpensChoice);
        });
        return found[0];
    }

    private static boolean manaEffectOpensChoice(CardEffect effect) {
        return effect instanceof AwardAnyColorManaEffect
                || (effect instanceof AwardManaOfColorsEffect ofColors && ofColors.colors().size() > 1)
                || effect instanceof AwardManaOfColorsLandsCouldProduceEffect;
    }

    /**
     * Builds a virtual mana pool excluding sources whose mana tap would open an interactive
     * choice, whether from the source itself, a replacement, or an attached mana trigger.
     * Used when computing affordable attackers for attack tax, to keep the declaration prompt
     * active while mana abilities are activated.
     */
    public VirtualManaPool buildSafeVirtualManaPool(GameData gameData, UUID playerId) {
        VirtualManaPool virtual = new VirtualManaPool();

        ManaPool current = gameData.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addSnowManaTag(color, current.getSnowMana(color));
                virtual.addBasicLandManaTag(color, current.getBasicLandMana(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                if (wouldTapForManaOpenChoice(gameData, perm)) {
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
                boolean anyColorReplacement = perm.getCard().hasType(CardType.LAND)
                        && fixedLandColor == null
                        && gameQueryService.basicLandManaProducesAnyColor(gameData, perm);
                Set<ManaColor> twisted = fixedLandColor == null && !anyColorReplacement
                        ? gameQueryService.twistedLandManaColors(gameData, perm)
                        : Set.of();
                if (fixedLandColor != null) {
                    ManaColor amountKey = overriddenColor != null ? overriddenColor
                            : (overriddenColors.isEmpty() ? null : overriddenColors.getFirst());
                    int amount = estimateLandManaAmount(perm, gameData, amountKey);
                    if (amount > 0) {
                        addManaToVirtualPool(virtual, gameData, perm, fixedLandColor, amount);
                        if (isCreature) {
                            virtual.addCreatureMana(fixedLandColor, amount);
                        }
                    }
                } else if (anyColorReplacement) {
                    // This replacement opens the same color-choice interaction as an any-color
                    // mana ability, so it is excluded from the safe pool.
                } else if (!twisted.isEmpty()) {
                    // Multi-color Reality Twist prompts; treat like a choice source in the safe pool.
                    if (twisted.size() == 1) {
                        addTwistedManaToVirtualPool(virtual, twisted, 1, isCreature,
                                isBasicLandSource(gameData, perm));
                    }
                } else if (overriddenColor != null) {
                    addManaToVirtualPool(virtual, gameData, perm, overriddenColor, 1);
                    if (isCreature) {
                        virtual.addCreatureMana(overriddenColor, 1);
                    }
                } else if (overriddenColors.size() > 1) {
                    // Multi-type land override prompts for a color — skip in the safe pool.
                } else if (hasLivePrintedTapMana(gameData, perm)) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                        if (effect instanceof AwardManaEffect manaEffect) {
                            int amount = estimateManaAmount(manaEffect.amount(), perm, gameData);
                            addManaToVirtualPool(virtual, gameData, perm, manaEffect.color(), amount);
                            if (isCreature) {
                                virtual.addCreatureMana(manaEffect.color(), amount);
                            }
                        }
                        // An ON_TAP "add one mana of any color" prompts for the color just as the
                        // activated-ability form does, so it is skipped here for the same reason.
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

            for (EnumMap<ManaColor, Integer> abilityByColor : manaOptionsFor(ability, permanent, gameData, playerId)) {
                int abilityTotal = 0;
                for (Map.Entry<ManaColor, Integer> e : abilityByColor.entrySet()) {
                    ManaColor color = e.getKey();
                    int amount = e.getValue();
                    addManaToVirtualPool(virtual, gameData, permanent, color, amount);
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
                                                             GameData gameData, UUID playerId) {
        EnumMap<ManaColor, Integer> fixed = new EnumMap<>(ManaColor.class);
        int anyColorAmount = 0;
        List<EnumMap<ManaColor, Integer>> conditionalOptions = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof AwardManaEffect manaEffect) {
                int amount = estimateManaAmount(manaEffect.amount(), permanent, gameData);
                if (amount > 0) {
                    fixed.merge(ManaProductionSupport.effectiveColor(gameData, null, permanent,
                            manaEffect.color()), amount, Integer::sum);
                }
            } else if (effect instanceof ManaProducingEffect mana
                    && !mana.estimatedMutuallyExclusiveManaColors().isEmpty()) {
                DynamicAmount amountDefinition = mana.estimatedManaAmount();
                if (amountDefinition != null) {
                    int amount = estimateManaAmount(amountDefinition, permanent, gameData);
                    if (amount > 0) {
                        List<ManaColor> availableColors = mana.estimatedMutuallyExclusiveManaColors();
                        ManaAbilityEffectHandler handler = manaAbilityEffectHandlerRegistry == null
                                ? null
                                : manaAbilityEffectHandlerRegistry.getHandler(effect);
                        if (handler != null) {
                            List<ManaColor> currentColors = handler.availableManaColors(
                                    gameData, playerId, permanent, effect);
                            if (!currentColors.isEmpty()) {
                                availableColors = currentColors;
                            }
                        }
                        for (ManaColor color : availableColors) {
                            EnumMap<ManaColor, Integer> option = new EnumMap<>(ManaColor.class);
                            option.put(color, amount);
                            conditionalOptions.add(option);
                        }
                    }
                }
            } else if (effect instanceof ManaProducingEffect mana && mana.estimatedCountsAllColors()) {
                anyColorAmount += Math.max(1, mana.estimatedWildcardMana());
            } else if (effect instanceof AwardChosenColorManaEffect chosen
                    && chosen.restriction() == null
                    && permanent != null && permanent.getChosenColor() != null) {
                fixed.merge(ManaColor.valueOf(permanent.getChosenColor().name()), 1, Integer::sum);
            }
        }
        List<EnumMap<ManaColor, Integer>> baseOptions;
        if (conditionalOptions.isEmpty()) {
            baseOptions = List.of(fixed);
        } else {
            baseOptions = conditionalOptions.stream().map(option -> {
                EnumMap<ManaColor, Integer> merged = new EnumMap<>(fixed);
                option.forEach((color, amount) -> merged.merge(color, amount, Integer::sum));
                return merged;
            }).toList();
        }
        if (anyColorAmount == 0) {
            return baseOptions;
        }
        List<EnumMap<ManaColor, Integer>> options = new ArrayList<>();
        for (EnumMap<ManaColor, Integer> baseOption : baseOptions) {
            for (ManaColor color : ManaColor.COLORS) {
                EnumMap<ManaColor, Integer> option = new EnumMap<>(baseOption);
                option.merge(color, anyColorAmount, Integer::sum);
                options.add(option);
            }
        }
        return options;
    }

    /**
     * Adds a "one mana of any color" producer to a virtual pool: every color is offered, and the
     * inflation of the total is recorded so the source still counts as the single mana one tap
     * yields. Mirrors {@link #manaOptionsFor} for the slots that have no per-ability bookkeeping —
     * the {@code ON_TAP} slot here, and {@code AiManaManager.addCardManaToPool}'s hypothetical
     * land evaluation.
     */
    public static void addAnyColorManaToVirtualPool(ManaPool virtual, int amount, boolean isCreature) {
        addAnyColorManaToVirtualPool(virtual, amount, isCreature, false);
    }

    public static void addAnyColorManaToVirtualPool(ManaPool virtual, int amount, boolean isCreature,
                                                    boolean basicLandSource) {
        if (amount <= 0) {
            return;
        }
        for (ManaColor color : ManaColor.COLORS) {
            virtual.add(color, amount);
            if (basicLandSource) {
                virtual.addBasicLandManaTag(color, amount);
            }
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
        }
        if (virtual instanceof VirtualManaPool vmp) {
            int overcount = amount * (ManaColor.COLORS.size() - 1);
            vmp.addFlexibleOvercount(overcount);
            if (isCreature) {
                vmp.addCreatureManaOvercount(overcount);
            }
        }
    }

    private void addManaToVirtualPool(ManaPool virtual, GameData gameData, Permanent source,
                                      ManaColor color, int amount) {
        color = ManaProductionSupport.effectiveColor(gameData, null, source, color);
        if (source != null && gameQueryService.hasEffectiveSupertype(gameData, source, CardSupertype.SNOW)) {
            virtual.addSnowMana(color, amount);
        } else {
            virtual.add(color, amount);
        }
        if (source != null && gameQueryService.hasEffectiveSupertype(gameData, source, CardSupertype.BASIC)) {
            virtual.addBasicLandManaTag(color, amount);
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
                && AbilityActivationService.isManaAbility(ability);
    }

    /**
     * Returns true if the card's activated mana abilities would trigger an interactive
     * color choice prompt (e.g. AwardAnyColorManaEffect on Birds of Paradise,
     * AwardManaOfColorsEffect with multiple colors, or AwardManaOfColorsLandsCouldProduceEffect on
     * Fellwar Stone).
     * Cards with ON_TAP effects are always safe — they produce mana without choices.
     */
    public static boolean wouldManaAbilityTriggerChoice(Card card) {
        return wouldManaAbilityTriggerChoice(card.getActivatedAbilities());
    }

    private static boolean wouldManaAbilityTriggerChoice(List<ActivatedAbility> abilities) {
        for (ActivatedAbility ability : abilities) {
            if (!isFreeTapManaAbility(ability)) {
                continue;
            }
            for (CardEffect effect : ability.getEffects()) {
                if (manaEffectOpensChoice(effect)) {
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
        addTwistedManaToVirtualPool(virtual, twisted, amount, isCreature, false);
    }

    private static void addTwistedManaToVirtualPool(ManaPool virtual, Set<ManaColor> twisted,
                                                    int amount, boolean isCreature,
                                                    boolean basicLandSource) {
        if (twisted.size() == 1) {
            ManaColor color = twisted.iterator().next();
            virtual.add(color, amount);
            if (basicLandSource) {
                virtual.addBasicLandManaTag(color, amount);
            }
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
            return;
        }
        for (ManaColor color : twisted) {
            virtual.add(color, amount);
            if (basicLandSource) {
                virtual.addBasicLandManaTag(color, amount);
            }
            if (isCreature) {
                virtual.addCreatureMana(color, amount);
            }
        }
        if (virtual instanceof VirtualManaPool vmp && twisted.size() > 1) {
            // Only the total is inflated. Each colour is offered by exactly one of the mutually
            // exclusive options, so its per-colour maximum equals its per-colour sum and the
            // per-colour over-count is zero — subtracting `amount` per colour instead drove
            // get(colour) to 0 for every option, leaving the land unable to pay any pip at all.
            vmp.addFlexibleOvercount(amount * (twisted.size() - 1));
            if (isCreature) {
                vmp.addCreatureManaOvercount(amount * (twisted.size() - 1));
            }
        }
    }

    private boolean isBasicLandSource(GameData gameData, Permanent permanent) {
        return permanent != null
                && gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC);
    }

    /**
     * Returns true if the card has ON_TAP mana-producing effects (basic lands, mana creatures like Llanowar Elves).
     */
    public static boolean hasOnTapManaEffects(Card card) {
        return card.getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
    }
}
