package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetArtifactUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Mizzium Transreliquat's temporary artifact copy abilities. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetArtifactUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetArtifactUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        if (sourceId == null || targetId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null) {
            log.info("Game {} - Artifact copy source or target no longer exists", gameData.id);
            return;
        }

        BecomeCopyOfTargetArtifactUntilEndOfTurnEffect copyEffect =
                (BecomeCopyOfTargetArtifactUntilEndOfTurnEffect) effect;
        List<ActivatedAbility> retainedAbilities = copyEffect.retainsAbility()
                ? source.getOriginalCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .filter(ability -> ability.getEffects().stream()
                        .anyMatch(candidate -> candidate instanceof BecomeCopyOfTargetArtifactUntilEndOfTurnEffect retained
                                && retained.retainsAbility()))
                .toList()
                : List.of();

        Card preCopyCard = PermanentCopierService.preCopyCard(gameData, source);
        String originalName = source.getCard().getName();
        String targetName = target.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target.getCard(), null, null, Set.of(),
                retainedAbilities);
        // The second ability's copy doesn't wear off (CR 611.2a); it is newer than the first ability's copies this
        // turn, so as those end it stays a copy of what it targeted (CR 613.7).
        PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), sourceId, entry.getControllerId(),
                source, preCopyCard, copyEffect.retainsAbility() ? Duration.PERMANENT : Duration.UNTIL_END_OF_TURN);
        if (copyEffect.retainsAbility()) {
            gameLogService.append(gameData, GameLog.text(originalName + " becomes a copy of " + targetName + "."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + targetName + " until end of turn."));
        log.info("Game {} - {} becomes a copy of {} until end of turn", gameData.id, originalName, targetName);
    }
}
