package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves the Undead Alchemist-style milled-creature trigger: exiles the milled card if it is still
 * the same graveyard object, then creates the token for the trigger's controller regardless.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileMilledCreatureAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileMilledCreatureAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var milledEffect = (ExileMilledCreatureAndCreateTokenEffect) effect;
        UUID milledCardId = entry.getTriggeringCardId();
        Card milledCard = gameQueryService.findCardInGraveyardById(gameData, milledCardId);
        long expectedVersion = entry.getTriggeringCardGraveyardEntryVersion();
        if (milledCard != null && (expectedVersion < 0
                || expectedVersion == gameData.graveyardEntryVersion(milledCardId))) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, milledCardId);
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, milledCardId);
            exileService.exileCard(gameData, ownerId, milledCard);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability exiles " + milledCard.getName() + "."));
        }

        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                milledEffect.tokenName(), milledEffect.tokenPower(), milledEffect.tokenToughness(),
                milledEffect.tokenColor(), milledEffect.tokenSubtypes(), Set.of(), Set.of());
        permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s ability creates a " + milledEffect.tokenName() + " creature token."));
        log.info("Game {} - {} creates a {} token from a milled creature trigger",
                gameData.id, entry.getCard().getName(), milledEffect.tokenName());
    }
}
