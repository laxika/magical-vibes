package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardTwoDifferentColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LandManaTypeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Determines the colored mana a source could produce in the current game state. */
@Component
@RequiredArgsConstructor
public class ManaSourceColorSupport {

    private final GameQueryService gameQueryService;
    private final LandManaTypeSupport landManaTypeSupport;

    public boolean canProduceMultipleColors(GameData gameData, Permanent source) {
        return coloredManaTypesCouldProduce(gameData, source).size() >= 2;
    }

    public Set<ManaColor> coloredManaTypesCouldProduce(GameData gameData, Permanent source) {
        if (source == null) {
            return Set.of();
        }
        if (gameQueryService.isLand(gameData, source)) {
            return landManaTypeSupport.manaTypesCouldProduce(gameData, source).stream()
                    .filter(ManaColor.COLORS::contains)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, source);
        if (staticBonus.losesAllAbilities() || source.isLosesAllAbilitiesUntilEndOfTurn() || source.isFaceDown()) {
            return Set.of();
        }

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(EffectSlot.ON_TAP));
        List<ActivatedAbility> abilities = new ArrayList<>(source.getCard().getActivatedAbilities());
        abilities.addAll(staticBonus.grantedActivatedAbilities());
        abilities.addAll(source.getPersistentGrantedActivatedAbilities());
        abilities.addAll(source.getTemporaryActivatedAbilities());
        abilities.addAll(source.getUntilNextTurnActivatedAbilities());

        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        addColors(effects, source, colors);
        for (ActivatedAbility ability : abilities) {
            addColors(ability.getEffects(), source, colors);
        }
        colors.retainAll(ManaColor.COLORS);
        return Set.copyOf(colors);
    }

    private static void addColors(List<CardEffect> effects, Permanent source, Set<ManaColor> colors) {
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect mana) {
                add(colors, mana.color());
            } else if (effect instanceof AwardAnyColorManaEffect
                    || effect instanceof AwardTwoDifferentColorManaEffect) {
                colors.addAll(ManaColor.COLORS);
            } else if (effect instanceof AwardManaOfColorsEffect mana) {
                colors.addAll(mana.colors());
            } else if (effect instanceof AwardRestrictedManaOfColorsEffect mana) {
                colors.addAll(mana.colors());
            } else if (effect instanceof AwardChosenColorManaEffect) {
                if (source.getChosenColor() != null) {
                    add(colors, ManaColor.valueOf(source.getChosenColor().name()));
                }
            } else if (effect instanceof AwardManaToChosenPlayerEffect mana) {
                if (mana.anyColor()) {
                    colors.addAll(ManaColor.COLORS);
                } else {
                    add(colors, mana.color());
                }
            } else if (effect instanceof AwardRestrictedManaEffect mana) {
                add(colors, mana.color());
            } else if (effect instanceof RemoveCountersForManaEffect mana) {
                colors.addAll(mana.colors());
            } else if (effect instanceof ManaProducingEffect mana) {
                if (mana.estimatedCountsAllColors()) {
                    colors.addAll(ManaColor.COLORS);
                }
                add(colors, mana.estimatedManaColor());
                colors.addAll(mana.estimatedMutuallyExclusiveManaColors());
            }
        }
    }

    private static void add(Set<ManaColor> colors, ManaColor color) {
        if (color != null) {
            colors.add(color);
        }
    }
}
