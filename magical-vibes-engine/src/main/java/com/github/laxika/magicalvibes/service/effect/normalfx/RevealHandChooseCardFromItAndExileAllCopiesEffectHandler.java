package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCardFromItAndExileAllCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealHandChooseCardFromItAndExileAllCopiesEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandChooseCardFromItAndExileAllCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandChooseCardFromItAndExileAllCopiesEffect) effect;
        Predicate<Card> choosable = card -> predicateEvaluationService.matchesCardPredicate(
                card, e.choosableFilter(), null);
        if (gameData.playerHands.getOrDefault(entry.getTargetId(), List.of()).stream().noneMatch(choosable)) {
            LibraryShuffleHelper.shuffleLibrary(gameData, entry.getTargetId());
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getTargetId()) + " shuffles their library."));
            return;
        }

        playerInputService.beginRevealHandChooseCardFromItAndExileAllCopiesChoice(
                gameData, entry.getControllerId(), entry.getTargetId(),
                choosable,
                CardPredicateUtils.describeFilter(e.choosableFilter()), entry.getCard(), e.chooseAnyNumber());
    }
}
