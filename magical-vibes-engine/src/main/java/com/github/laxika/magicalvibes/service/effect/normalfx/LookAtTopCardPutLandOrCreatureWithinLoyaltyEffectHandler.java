package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Nissa, Steward of Elements 0: look at the top card of your library. If it's a land card or a
 * creature card with mana value less than or equal to the number of loyalty counters on Nissa, you
 * may put that card onto the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardPutLandOrCreatureWithinLoyaltyEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // "Look at" is private, so the card's identity is not broadcast to opponents.
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();

        int loyalty = 0;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            loyalty = source.getCounterCount(CounterType.LOYALTY);
        }

        boolean eligible = topCard.hasType(CardType.LAND)
                || (topCard.hasType(CardType.CREATURE) && topCard.getManaValue() <= loyalty);

        if (!eligible) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + " leaves the top card on their library (" + sourceName + ")."));
            return;
        }

        // Eligible — offer to put it onto the battlefield (the mayfx handler completes on accept).
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect()),
                sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
        ));
    }
}
