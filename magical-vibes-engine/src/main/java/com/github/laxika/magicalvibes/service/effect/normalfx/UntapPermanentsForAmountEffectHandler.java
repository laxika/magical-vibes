package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsForAmountEffect;
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
public class UntapPermanentsForAmountEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapPermanentsForAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var untapEffect = (UntapPermanentsForAmountEffect) effect;
        UUID chooserId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        if (chooserId == null || !gameData.playerIds.contains(chooserId)) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, untapEffect.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<Permanent> candidates = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (untapEffect.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent,
                    untapEffect.filter(), filterContext)) {
                candidates.add(permanent);
            }
        });

        int requiredCount = Math.min(amount, candidates.size());
        if (requiredCount == 0) {
            return;
        }
        if (candidates.size() <= requiredCount) {
            untapAll(gameData, entry, candidates);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, chooserId,
                candidates.stream().map(Permanent::getId).toList(), requiredCount,
                new MultiPermanentChoiceContext.UntapPermanentsForAmount(
                        entry.getCard().getName(), requiredCount),
                entry.getCard().getName() + " — Choose exactly " + requiredCount
                        + " permanent" + (requiredCount == 1 ? "" : "s") + " to untap.");
    }

    private void untapAll(GameData gameData, StackEntry entry, List<Permanent> candidates) {
        int untapped = 0;
        for (Permanent candidate : candidates) {
            if (tapUntapSupport.untapPermanent(gameData, candidate)) {
                untapped++;
            }
        }
        gameLogService.append(gameData, GameLog.text(
                entry.getCard().getName() + " untaps " + untapped + " permanent(s)."));
        log.info("Game {} - {} untaps {} permanent(s)", gameData.id, entry.getCard().getName(), untapped);
    }
}
