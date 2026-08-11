package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayExileCardFromGraveyardEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TargetPlayerMayExileCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerMayExileCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameData.queueMayAbility(entry.getCard(), controllerId,
                    new MayEffect(new DrawCardEffect(), "Draw a card?"));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetPlayerId,
                List.of(new TargetPlayerMayExileCardFromGraveyardEffect()),
                entry.getCard().getName() + " - Exile a card from your graveyard?",
                null,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                null,
                null,
                null,
                null,
                controllerId));
    }
}
