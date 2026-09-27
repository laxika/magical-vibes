package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Resolves My Laughter Echoes' abandon-and-repeat instruction. */
@Component
@RequiredArgsConstructor
public class AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var repeat = (AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect) effect;
        if (entry.getSourcePermanentId() == null || repeat.schemeSnapshot() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, source.getId()))) {
            return;
        }

        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, source)) {
            return;
        }
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, entry.getControllerId(), source.getCard());
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is abandoned."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        StackEntry schemeSnapshot = repeat.schemeSnapshot();
        StackEntry replay = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                schemeSnapshot.getCard(),
                entry.getControllerId(),
                schemeSnapshot.getDescription(),
                new ArrayList<>(schemeSnapshot.getEffectsToResolve()),
                schemeSnapshot.getTargetId(),
                schemeSnapshot.getSourcePermanentId());
        gameData.enqueueTrigger(replay);
        triggerCollectionService.checkSchemeSetInMotionTriggers(gameData, replay);
    }
}
