package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardHasteGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardUncounterableGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

/**
 * Resolves the mana types a land could produce in the current game state. This includes all of the
 * land's mana abilities and the basic-land-type and mana-replacement effects that currently apply.
 */
@Component
@RequiredArgsConstructor
public class LandManaTypeSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    public Set<ManaColor> manaTypesCouldProduce(GameData gameData, Permanent land) {
        return manaTypesCouldProduce(gameData, land, new HashSet<>());
    }

    private Set<ManaColor> manaTypesCouldProduce(GameData gameData, Permanent land, Set<UUID> visited) {
        if (land == null || !gameQueryService.isLand(gameData, land)) {
            return Set.of();
        }
        if (!visited.add(land.getId())) return Set.of();

        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, land);
        List<CardEffect> printedTapEffects = staticBonus.losesAllAbilities() || land.isLosesAllAbilitiesUntilEndOfTurn()
                || land.isFaceDown() ? List.of() : land.getCard().getEffects(EffectSlot.ON_TAP);
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (!staticBonus.losesAllAbilities() && !land.isLosesAllAbilitiesUntilEndOfTurn() && !land.isFaceDown()) {
            abilities.addAll(land.getCard().getActivatedAbilities());
        }
        abilities.addAll(staticBonus.grantedActivatedAbilities());
        abilities.addAll(land.getPersistentGrantedActivatedAbilities());
        abilities.addAll(land.getTemporaryActivatedAbilities());
        abilities.addAll(land.getUntilNextTurnActivatedAbilities());
        Set<CardSubtype> basicLandTypes = gameQueryService.effectiveBasicLandTypes(gameData, land);
        List<ManaColor> overriddenColors = gameQueryService.getOverriddenLandManaColors(gameData, land);

        boolean hasManaAbility = printedTapEffects.stream().anyMatch(this::isManaEffect)
                || abilities.stream().anyMatch(ability -> ability.getEffects().stream().anyMatch(this::isManaEffect));
        if (!hasManaAbility && basicLandTypes.isEmpty() && overriddenColors.isEmpty()) {
            return Set.of();
        }

        ManaColor fixedColor = gameQueryService.fixedLandManaColor(gameData, land);
        if (fixedColor != null) {
            return Set.of(fixedColor);
        }

        if (gameQueryService.basicLandManaProducesAnyColor(gameData, land)) {
            return EnumSet.copyOf(ManaColor.COLORS);
        }

        if (!overriddenColors.isEmpty()) {
            return EnumSet.copyOf(overriddenColors);
        }

        Set<ManaColor> types = EnumSet.noneOf(ManaColor.class);
        Set<ManaColor> twistedColors = gameQueryService.twistedLandManaColors(gameData, land);
        if (!twistedColors.isEmpty()) {
            types.addAll(twistedColors);
            return types;
        }

        for (CardSubtype subtype : basicLandTypes) {
            types.add(EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(subtype));
        }
        addManaTypesFromEffects(gameData, printedTapEffects, land, types, visited);
        for (ActivatedAbility ability : abilities) {
            addManaTypesFromEffects(gameData, ability.getEffects(), land, types, visited);
        }
        return types;
    }

    private boolean isManaEffect(CardEffect effect) {
        return effect instanceof ManaProducingEffect;
    }

    private void addManaTypesFromEffects(GameData gameData, List<CardEffect> effects,
                                         Permanent source, Set<ManaColor> types, Set<UUID> visited) {
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof AwardManaOfTypeLandsCouldProduceEffect mana) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
                for (UUID playerId : gameData.orderedPlayerIds) {
                    boolean isController = playerId.equals(controllerId);
                    if (mana.scope() == ManaColorLandScope.CONTROLLER ? !isController : isController) continue;
                    for (Permanent land : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                        if (gameQueryService.isLand(gameData, land)
                                && predicateEvaluationService.matchesPermanentPredicate(
                                gameData, land, mana.landPredicate())) {
                            types.addAll(manaTypesCouldProduce(gameData, land, new HashSet<>(visited)));
                        }
                    }
                }
            } else if (effect instanceof AwardAnyColorManaEffect mana) {
                if (mana.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY
                        || mana.restriction() == ManaSpendRestriction.COMMANDER_COLOR_IDENTITY_WITH_CREATURE_TYPE_SCRY) {
                    types.addAll(ManaProductionSupport.commanderColorIdentity(gameData,
                            gameQueryService.findPermanentController(gameData, source.getId())));
                } else if (mana.restriction() == ManaSpendRestriction.CHOSEN_COLORS) {
                    source.getChosenColors().stream()
                            .map(color -> ManaColor.valueOf(color.name()))
                            .forEach(types::add);
                } else {
                    types.addAll(ManaColor.COLORS);
                }
            } else if (effect instanceof AwardManaOfColorsEffect mana) {
                types.addAll(mana.colors());
            } else if (effect instanceof AwardChosenColorManaEffect) {
                if (source.getChosenColor() != null) {
                    types.add(ManaColor.valueOf(source.getChosenColor().name()));
                }
            } else if (effect instanceof AwardHasteGrantingManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof AwardManaToChosenPlayerEffect mana) {
                if (mana.anyColor()) {
                    types.addAll(ManaColor.COLORS);
                } else {
                    addIfNonNull(types, mana.color());
                }
            } else if (effect instanceof AwardRestrictedManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof AwardUncounterableGrantingManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof RemoveCountersForManaEffect mana) {
                types.addAll(mana.colors());
            } else if (effect instanceof ManaProducingEffect mana) {
                if (mana.estimatedCountsAllColors()) {
                    types.addAll(ManaColor.COLORS);
                }
                addIfNonNull(types, mana.estimatedManaColor());
            }
        }
    }

    private static void addIfNonNull(Set<ManaColor> types, ManaColor color) {
        if (color != null) {
            types.add(color);
        }
    }
}
