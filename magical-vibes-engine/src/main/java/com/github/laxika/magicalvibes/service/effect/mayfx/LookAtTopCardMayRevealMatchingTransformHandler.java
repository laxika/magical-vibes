package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingTransformEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Completes a top-card reveal choice and transforms the source when the card matches. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingTransformHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AnimationSupport animationSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingTransformEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayRevealMatchingTransformEffect effect = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealMatchingTransformEffect)
                .map(e -> (LookAtTopCardMayRevealMatchingTransformEffect) e)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(ability.controllerId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.getFirst();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " reveals ", topCard, " from the top of their library."));

            boolean matches = predicateEvaluationService.matchesCardPredicate(
                    topCard, effect.predicate(), ability.sourceCard().getId(), gameData, ability.controllerId());
            if (matches) {
                Permanent source = ability.sourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
                if (source != null && !source.isTransformed()) {
                    animationSupport.transformToBackFace(gameData, source);
                }
            } else {
                log.info("Game {} - {} revealed {} but it does not match, no transform",
                        gameData.id, player.getUsername(), topCard.getName());
            }
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " chooses not to reveal the top card."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
