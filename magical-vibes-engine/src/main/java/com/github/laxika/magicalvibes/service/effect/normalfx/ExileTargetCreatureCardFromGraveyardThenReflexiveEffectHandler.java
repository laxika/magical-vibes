package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Venom's graveyard exile and creates its later reflexive triggered ability. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardThenReflexiveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardThenReflexiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        var effect = (ExileTargetCreatureCardFromGraveyardThenReflexiveEffect) effectToResolve;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        int toughness = targetCard.getToughness() == null ? 0 : Math.max(0, targetCard.getToughness());
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from a graveyard."));
        entry.setEventValue(toughness);
        int effectIndex = entry.getEffectsToResolve().indexOf(effectToResolve);
        if (effectIndex < 0) {
            throw new IllegalStateException("Could not locate graveyard exile effect on stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(effect.reflexiveEffect(), false, true)));
    }
}
