package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeTargetPermanentCopyOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeTargetPermanentCopyOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTargetId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card sourceCard = source != null
                ? source.getCard()
                : entry.getSourcePermanentSnapshot() == null ? null : entry.getSourcePermanentSnapshot().getCard();
        if (sourceCard == null) {
            return;
        }

        String targetName = target.getCard().getName();
        if (source != null) {
            permanentCopierService.applyCloneCopy(target, source, null, null);
        } else {
            permanentCopierService.applyCloneCopy(target, sourceCard, null, null, Set.of());
        }
        gameLogService.append(gameData, GameLog.textCardText(targetName + " becomes a copy of ", sourceCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, targetName, sourceCard.getName());
    }
}
