package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfColorTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves "whenever you tap a land for {C}, add an additional {C}" from the stack entry of the triggered mana ability
 * (CR 605.4a): when the tap produced that type, or the tapped land has a tap mana ability that produces it, the
 * ability's controller adds one more.
 */
@Component
@RequiredArgsConstructor
public class AddManaWhenLandOfColorTappedForManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaWhenLandOfColorTappedForManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManaColor color = ((AddManaWhenLandOfColorTappedForManaEffect) effect).color();
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (tappedLand == null || !(entry.getProducedManaColors().contains(color)
                || landProducesManaColor(gameData, tappedLand, color))) {
            return;
        }
        TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, entry.getControllerId(), color, 1);
    }

    private boolean landProducesManaColor(GameData gameData, Permanent land, ManaColor color) {
        return tapManaAbilityProducesColor(ManaAbilities.intrinsic(land.getCard()), color)
                || tapManaAbilityProducesColor(
                        gameQueryService.computeStaticBonus(gameData, land).grantedActivatedAbilities(), color);
    }

    private static boolean tapManaAbilityProducesColor(List<ActivatedAbility> abilities, ManaColor color) {
        return abilities.stream()
                .filter(ActivatedAbility::isRequiresTap)
                .filter(ActivatedAbility::isManaAbility)
                .flatMap(ability -> ability.getEffects().stream())
                .filter(ManaProducingEffect.class::isInstance)
                .map(ManaProducingEffect.class::cast)
                .anyMatch(mana -> mana.estimatedManaColor() == color);
    }
}
