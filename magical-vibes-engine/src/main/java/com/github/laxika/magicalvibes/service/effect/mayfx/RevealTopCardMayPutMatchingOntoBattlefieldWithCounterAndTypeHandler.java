package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the optional modified battlefield entry for a matching revealed card. */
@Component
@RequiredArgsConstructor
public class RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeHandler
        implements MayEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect effect = ability.effects().stream()
                .filter(RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect.class::isInstance)
                .map(RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect.class::cast)
                .findFirst()
                .orElseThrow();
        List<Card> deck = gameData.playerDecks.get(ability.controllerId());

        if (accepted && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            Permanent permanent = new Permanent(topCard);
            permanent.getPersistentGrantedCardTypes().add(effect.addedCardType());
            if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setCounterCount(effect.counterType(),
                        permanent.getCounterCount(effect.counterType()) + 1);
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ability.controllerId(), permanent);

            UUID enteringControllerId = gameData.playerBattlefields.entrySet().stream()
                    .filter(e -> e.getValue().stream().anyMatch(p -> p.getId().equals(permanent.getId())))
                    .map(java.util.Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (enteringControllerId != null && permanent.getCard().hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, enteringControllerId, permanent.getCard(), null, false);
            }

            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " onto the battlefield."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " leaves the top card on their library."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
