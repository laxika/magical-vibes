package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenLinkedToCardsExiledThisWayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates the token of a {@link CreateTokenLinkedToCardsExiledThisWayEffect} and links the cards the resolution's
 * {@link StepKind#EXILE} step exiled to that token alone: the exile entry remembers the token as its source, so the
 * token's "the exiled card" is that card and a copy of the token returns nothing.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenLinkedToCardsExiledThisWayEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenLinkedToCardsExiledThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenLinkedToCardsExiledThisWayEffect) effect;
        Set<UUID> exiledIds = Set.copyOf(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds());
        List<UUID> tokenIds = permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.token(),
                entry.getCard().getSetCode());
        for (UUID tokenId : tokenIds) {
            gameData.exiledCards.replaceAll(exiled -> exiledIds.contains(exiled.card().getId())
                    ? new ExiledCardEntry(exiled.card(), exiled.ownerId(), tokenId, exiled.faceDown(),
                    exiled.exilerId(), exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(),
                    exiled.abilityLink())
                    : exiled);
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability creates a token."));
    }
}
