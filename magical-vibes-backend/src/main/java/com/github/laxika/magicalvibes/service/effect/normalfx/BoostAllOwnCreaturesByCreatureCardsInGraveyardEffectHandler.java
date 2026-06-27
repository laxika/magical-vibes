package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllOwnCreaturesByCreatureCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        // Count creature cards in controller's graveyard
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int x = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    x++;
                }
            }
        }

        if (x == 0) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + " gives +0/+0 (no creature cards in graveyard).");
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + x);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + x);
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + x + "/+" + x + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{} (creature cards in graveyard)", gameData.id, entry.getCard().getName(), count, x, x);
    }
}
