package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentUntilSourceLeavesEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // The stack entry identifies the source object that created the ability. If that object
        // has already left the battlefield, this effect does nothing: the "until this leaves"
        // duration has already ended. The card-reference fallback is retained for older callers
        // that do not stamp a source permanent id.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? findSourceByCardReference(gameData, entry.getControllerId(), entry.getCard())
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanentId != null && sourcePermanent == null) {
            return;
        }
        if (sourcePermanentId == null && sourcePermanent != null) {
            sourcePermanentId = sourcePermanent.getId();
        }

        if (sourcePermanentId == null) {
            // Source already left the battlefield — exile still happens but no return tracking
            log.info("Game {} - Source permanent for {} no longer on battlefield, exile without return tracking",
                    gameData.id, entry.getCard().getName());
        }

        Card card = target.getOriginalCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);

        permanentRemovalService.removePermanentToExile(gameData, target);

        // Imprint the exiled card onto the source (e.g. Ixalan's Binding)
        if (e.imprint() && sourcePermanent != null) {
            gameData.setImprintedCard(sourcePermanent.getCard(), card);
        }

        
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (sourcePermanentId != null) {
            gameData.addExileReturnOnPermanentLeave(sourcePermanentId, new PendingExileReturn(card, ownerId));

            // Also add source tracking so AllowCastFromCardsExiledWithSourceEffect can find it
            var exiledEntry = gameData.findExiledCard(card.getId());
            if (exiledEntry != null && exiledEntry.sourcePermanentId() == null) {
                gameData.removeFromExile(card.getId());
                gameData.addToExile(ownerId, card, sourcePermanentId);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private Permanent findSourceByCardReference(GameData gameData, UUID controllerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElse(null);
    }
}
