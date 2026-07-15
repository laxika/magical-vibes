package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CastTargetInstantOrSorceryFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastTargetInstantOrSorceryFromGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();

        // Get the targeted card ID from targetCardIds (set at ETB trigger time)
        // Target card ID comes from targetCardIds (set at ETB trigger time) or, when cast as a
        // spell (e.g. Memory Plunder), from the single spell target.
        UUID targetCardId = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target no longer in graveyard)."));
            return;
        }

        // Verify target is still in a graveyard matching the scope
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target not in any graveyard)."));
            return;
        }
        boolean validScope = switch (e.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(controllerId);
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(controllerId);
            case ALL_GRAVEYARDS -> true;
        };
        if (!validScope) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target not in a valid graveyard)."));
            return;
        }

        // Verify target is still an instant or sorcery
        if (!targetCard.hasType(CardType.INSTANT) && !targetCard.hasType(CardType.SORCERY)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (target is not an instant or sorcery)."));
            return;
        }

        // Queue may-cast choice
        String prompt = e.withoutPayingManaCost()
                ? entry.getCard().getName() + " — Cast " + targetCard.getName() + " without paying its mana cost?"
                : entry.getCard().getName() + " — Cast " + targetCard.getName() + "?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard,
                controllerId,
                List.of(e),
                prompt
        ));
    }
}
