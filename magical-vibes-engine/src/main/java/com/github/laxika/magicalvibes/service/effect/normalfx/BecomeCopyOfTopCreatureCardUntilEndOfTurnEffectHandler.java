package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTopCreatureCardUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.getFirst();
        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (!source.isCopyUntilEndOfTurn()) {
            source.setPreCopyCard(source.getCard());
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, topCard, null, null, Set.of());
        source.setCopyUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(),
                entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                source.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + topCard.getName() + " until end of turn."));
        log.info("Game {} - {} becomes a copy of top creature card {} until end of turn",
                gameData.id, originalName, topCard.getName());
    }
}
