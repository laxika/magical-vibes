package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardCreatureToBattlefieldEffect typed =
                (RevealTopCardCreatureToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (!topCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" remains on top of " + playerName + "'s library (" + sourceName + ").")
                    .build());
            return;
        }

        deck.removeFirst();
        Permanent permanent = new Permanent(topCard);
        if (typed.grantHaste()) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        gameLogService.append(gameData, GameLog.builder()
                .card(topCard)
                .text(" enters the battlefield under " + playerName + "'s control (" + sourceName + ").")
                .build());

        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, topCard, null, false);

        if (typed.sacrificeAtEndStep()) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }

        log.info("Game {} - {} puts {} onto the battlefield ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}
