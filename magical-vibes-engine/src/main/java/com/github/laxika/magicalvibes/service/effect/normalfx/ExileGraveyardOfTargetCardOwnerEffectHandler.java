package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardOfTargetCardOwnerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileGraveyardOfTargetCardOwnerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileGraveyardOfTargetCardOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        }
        if (targetCardId == null) {
            return;
        }

        UUID graveyardOwnerId = null;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (targetCardId.equals(permanent.getCard().getId())) {
                    graveyardOwnerId = gameData.stolenCreatures.get(permanent.getId());
                    break;
                }
            }
            if (graveyardOwnerId != null) {
                break;
            }
        }
        if (graveyardOwnerId == null) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        List<Card> exiledCards = new ArrayList<>(graveyard);
        graveyard.clear();
        graveyardService.notifyCardsExiledFromGraveyard(gameData, graveyardOwnerId, exiledCards);
        for (Card card : exiledCards) {
            exileService.exileCard(gameData, graveyardOwnerId, card);
        }

        String playerName = gameData.playerIdToName.get(graveyardOwnerId);
        gameLogService.append(gameData, GameLog.text(playerName + "'s graveyard is exiled ("
                + exiledCards.size() + " card" + (exiledCards.size() == 1 ? "" : "s") + ")."));
    }
}
