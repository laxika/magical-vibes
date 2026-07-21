package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ExileTargetCreatureCardCreateTokensEqualToToughnessEffect} (Morbid Bloom):
 * exile the single graveyard target, then create X copies of the token template where X is the
 * exiled card's toughness. An illegal target is already caught by the whole-spell fizzle check
 * before this runs (single-target spell), but the null guard keeps the handler self-contained.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardCreateTokensEqualToToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardCreateTokensEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCreatureCardCreateTokensEqualToToughnessEffect) effect;

        UUID targetCardId = entry.getTargetId();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        // Printed toughness of the card in the graveyard (null / negative clamped to 0), captured
        // before it leaves the graveyard.
        int toughness = targetCard.getToughness() == null ? 0 : Math.max(0, targetCard.getToughness());

        graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard);

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.textCardText(playerName + " exiles ", targetCard, " from a graveyard."));

        if (toughness > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), e.tokenTemplate(), toughness,
                    entry.getCard().getSetCode()));
        }
    }
}
