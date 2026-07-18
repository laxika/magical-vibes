package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * End-step resolution for Colfenor's Urn: "if three or more cards have been exiled with this
 * artifact, sacrifice it. If you do, return those cards to the battlefield under their owner's
 * control." The intervening-if (CR 603.4) is re-checked here at resolution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeSelfAndReturnCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndReturnCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndReturnCardsExiledWithSourceEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        // Intervening-if re-check (CR 603.4).
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(en -> sourcePermanentId.equals(en.sourcePermanentId()))
                .toList();
        if (toReturn.size() < e.minCount()) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (self == null || !permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Return every card exiled with it to the battlefield under its owner's control.
        for (ExiledCardEntry exiledEntry : toReturn) {
            Card card = exiledEntry.card();
            UUID ownerId = exiledEntry.ownerId();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }
            Permanent perm = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);
            String logEntry = card.getName() + " returns to the battlefield under "
                    + gameData.playerIdToName.get(ownerId) + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(card).text(" returns to the battlefield under " + gameData.playerIdToName.get(ownerId) + "'s control.").build());
            log.info("Game {} - {} returns from exile via {} (three or more cards exiled)",
                    gameData.id, card.getName(), entry.getCard().getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
        }
    }
}
