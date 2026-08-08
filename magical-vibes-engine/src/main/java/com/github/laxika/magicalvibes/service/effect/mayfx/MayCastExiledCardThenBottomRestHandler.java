package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardThenBottomRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileBottomRandomSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Offers a card in exile as a free cast and then bottoms the rest of what the same permanent exiled
 * (Possibility Storm). The bottoming runs before the cast so the chosen card — which leaves exile to
 * go on the stack — is the only one that stays out of the library; declining bottoms it too.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastExiledCardThenBottomRestHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final ExileBottomRandomSupport exileBottomRandomSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCardThenBottomRestEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var typedEffect = (MayCastExiledCardThenBottomRestEffect) ability.effects().getFirst();

        if (accepted && ability.targetCardId() != null) {
            exileBottomRandomSupport.bottomCardsExiledWithSource(
                    gameData, typedEffect.sourcePermanentId(), ability.targetCardId());
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, ability.targetCardId());
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        exileBottomRandomSupport.bottomCardsExiledWithSource(gameData, typedEffect.sourcePermanentId(), null);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
