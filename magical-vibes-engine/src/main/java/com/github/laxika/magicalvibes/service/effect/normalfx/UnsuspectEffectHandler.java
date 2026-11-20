package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.UnsuspectEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnsuspectEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnsuspectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UnsuspectEffect unsuspect = (UnsuspectEffect) effect;
        List<UUID> targetIds = switch (unsuspect.scope()) {
            case SELF, TARGET, ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE ->
                    subjectResolutionService.permanents(gameData, entry, effect, unsuspect.scope().subject()).stream()
                            .map(Permanent::getId).toList();
            default -> List.of();
        };

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !target.isSuspected()) {
                continue;
            }
            target.setSuspected(false);
            synchronized (gameData.floatingEffects) {
                gameData.expireFloatingEffects(floating -> targetId.equals(floating.affectedPermanentId())
                        && floating.effect() instanceof SuspectedEffect);
            }
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is no longer suspected."));
        }
    }
}
