package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Supplant Form's bounce followed by a token copy. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
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
        if (!permanentRemovalService.removePermanentToHand(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " is returned to its owner's hand."));
        log.info("Game {} - {} returned to owner's hand by {}",
                gameData.id, sourceCard.getName(), entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                new CreateTokenCopyOfTargetPermanentEffect();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        var enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                    sourceCard, copyEffect);
            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, entry.getControllerId(), tokenPermanent,
                    enterTappedTypesSnapshot, simultaneouslyEntered);
            simultaneouslyEntered.add(tokenPermanent);

            gameLogService.append(gameData, GameLog.textCardText(
                    "A token copy of ", sourceCard, " is created."));
            log.info("Game {} - Token copy of {} created via {}",
                    gameData.id, sourceCard.getName(), entry.getCard().getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), tokenCard, null, false);
        }
    }
}
