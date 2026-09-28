package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardIdPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Kefka, Dancing Mad's end-step graveyard exile and free-cast permission. */
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(opponentId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            Card graveyardCard = graveyard.get(ThreadLocalRandom.current().nextInt(graveyard.size()));
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, graveyardCard.getId());
            Card exiled = graveyardCard;
            if (graveyardCard.getOwnerId() == null) {
                exiled = graveyardCard.createRuntimeCopy();
                exiled.setOwnerId(opponentId);
            }
            exileService.exileCard(gameData, opponentId, exiled);

            if (exiled.hasType(CardType.LAND)) {
                continue;
            }

            UUID cardId = exiled.getId();
            gameData.exilePlayPermissions.put(cardId, controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
            gameData.exilePlayWithoutPayingManaCost.add(cardId);
            gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                    controllerId,
                    null,
                    exiled,
                    null,
                    new StackEntryAllOfPredicate(List.of(
                            new StackEntryCastFromZonePredicate(Zone.EXILE),
                            new StackEntryCardIdPredicate(cardId))),
                    List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.OWNER)),
                    true,
                    false,
                    null,
                    null,
                    null,
                    false,
                    false,
                    gameData.turnNumber));
        }
    }
}
