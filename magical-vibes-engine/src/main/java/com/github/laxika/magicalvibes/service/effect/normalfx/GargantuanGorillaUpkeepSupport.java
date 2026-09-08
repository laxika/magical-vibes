package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared steps of {@code GargantuanGorillaUpkeepEffect}, used by the resolution handler, the
 * accept/decline handler and the "which Forest" choice completion.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GargantuanGorillaUpkeepSupport {

    private static final PermanentHasSubtypePredicate FOREST = new PermanentHasSubtypePredicate(CardSubtype.FOREST);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;

    /** The Forests {@code controllerId} controls — snow or not, both are legal sacrifices. */
    public List<UUID> forestIds(GameData gameData, UUID controllerId) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, FOREST)) {
                    ids.add(permanent.getId());
                }
            }
        }
        return ids;
    }

    /**
     * "If you sacrifice a snow Forest this way, this creature gains trample until end of turn."
     * Snowness is read before the Forest leaves the battlefield.
     */
    public void sacrificeForest(GameData gameData, UUID controllerId, UUID forestId, UUID sourcePermanentId) {
        Permanent forest = gameQueryService.findPermanentById(gameData, forestId);
        if (forest == null) {
            return;
        }
        boolean snow = gameQueryService.hasEffectiveSupertype(gameData, forest, CardSupertype.SNOW);
        destructionSupport.sacrificeAndLog(gameData, forest, controllerId);
        if (!snow) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }
        // CR 613 layer engine: a one-shot keyword grant is a floating layer-6 effect with its own
        // timestamp; the legacy bucket is kept for direct Permanent.hasKeyword callers.
        source.getGrantedKeywords().add(Keyword.TRAMPLE);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                source.getCard().getName(), null, controllerId,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                source.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " gains trample until end of turn."));
        log.info("Game {} - {} gains trample from a sacrificed snow Forest", gameData.id, source.getCard().getName());
    }

    /** "If you don't sacrifice a Forest, sacrifice this creature and it deals 7 damage to you." */
    public void applyPenalty(GameData gameData, UUID controllerId, UUID sourcePermanentId, Card sourceCard) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null && controllerId.equals(
                gameQueryService.findPermanentController(gameData, sourcePermanentId))) {
            destructionSupport.sacrificeAndLog(gameData, source, controllerId);
        }
        destructionSupport.dealNoncombatDamageToPlayer(gameData, controllerId, 7,
                sourceCard.getName(), sourceCard);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
