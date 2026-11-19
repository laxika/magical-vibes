package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (targetId == null || triggeringCardId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - Triggering-spell copy target no longer exists", gameData.id);
            return;
        }

        // The spell may already have left the stack (countered in response); its last known card is
        // still copied, so the copy uses the card wherever it now is.
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(gameData, triggeringCardId);
        Card spellCard = triggeringSpell != null
                ? triggeringSpell.getCard()
                : gameQueryService.findCardById(gameData, triggeringCardId);
        if (spellCard == null) {
            log.info("Game {} - Triggering spell no longer exists", gameData.id);
            return;
        }
        if (spellCard.isCantBeCopied()) {
            return;
        }

        String originalName = target.getCard().getName();
        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, target);
        permanentCopierService.applyCloneCopy(target, spellCard, null, null, Set.of());
        PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                target, preCopyCard, Duration.UNTIL_END_OF_TURN);

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + spellCard.getName() + " until end of turn."));
        log.info("Game {} - {} becomes a copy of {} until end of turn",
                gameData.id, originalName, spellCard.getName());
    }
}
