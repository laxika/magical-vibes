package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndBoostSelfByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndBoostSelfByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String cardName = entry.getCard().getName();
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = targetPlayerName + "'s library is empty — " + cardName + "'s ability does nothing.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        // Snapshot the top card before milling to get its mana value
        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();

        // Mill one card
        graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);

        // Boost the source creature
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        if (manaValue > 0) {
            self.setPowerModifier(self.getPowerModifier() + manaValue);
            self.setToughnessModifier(self.getToughnessModifier() + manaValue);
        }

        String logEntry = cardName + " gets +" + manaValue + "/+" + manaValue
                + " until end of turn (milled " + topCard.getName() + ", mana value " + manaValue + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets +{}/+{} from milling {}", gameData.id, cardName, manaValue, manaValue, topCard.getName());
    }
}
