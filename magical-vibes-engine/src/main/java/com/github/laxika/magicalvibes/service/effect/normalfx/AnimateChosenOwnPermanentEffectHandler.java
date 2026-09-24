package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a non-targeting choice of one matching permanent the controller owns. */
@Component
@RequiredArgsConstructor
public class AnimateChosenOwnPermanentEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateChosenOwnPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var animate = (AnimateChosenOwnPermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard == null ? null : sourceCard.getId())
                .withSourceControllerId(controllerId)
                .withXValue(entry.getXValue());
        List<UUID> candidates = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, animate.filter(), filterContext))
                .map(Permanent::getId)
                .toList();

        if (candidates.isEmpty()) {
            if (sourceCard != null) {
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        " resolves but its controller controls no matching permanent."));
            }
            return;
        }
        if (candidates.size() == 1) {
            entry.setChosenPermanentId(candidates.getFirst());
            animationSupport.animateChosen(gameData, entry, animate.animation());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.AnimateChosenOwnPermanent(animate.animation(), controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, candidates,
                sourceCard == null ? "Choose a permanent you control to animate."
                        : sourceCard.getName() + " - Choose a permanent you control to animate.");
    }
}
