package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        UUID controllerId = entry.getControllerId();
        UUID ownerId = graveyardOwnerId != null ? graveyardOwnerId : controllerId;

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
        exileService.exileCard(gameData, ownerId, targetCard);

        // Grant the controller permission to cast the exiled card this turn, spending mana of any
        // type; and exile it instead of putting it into a graveyard. All expire during cleanup.
        gameData.exilePlayPermissions.put(targetCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(targetCard.getId());
        gameData.exilePlayAnyManaType.add(targetCard.getId());
        gameData.exileInsteadOfGraveyard.add(targetCard.getId());

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + targetCard.getName()
                        + " from an opponent's graveyard (may cast it this turn)."));
    }
}
