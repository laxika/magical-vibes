package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept/decline half of Elite Arcanist's copy ability. A declined copy is removed from the exile
 * zone it was parked in, since a copy of a card outside the stack or battlefield ceases to exist
 * (CR 707.10a).
 */
@Component
@RequiredArgsConstructor
public class CopyImprintedCardAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyImprintedCardAndMayCastCopyEffect.class;
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
