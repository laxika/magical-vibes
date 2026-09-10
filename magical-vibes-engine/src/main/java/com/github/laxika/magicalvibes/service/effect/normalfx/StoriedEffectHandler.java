package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoriedEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StoriedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        checkPermanentStoried(gameData, entry.getControllerId());
    }

    public void checkPermanentStoried(GameData gameData, UUID controllerId) {
        if (controllerId == null || gameData.playersWithEnduringStory.contains(controllerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || !controlsStoriedPermanent(battlefield)
                || battlefield.stream().filter(permanent -> isQualifyingPermanent(gameData, permanent)).count() < 3) {
            return;
        }

        gameData.playersWithEnduringStory.add(controllerId);
        Card sourceCard = battlefield.stream()
                .map(Permanent::getCard)
                .filter(card -> card.getEffects(EffectSlot.STATIC).stream().anyMatch(StoriedEffect.class::isInstance))
                .findFirst()
                .orElse(null);
        if (sourceCard != null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(sourceCard, "'s controller has an enduring story."));
        }
        log.info("Game {} - {} has an enduring story", gameData.id, controllerId);
    }

    private boolean controlsStoriedPermanent(List<Permanent> battlefield) {
        return battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> !permanent.isLosesAllAbilitiesUntilEndOfTurn())
                .map(Permanent::getCard)
                .anyMatch(card -> card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(StoriedEffect.class::isInstance));
    }

    private boolean isQualifyingPermanent(GameData gameData, Permanent permanent) {
        return gameQueryService.isArtifact(gameData, permanent)
                || gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.LEGENDARY)
                || gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.SAGA);
    }
}
