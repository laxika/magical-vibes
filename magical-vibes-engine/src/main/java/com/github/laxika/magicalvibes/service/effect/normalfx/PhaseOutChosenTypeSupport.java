package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared Teferi's Realm resolve path: map the chosen type string to a nontoken permanent filter and
 * phase every match out.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseOutChosenTypeSupport {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PhasingService phasingService;
    private final GameLogService gameLogService;

    /**
     * Phases out every nontoken permanent matching {@code typeName} ("ARTIFACT", "CREATURE",
     * "LAND", or "NON_AURA_ENCHANTMENT").
     */
    public void phaseOutChosenType(GameData gameData, Card sourceCard, String typeName) {
        PermanentPredicate filter = filterFor(typeName);
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard != null ? sourceCard.getId() : null);

        List<Permanent> toPhaseOut = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                    toPhaseOut.add(permanent);
                }
            }
        });

        if (toPhaseOut.isEmpty()) {
            return;
        }

        phasingService.phaseOut(gameData, toPhaseOut);
        gameLogService.append(gameData, GameLog.builder()
                .card(sourceCard)
                .text(String.format(" phases out %d permanent(s).", toPhaseOut.size()))
                .build());
        log.info("Game {} - {} phases out {} permanents of type {}", gameData.id,
                sourceCard != null ? sourceCard.getName() : "effect", toPhaseOut.size(), typeName);
    }

    static PermanentPredicate filterFor(String typeName) {
        PermanentPredicate typeFilter = switch (typeName) {
            case "ARTIFACT" -> new PermanentIsArtifactPredicate();
            case "CREATURE" -> new PermanentIsCreaturePredicate();
            case "LAND" -> new PermanentIsLandPredicate();
            case "NON_AURA_ENCHANTMENT" -> new PermanentAllOfPredicate(List.of(
                    new PermanentIsEnchantmentPredicate(),
                    new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA))));
            default -> throw new IllegalArgumentException("Invalid Teferi's Realm type choice: " + typeName);
        };
        return new PermanentAllOfPredicate(List.of(
                typeFilter,
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
