package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn.add(controllerId);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": until end of turn, you may cast instant and sorcery spells from the top of your "
                        + "graveyard. If a spell cast this way would be put into a graveyard, exile it "
                        + "instead."));
        log.info("Game {} - {} may cast top instant/sorcery from graveyard until end of turn",
                gameData.id, controllerId);
    }
}
