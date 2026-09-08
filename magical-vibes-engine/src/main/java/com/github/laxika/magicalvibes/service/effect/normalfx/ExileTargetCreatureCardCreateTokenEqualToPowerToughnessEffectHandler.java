package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an exile-from-your-graveyard effect with a stat-matched token rider. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect) effect;

        UUID targetCardId = entry.getTargetId();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        int power = targetCard.getPower() == null ? 0 : Math.max(0, targetCard.getPower());
        int toughness = targetCard.getToughness() == null ? 0 : Math.max(0, targetCard.getToughness());

        graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard);

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " exiles ", targetCard, " from your graveyard."));

        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                e.tokenTemplate().withPowerToughness(power, toughness),
                1,
                entry.getCard().getSetCode()));
    }
}
