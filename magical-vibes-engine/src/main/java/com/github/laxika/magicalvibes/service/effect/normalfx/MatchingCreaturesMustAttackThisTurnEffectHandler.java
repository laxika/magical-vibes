package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingCreaturesMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MatchingCreaturesMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MatchingCreaturesMustAttackThisTurnEffect) effect;
        // A type chosen by this resolution is written in now, so the turn-long rule keeps it (CR 608.2h).
        PermanentPredicate matcher = subjectResolutionService.bindChosenValues(gameData, entry, effect, e.matcher());
        List<UUID> players = List.of();
        if (e.players() != null) {
            players = subjectResolutionService.players(gameData, entry, effect, e.players()).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            if (players.isEmpty()) {
                return;
            }
            matcher = new PermanentAllOfPredicate(List.of(matcher, players.size() == 1
                    ? new PermanentControlledByPlayerPredicate(players.getFirst())
                    : new PermanentAnyOfPredicate(players.stream()
                    .<PermanentPredicate>map(PermanentControlledByPlayerPredicate::new).toList())));
        }

        if (e.lockedAtResolution()) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId())
                    .withSourcePermanentId(entry.getSourcePermanentId());
            PermanentPredicate finalMatcher = matcher;
            final int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                // "if able" is enforced by combat: the must-attack requirement only bites creatures that
                // can legally attack, so setting the flag on ones that can't is harmless.
                if (gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, finalMatcher, filterContext)) {
                    gameData.addEffectOnPermanent(entry, new MustAttackIfAbleEffect(), permanent.getId(), Duration.UNTIL_END_OF_TURN);
                    count[0]++;
                }
            });
            gameLogService.append(gameData,
                    GameLog.text(entry.getCard().getName() + " forces " + count[0]
                            + " creature(s) to attack this turn if able."));
            return;
        }

        // A group requirement doesn't change characteristics or control, so it modifies the game rules
        // and also binds creatures that enter or start matching later this turn (CR 611.2c).
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new MatchingCreaturesMustAttackEffect(matcher), entry.getControllerId(),
                Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), players.isEmpty()
                ? " forces matching creatures to attack this turn if able."
                : " forces creatures " + players.stream().map(gameData.playerIdToName::get)
                .collect(Collectors.joining(" and ")) + " controls to attack this turn if able."));
        log.info("Game {} - {} forces matching creatures to attack this turn if able",
                gameData.id, entry.getCard().getName());
    }
}
