package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMilledCreatureOrLandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles one of Lluwen's resolution-time offers to put a milled creature or land on top. */
@Component
@RequiredArgsConstructor
public class PutMilledCreatureOrLandOnTopOfLibraryHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMilledCreatureOrLandOnTopOfLibraryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID groupId = ability.effects().stream()
                .filter(PutMilledCreatureOrLandOnTopOfLibraryEffect.class::isInstance)
                .map(PutMilledCreatureOrLandOnTopOfLibraryEffect.class::cast)
                .map(PutMilledCreatureOrLandOnTopOfLibraryEffect::groupId)
                .findFirst()
                .orElseThrow();

        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(PutMilledCreatureOrLandOnTopOfLibraryEffect.class::isInstance)
                    .map(PutMilledCreatureOrLandOnTopOfLibraryEffect.class::cast)
                    .anyMatch(marker -> groupId.equals(marker.groupId())));

            Card card = gameQueryService.findCardInGraveyardById(gameData, ability.sourceCard().getId());
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, ability.sourceCard().getId());
            if (card != null && ownerId != null
                    && (card.hasType(CardType.CREATURE) || card.hasType(CardType.LAND))) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                gameData.playerDecks.get(ownerId).addFirst(card);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
