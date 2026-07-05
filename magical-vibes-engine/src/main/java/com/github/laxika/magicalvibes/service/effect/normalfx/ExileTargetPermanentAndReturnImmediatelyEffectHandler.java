package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnImmediatelyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndReturnImmediatelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndReturnImmediatelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentAndReturnImmediatelyEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        Card card = target.getOriginalCard();
        boolean hadBonusSubtype = e.bonusSubtype() != null
                && card.getSubtypes().contains(e.bonusSubtype());

        // Exile the permanent
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Immediately return from exile as a new permanent
        gameData.removeFromExile(card.getId());
        Permanent returned = new Permanent(card);
        if (e.plusOnePlusOneCountersOnReturn() > 0
                && !gameQueryService.cantHaveCounters(gameData, returned)) {
            returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, e.plusOnePlusOneCountersOnReturn());
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, returned);

        String logEntry = card.getName() + " is exiled by " + entry.getCard().getName()
                + " and returns to the battlefield under " + gameData.playerIdToName.get(ownerId) + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} flickers {} (immediate return)", gameData.id, entry.getCard().getName(), card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);

        // Apply bonus if the exiled permanent had the required subtype
        if (hadBonusSubtype && e.bonusEffect() instanceof DrawCardEffect drawEffect) {
            int drawAmount = amountEvaluationService.evaluate(gameData, drawEffect.amount(),
                    AmountContext.forStackEntry(entry, null));
            for (int i = 0; i < drawAmount; i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            String drawLog = gameData.playerIdToName.get(entry.getControllerId())
                    + " draws a card (" + card.getName() + " was a " + e.bonusSubtype().getDisplayName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, drawLog);
        }
    }
}
