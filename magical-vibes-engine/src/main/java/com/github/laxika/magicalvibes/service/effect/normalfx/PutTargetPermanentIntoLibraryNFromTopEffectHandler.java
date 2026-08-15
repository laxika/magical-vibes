package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetPermanentIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPermanentIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetPermanentIntoLibraryNFromTopEffect) effect;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        int position = amountEvaluationService.evaluate(gameData, e.position(),
                AmountContext.forStackEntry(entry, gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())));
        if (permanentRemovalService.removePermanentToLibraryPosition(gameData, target, position)) {
            String ordinal = switch (position) {
                case 0 -> "on top of";
                case 1 -> "second from the top of";
                case 2 -> "third from the top of";
                default -> (position + 1) + "th from the top of";
            };

            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" is put " + ordinal + " its owner's library.").build());
            log.info("Game {} - {} put {} library (position {})", gameData.id, target.getCard().getName(), ordinal, position);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
