package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RevealTopCardMayPutMatchingOntoBattlefieldOrBottomHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final MayMiscHandlerService mayMiscHandlerService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect effect = ability.effects().stream()
                .filter(RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class::isInstance)
                .map(RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (deck == null || deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.stage() == RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.Stage.MAY_REVEAL) {
            if (!accepted) {
                gameLogService.append(gameData,
                        GameLog.text(player.getUsername() + " declines to reveal the top card of their library."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            Card topCard = deck.getFirst();
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " reveals ", topCard, " from the top of their library."));
            boolean matches = predicateEvaluationService.matchesCardPredicate(
                    topCard, effect.predicate(), ability.sourceCard().getId());
            if (!matches) {
                mayMiscHandlerService.handleRevealTopCardMayBottomChoice(gameData, player, true);
                return;
            }

            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    ability.sourceCard(), player.getId(),
                    List.of(effect.withStage(RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.Stage.MAY_PUT)),
                    ability.sourceCard().getName() + " — Put " + topCard.getName() + " onto the battlefield?"));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted) {
            mayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice(gameData, player, true);
        } else {
            mayMiscHandlerService.handleRevealTopCardMayBottomChoice(gameData, player, true);
        }
    }
}
