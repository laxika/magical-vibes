package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mayLookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffectHandler")
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffectHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class::isInstance)
                .map(LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (effect.stage() == LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.Stage.MAY_PUT) {
            if (accepted) {
                mayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice(
                        gameData, player, true, effect.enterTapped());
            } else {
                queueBottomChoice(gameData, ability, effect);
            }
            return;
        }

        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (accepted && deck != null && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            deck.add(topCard);
            gameLogService.append(gameData,
                    GameLog.text(player.getUsername() + " puts the top card of their library on the bottom."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void queueBottomChoice(GameData gameData, PendingMayAbility ability,
                                   LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect effect) {
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                ability.sourceCard(),
                ability.controllerId(),
                List.of(effect.withStage(
                        LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect.Stage.MAY_BOTTOM)),
                ability.sourceCard().getName() + " — Put the top card of your library on the bottom?"));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
