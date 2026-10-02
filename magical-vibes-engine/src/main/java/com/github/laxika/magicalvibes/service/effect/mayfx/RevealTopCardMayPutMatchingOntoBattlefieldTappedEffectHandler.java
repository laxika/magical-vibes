package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the optional reveal and puts a matching card onto the battlefield tapped. */
@Component
@RequiredArgsConstructor
public class RevealTopCardMayPutMatchingOntoBattlefieldTappedEffectHandler
        implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect effect = ability.effects().stream()
                .filter(RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect.class::isInstance)
                .map(RevealTopCardMayPutMatchingOntoBattlefieldTappedEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return;
        }

        UUID controllerId = ability.controllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (!accepted) {
            gameLogService.append(gameData,
                    GameLog.text(player.getUsername() + " chooses not to reveal the top card."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (library == null || library.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " reveals ", topCard, " from the top of their library."));

        if (!predicateEvaluationService.matchesCardPredicate(
                topCard, effect.predicate(), ability.sourceCard().getId(), gameData, controllerId)) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" remains on top of " + player.getUsername() + "'s library.")
                    .build());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        library.removeFirst();
        Permanent permanent = new Permanent(topCard, Zone.LIBRARY);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        battlefieldEntryService.processLandETBEffects(gameData, controllerId, topCard);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, player.getUsername()));

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
