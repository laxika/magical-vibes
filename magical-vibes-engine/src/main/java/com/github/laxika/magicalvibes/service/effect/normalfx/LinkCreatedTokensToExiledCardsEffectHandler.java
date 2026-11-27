package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LinkCreatedTokensToExiledCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link LinkCreatedTokensToExiledCardsEffect}: the exile records of the cards name the first created token
 * still on the battlefield as their source, and every other created token gets that token as its
 * {@link Permanent#getLinkedExileSourceId() exile link}. A copy of one of the tokens is not linked (the Timothar
 * ruling), since the link is the token's own state, not a copiable value.
 */
@Component
@RequiredArgsConstructor
public class LinkCreatedTokensToExiledCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LinkCreatedTokensToExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var link = (LinkCreatedTokensToExiledCardsEffect) effect;
        List<UUID> created = entry.getCreatedPermanentIds();
        if (link.createdFrom() >= created.size()) {
            return;
        }
        List<Permanent> tokens = created.subList(link.createdFrom(), created.size()).stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .toList();
        if (tokens.isEmpty()) {
            return;
        }
        UUID anchorId = tokens.getFirst().getId();
        Set<UUID> exiledIds = Set.copyOf(link.exiledCardIds());
        gameData.exiledCards.replaceAll(exiled -> exiledIds.contains(exiled.card().getId())
                ? new ExiledCardEntry(exiled.card(), exiled.ownerId(), anchorId, exiled.faceDown(),
                exiled.exilerId(), exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(),
                exiled.abilityLink())
                : exiled);
        tokens.stream().skip(1).forEach(token -> token.setLinkedExileSourceId(anchorId));
    }
}
