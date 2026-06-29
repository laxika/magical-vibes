package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerCardsInControllerGraveyardEffect;
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
public class BoostTargetCreaturePerCardsInControllerGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreaturePerCardsInControllerGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreaturePerCardsInControllerGraveyardEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) {
                    continue;
                }
                if (gameQueryService.matchesCardPredicate(card, boost.filter(), null, gameData, controllerId)) {
                    count++;
                }
            }
        }

        int powerBoost = boost.basePower() + count * boost.powerPerCard();
        int toughnessBoost = boost.baseToughness() + count * boost.toughnessPerCard();
        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = target.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + count + " matching card(s) in graveyard).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} graveyard card(s)",
                gameData.id, target.getCard().getName(), powerBoost, toughnessBoost, count);
    }
}
