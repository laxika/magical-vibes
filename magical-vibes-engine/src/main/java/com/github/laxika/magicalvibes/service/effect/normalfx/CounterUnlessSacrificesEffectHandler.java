package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves ward's "sacrifice a permanent" ransom. */
@Component
@RequiredArgsConstructor
public class CounterUnlessSacrificesEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessSacrificesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessSacrificesEffect sacrificeEffect = (CounterUnlessSacrificesEffect) effect;
        UUID targetCardId = entry.getTargetId();
        StackEntry targetEntry;
        if (targetCardId != null) {
            targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        } else if (entry.getTriggeringCardId() != null) {
            targetCardId = entry.getTriggeringCardId();
            targetEntry = counterSupport.findCounterTargetExcludingSource(gameData, targetCardId, entry);
        } else {
            return;
        }

        if (targetEntry == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetEntry.getControllerId());
        List<Permanent> matchingPermanents = battlefield == null
                ? List.of()
                : battlefield.stream()
                        .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                                gameData, permanent, sacrificeEffect.filter()))
                        .toList();
        if (matchingPermanents.isEmpty()) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        String prompt = "Sacrifice a " + sacrificeEffect.sacrificeDescription() + " to prevent "
                + targetEntry.getCard().getName()
                + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetEntry.getControllerId(),
                List.of(sacrificeEffect), prompt, targetCardId));
    }
}
