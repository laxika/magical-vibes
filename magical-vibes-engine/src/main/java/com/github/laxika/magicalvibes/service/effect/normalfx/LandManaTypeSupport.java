package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardHasteGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardUncounterableGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves the mana types a land could produce in the current game state. This includes all of the
 * land's mana abilities and the basic-land-type and mana-replacement effects that currently apply.
 */
@Component
@RequiredArgsConstructor
public class LandManaTypeSupport {

    private final GameQueryService gameQueryService;

    public Set<ManaColor> manaTypesCouldProduce(GameData gameData, Permanent land) {
        if (land == null || !land.getCard().hasType(CardType.LAND)) {
            return Set.of();
        }

        List<CardEffect> printedTapEffects = land.getCard().getEffects(EffectSlot.ON_TAP);
        List<ActivatedAbility> abilities = land.getCard().getActivatedAbilities();
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
        addManaTypesFromEffects(printedTapEffects, land, types);
        for (ActivatedAbility ability : abilities) {
            addManaTypesFromEffects(ability.getEffects(), land, types);
        }
        return types;
    }

    private boolean isManaEffect(CardEffect effect) {
        return effect instanceof ManaProducingEffect;
    }

    private void addManaTypesFromEffects(List<CardEffect> effects, Permanent source, Set<ManaColor> types) {
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof AwardAnyColorManaEffect) {
                types.addAll(ManaColor.COLORS);
            } else if (effect instanceof AwardManaOfColorsEffect mana) {
                types.addAll(mana.colors());
            } else if (effect instanceof AwardChosenColorManaEffect) {
                if (source.getChosenColor() != null) {
                    types.add(ManaColor.valueOf(source.getChosenColor().name()));
                }
            } else if (effect instanceof AwardHasteGrantingManaEffect mana) {
                addIfNonNull(types, mana.color());
            } else if (effect instanceof AwardManaToChosenPlayerEffect mana) {
                addIfNonNull(types, mana.color());
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
