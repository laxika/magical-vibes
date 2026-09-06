package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapPermanentsForAmountEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapPermanentsForAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TapPermanentsForAmountEffect tapEffect = (TapPermanentsForAmountEffect) effect;
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, tapEffect.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Permanent> candidates = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.isTapped()
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, tapEffect.filter(), filterContext)) {
                candidates.add(permanent);
            }
        }

        int requiredCount = Math.min(amount, candidates.size());
        if (requiredCount == 0) {
            return;
        }
        if (candidates.size() <= requiredCount) {
            tapAll(gameData, entry, candidates);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, playerId,
                candidates.stream().map(Permanent::getId).toList(), requiredCount,
                new MultiPermanentChoiceContext.TapPermanentsForAmount(
                        entry.getCard().getName(), requiredCount),
                entry.getCard().getName() + " — Choose exactly " + requiredCount
                        + " permanent" + (requiredCount == 1 ? "" : "s") + " to tap.");
    }

    private void tapAll(GameData gameData, StackEntry entry, List<Permanent> candidates) {
        UUID tappingPlayerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        int tapped = 0;
        for (Permanent candidate : candidates) {
            if (tapUntapSupport.tapPermanent(gameData, candidate, tappingPlayerId)) {
                tapped++;
            }
        }
        gameLogService.append(gameData, GameLog.text(
                entry.getCard().getName() + " taps " + tapped + " permanent(s)."));
        log.info("Game {} - {} taps {} permanent(s)", gameData.id, entry.getCard().getName(), tapped);
    }
}
