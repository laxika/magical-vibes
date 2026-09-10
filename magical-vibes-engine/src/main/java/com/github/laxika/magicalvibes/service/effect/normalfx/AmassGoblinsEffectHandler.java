package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteAmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AmassGoblinsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AmassGoblinsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AmassGoblinsEffect amass = (AmassGoblinsEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, amass.count(),
                AmountContext.forStackEntry(entry, source)));
        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        boolean controlsArmy = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, army, FilterContext.of(gameData).withSourceControllerId(entry.getControllerId())));
        if (!controlsArmy) {
            permanentControlSupport.applyCreateToken(
                    gameData,
                    entry.getControllerId(),
                    new CreateTokenEffect("Goblin Army", 0, 0, CardColor.BLACK,
                            List.of(CardSubtype.GOBLIN, CardSubtype.ARMY), java.util.Set.of(), java.util.Set.of()),
                    1,
                    entry.getCard().getSetCode());
        }

        int index = entry.getEffectsToResolve().indexOf(effect);
        if (index < 0) {
            index = findConditionalWrapperIndex(entry, effect);
        }
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1,
                List.of(new CompleteAmassGoblinsEffect(entry.getControllerId(), count, false)));
    }

    private int findConditionalWrapperIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) instanceof ConditionalEffect conditional
                    && conditional.wrapped() == effect) {
                return i;
            }
        }
        return -1;
    }
}
