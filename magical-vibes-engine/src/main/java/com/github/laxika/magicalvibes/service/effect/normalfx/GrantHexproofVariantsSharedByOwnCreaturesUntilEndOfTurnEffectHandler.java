package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHexproofVariantsSharedByOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantHexproofVariantsSharedByOwnCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantHexproofVariantsSharedByOwnCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }
        List<Permanent> creatures = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();

        Set<TargetingRestrictionEffect> variants = new LinkedHashSet<>();
        for (Permanent creature : creatures) {
            GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, creature);
            if (!bonus.losesAllAbilities()) {
                for (CardEffect staticEffect : creature.getCard().getStaticEffects()) {
                    addIfVariant(staticEffect, variants);
                }
            }
            for (CardEffect grantedEffect : bonus.grantedEffects()) {
                addIfVariant(grantedEffect, variants);
            }
        }
        if (variants.isEmpty()) {
            return;
        }

        for (Permanent recipient : creatures) {
            for (TargetingRestrictionEffect variant : variants) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantEffectEffect(variant, GrantScope.TARGET), recipient.getId(), null, null,
                        EffectDuration.UNTIL_END_OF_TURN, 0));
            }
        }
    }

    private static void addIfVariant(CardEffect effect, Set<TargetingRestrictionEffect> variants) {
        if (effect instanceof TargetingRestrictionEffect restriction
                && restriction.hexproofLike()
                && restriction.opponentOnly()
                && (restriction.mode() != TargetColorMode.ANY || !restriction.sourceCardTypes().isEmpty())) {
            variants.add(restriction);
        }
    }
}
