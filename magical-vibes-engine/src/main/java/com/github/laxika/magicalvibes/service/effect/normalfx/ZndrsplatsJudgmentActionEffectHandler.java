package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ZndrsplatsJudgmentActionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies one completed Zndrsplt's Judgment friend-or-foe choice. */
@Component
@RequiredArgsConstructor
public class ZndrsplatsJudgmentActionEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_COPY =
            new CreateTokenCopyOfTargetPermanentEffect();

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ZndrsplatsJudgmentActionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ZndrsplatsJudgmentActionEffect action = (ZndrsplatsJudgmentActionEffect) effect;
        Permanent creature = gameQueryService.findPermanentById(gameData, action.creatureId());
        if (creature == null
                || !action.playerId().equals(gameQueryService.findPermanentController(gameData, action.creatureId()))
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        if (action.friend()) {
            StackEntry copyEntry = new StackEntry(entry.getCard(), action.playerId());
            copyEntry.setTargetId(action.creatureId());
            tokenCopyHandler.resolve(gameData, copyEntry, TOKEN_COPY);
            return;
        }

        Card card = creature.getCard();
        if (permanentRemovalService.removePermanentToHand(gameData, creature)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is returned to its owner's hand."));
        }
    }
}
