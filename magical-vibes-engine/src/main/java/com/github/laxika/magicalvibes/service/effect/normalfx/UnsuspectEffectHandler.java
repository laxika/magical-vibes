package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UnsuspectEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnsuspectEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnsuspectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UnsuspectEffect unsuspect = (UnsuspectEffect) effect;
        List<UUID> targetIds = switch (unsuspect.scope()) {
            case SELF -> entry.getSourcePermanentId() == null
                    ? List.of() : List.of(entry.getSourcePermanentId());
            case TARGET -> {
                List<UUID> ids = entry.targetsForEffect(effect);
                if (ids.isEmpty() && entry.getTargetId() != null) {
                    ids = List.of(entry.getTargetId());
                }
                yield ids;
            }
            case ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE -> {
                Permanent source = entry.getSourcePermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                yield source == null || source.getAttachedTo() == null
                        ? List.of() : List.of(source.getAttachedTo());
            }
            default -> List.of();
        };

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !target.isSuspected()) {
                continue;
            }
            target.setSuspected(false);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is no longer suspected."));
        }
    }
}
