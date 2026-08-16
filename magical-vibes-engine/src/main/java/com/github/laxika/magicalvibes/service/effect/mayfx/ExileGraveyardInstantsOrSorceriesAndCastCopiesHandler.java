package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExileGraveyardInstantsOrSorceriesAndCastCopiesHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card copy = ability.sourceCard();
        if (!accepted) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, player.getId(), List.of(copy.getId()));
    }
}
