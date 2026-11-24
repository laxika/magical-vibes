package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect.Otherwise;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardMayPutMatchingOntoBattlefieldEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept → the matching top card goes onto the battlefield, recorded as the resolution's {@code StepKind.RETURN}
 * step; decline → it stays on top, goes into the hand, or may go to the bottom of the library, as the effect says.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayPutMatchingOntoBattlefieldEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayPutMatchingOntoBattlefieldEffect.class::isInstance)
                .map(LookAtTopCardMayPutMatchingOntoBattlefieldEffect.class::cast)
                .findFirst()
                .orElseThrow();
        List<Card> deck = gameData.playerDecks.get(player.getId());

        if (effect.stage() == Stage.MAY_BOTTOM) {
            if (accepted && deck != null && !deck.isEmpty()) {
                deck.addLast(deck.removeFirst());
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " puts the top card of their library on the bottom."));
            } else {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " leaves the top card on their library."));
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (!accepted && effect.otherwise() == Otherwise.MAY_BOTTOM && deck != null && !deck.isEmpty()) {
            gameData.pendingMayAbilities.addFirst(LookAtTopCardMayPutMatchingOntoBattlefieldEffectHandler.mayBottom(
                    ability.sourceCard(), player.getId(), effect, deck.getFirst()));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        mayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice(
                gameData, player, accepted, effect.enterTapped(), effect.otherwise() == Otherwise.HAND);
    }
}
