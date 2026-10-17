package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a temporary grant of creature types without granting a keyword ability. */
@Component
@RequiredArgsConstructor
public class TemporaryCreatureTypesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAllCreatureTypesToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        CardEffect applied = effect;
        if (effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                && grant.scope() == GrantScope.OWN_CREATURES) {
            // The creatures affected are those the controller has as the ability resolves (CR 611.2c).
            targets = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .map(Permanent::getId)
                    .toList();
            applied = new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.TARGET, grant.filter());
        } else if (targets.isEmpty() && entry.getTargetId() != null) {
            targets = List.of(entry.getTargetId());
        }
        for (UUID targetId : targets) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), applied,
                    targetId, null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
