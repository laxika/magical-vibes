package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingFreeCastLifeLoss;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves Kefka, Dancing Mad's end-step graveyard exile, then offers every exiled spell for a free
 * cast during resolution. Each player who owns a spell cast this way loses life equal to its mana
 * value once the casts are finished.
 */
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> castableSpellIds = new ArrayList<>();

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

            if (!exiled.hasType(CardType.LAND)) {
                castableSpellIds.add(exiled.getId());
                gameData.pendingFreeCastLifeLosses.add(new PendingFreeCastLifeLoss(
                        exiled.getId(), opponentId, exiled.getManaValue(), entry.getCard().getName()));
            }
        }

        if (castableSpellIds.isEmpty()) {
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprovisationCapstoneCastChoice(
                controllerId, castableSpellIds, castableSpellIds.size(),
                "Choose any number of exiled spells to cast without paying their mana costs."));
    }
}
