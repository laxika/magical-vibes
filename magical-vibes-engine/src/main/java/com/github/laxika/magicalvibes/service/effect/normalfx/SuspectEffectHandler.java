package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SuspectEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SuspectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SuspectEffect suspect = (SuspectEffect) effect;
        List<UUID> targetIds = switch (suspect.scope()) {
            case SELF, TARGET, ENCHANTED_CREATURE -> subjectResolutionService.permanents(gameData, entry, effect,
                    suspect.scope().subject()).stream().map(Permanent::getId).toList();
            case TOKENS_CREATED_THIS_RESOLUTION -> entry.getCreatedPermanentIds();
            default -> List.of();
        };

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            if (target.isSuspected() || gameQueryService.cantBecomeSuspected(gameData, target)) {
                continue;
            }
            target.setSuspected(true);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is suspected."));
        }
    }
}
