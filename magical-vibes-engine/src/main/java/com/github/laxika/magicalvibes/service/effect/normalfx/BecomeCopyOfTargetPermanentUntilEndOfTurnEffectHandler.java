package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetPermanentUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetPermanentUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() < 2) {
            return;
        }

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetIds.get(0));
        Permanent copySource = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (targetPermanent == null || copySource == null) {
            log.info("Game {} - temporary permanent copy target no longer exists", gameData.id);
            return;
        }

        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, targetPermanent);

        String originalName = FaceDownVisibility.publicName(gameData, targetPermanent);
        String copySourceName = FaceDownVisibility.publicName(gameData, copySource);
        BecomeCopyOfTargetPermanentUntilEndOfTurnEffect copyEffect =
                (BecomeCopyOfTargetPermanentUntilEndOfTurnEffect) effect;
        permanentCopierService.applyCloneCopy(
                targetPermanent, copySource, null, null, copyEffect.additionalTypes());
        if (!copyEffect.removedSupertypes().isEmpty()) {
            EnumSet<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(targetPermanent.getCard().getSupertypes());
            supertypes.removeAll(copyEffect.removedSupertypes());
            targetPermanent.getCard().setSupertypes(supertypes);
        }

        PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                targetPermanent, preCopyCard, Duration.UNTIL_END_OF_TURN);

        gameLogService.append(gameData, GameLog.text(
                originalName + " becomes a copy of " + copySourceName + " until end of turn."));
        log.info("Game {} - {} becomes a copy of {} until end of turn",
                gameData.id, originalName, copySourceName);
    }
}
