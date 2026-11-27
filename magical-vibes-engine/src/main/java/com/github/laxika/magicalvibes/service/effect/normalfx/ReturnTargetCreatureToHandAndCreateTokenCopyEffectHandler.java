package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Supplant Form's bounce followed by a token copy. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandAndCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Card sourceCard = target.getCard();
        Card copySource = PermanentCopierService.cardToCopy(target);
        if (!permanentRemovalService.removePermanentToHand(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " is returned to its owner's hand."));
        log.info("Game {} - {} returned to owner's hand by {}",
                gameData.id, sourceCard.getName(), entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        // One ordered token creation event (CR 616.1): the creating player orders its doublers, additions and kind
        // replacements; the token copies the creature's copiable values as it last existed (CR 707.2).
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(copySource), null,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
