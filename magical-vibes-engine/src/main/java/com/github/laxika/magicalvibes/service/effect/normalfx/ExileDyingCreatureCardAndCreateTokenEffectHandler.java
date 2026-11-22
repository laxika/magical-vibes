package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the accepted "you may exile that card. If you do, create a token." branch of Unscythe,
 * Killer of Kings' damaged-creature-dies trigger. The dying creature's card id is bound onto the
 * effect at trigger time. The card is exiled only if it is still in a graveyard (a token, or a card
 * that left the graveyard in response, can't be exiled — and then no token is created).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDyingCreatureCardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingCreatureCardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileDyingCreatureCardAndCreateTokenEffect) effect;
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card card = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || card == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (that card is no longer in a graveyard)."));
            log.info("Game {} - {} can't exile dying card {} (not in a graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, card);
        List<UUID> tokenIds = permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                e.token(),
                entry.getCard().getSetCode());
        for (UUID tokenId : tokenIds) {
            // The token's "the exiled card" is linked to this token only: copies of the token must not
            // return it, so the exile entry remembers the token as its source.
            gameData.exiledCards.replaceAll(exiled -> exiled.card().getId().equals(dyingCardId)
                    ? new ExiledCardEntry(exiled.card(), exiled.ownerId(), tokenId, exiled.faceDown(),
                    exiled.exilerId(), exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(),
                    exiled.abilityLink())
                    : exiled);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(),
                "'s ability exiles ", card, " and creates a token."));
        log.info("Game {} - {} exiles {} and creates a token",
                gameData.id, entry.getCard().getName(), card.getName());
    }
}
