package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteAmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenAmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureThenAmassGoblinsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureThenAmassGoblinsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, target);
        boolean drawCard = targetControllerId.equals(entry.getControllerId());
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

        PermanentPredicate army = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ARMY)));
        boolean controlsArmy = gameData.playerBattlefields.getOrDefault(targetControllerId, List.of()).stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, army, FilterContext.of(gameData).withSourceControllerId(targetControllerId)));

        if (!controlsArmy) {
            permanentControlSupport.applyCreateToken(
                    gameData,
                    targetControllerId,
                    new CreateTokenEffect("Goblin Army", 0, 0, CardColor.BLACK,
                            List.of(CardSubtype.GOBLIN, CardSubtype.ARMY), java.util.Set.of(), java.util.Set.of()),
                    1,
                    entry.getCard().getSetCode());
        }

        insertAfterCurrent(entry, effect,
                new CompleteAmassGoblinsEffect(targetControllerId, power, drawCard));
    }

    private void insertAfterCurrent(StackEntry entry, CardEffect current, CardEffect next) {
        int index = entry.getEffectsToResolve().indexOf(current);
        if (index < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(index + 1, List.of(next));
    }
}
