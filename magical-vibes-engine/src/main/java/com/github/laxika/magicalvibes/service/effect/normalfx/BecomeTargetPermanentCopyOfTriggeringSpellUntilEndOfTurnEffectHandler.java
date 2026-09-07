package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
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
        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(gameData, triggeringCardId);
        if (target == null || triggeringSpell == null) {
            log.info("Game {} - Triggering-spell copy target or spell no longer exists", gameData.id);
            return;
        }

        Card spellCard = triggeringSpell.getCard();
        if (spellCard.isCantBeCopied()) {
            return;
        }

        String originalName = target.getCard().getName();
        if (!target.isCopyUntilEndOfTurn()) {
            target.setPreCopyCard(target.getCard());
        }
        permanentCopierService.applyCloneCopy(target, spellCard, null, null, Set.of());
        target.setCopyUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), effect, target.getId(), null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + spellCard.getName() + " until end of turn."));
        log.info("Game {} - {} becomes a copy of {} until end of turn",
                gameData.id, originalName, spellCard.getName());
    }
}
