package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BecomeCopyOfCardInGraveyardUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect copyEffect =
                (BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, copyEffect.cardId());
        if (source == null || graveyardCard == null) {
            return;
        }

        if (!source.isCopyUntilEndOfTurn()) {
            source.setPreCopyCard(source.getCard());
        }
        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, graveyardCard, null, null, Set.of());
        source.setCopyUntilEndOfTurn(true);
        UUID sourcePermanentId = source.getId();
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                sourcePermanentId, null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData,
                GameLog.textCardText(originalName + " becomes a copy of ", graveyardCard,
                        " until end of turn."));
        log.info("Game {} - {} becomes a copy of {} until end of turn",
                gameData.id, originalName, graveyardCard.getName());
    }
}
