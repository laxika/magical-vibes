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
 * (Possibility Storm). The rest stay exiled until the cast is complete, including its target choice
 * (the bottoming is deferred through {@code GameData.deferredExileBottomSourceIds}); declining bottoms
 * them at once, and so does a chosen card that turns out to be uncastable.
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
            gameData.deferredExileBottomSourceIds.add(typedEffect.sourcePermanentId());
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, ability.targetCardId());
            // A card that could not be cast (no legal target) is still among the cards exiled with the
            // source, so it goes to the bottom with the rest.
            inputCompletionService.flushDeferredExileBottoms(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        exileBottomRandomSupport.bottomCardsExiledWithSource(gameData, typedEffect.sourcePermanentId(), null);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
