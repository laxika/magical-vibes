package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Currency Converter's exiled-card graveyard and token-creation ability. */
@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putEffect = (PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect) effect;
        UUID targetId = entry.getTargetId();
        if (putEffect.chooseAtResolution()) {
            UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId()
                    : entry.getSourcePermanentSnapshot() == null ? null : entry.getSourcePermanentSnapshot().getId();
            List<UUID> available = gameData.orderedPlayerIds.stream()
                    .flatMap(owner -> gameData.getPlayerExiledCards(owner).stream())
                    .map(card -> gameData.findExiledCard(card.getId()))
                    .filter(exiled -> exiled != null && !exiled.faceDown()
                            && sourceId != null && sourceId.equals(exiled.sourcePermanentId()))
                    .map(exiled -> exiled.card().getId()).toList();
            if (available.isEmpty()) return;
            if (available.size() > 1) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCardChoice(
                        entry.getControllerId(), available, entry.getCard().getName(), entry, effect));
                return;
            }
            targetId = available.getFirst();
        }
        putChosenCardIntoGraveyard(gameData, entry, putEffect, targetId);
    }

    public void putChosenCardIntoGraveyard(GameData gameData, StackEntry entry,
            PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect putEffect, UUID targetId) {
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId()) || exiled.faceDown()) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }

        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's graveyard."));

        CreateTokenEffect token = exiled.card().hasType(CardType.LAND)
                ? putEffect.landToken() : putEffect.nonlandToken();
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), token, 1, entry.getCard().getSetCode()));
    }
}
