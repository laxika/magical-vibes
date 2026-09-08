package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandler;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConditionalManaEffectHandler implements NormalEffectHandlerBean, ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final AwardManaEffectHandler awardManaEffectHandler;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConditionalManaEffect conditional = (ConditionalManaEffect) effect;
        ManaColor color = selectedColor(gameData, conditional, ConditionContext.forStackEntry(entry));
        awardManaEffectHandler.resolve(gameData, entry, new AwardManaEffect(color, conditional.amount()));
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        ConditionalManaEffect conditional = (ConditionalManaEffect) effect;
        int amount = calculateManaProduction(gameData, playerId, permanent, conditional, 0) * manaMultiplier;
        if (amount <= 0) {
            return;
        }

        ManaColor color = selectedColor(gameData, conditional, ConditionContext.forPermanent(permanent, playerId));
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId, color);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.SNOW)) {
            pool.addSnowMana(effectiveColor, amount);
        } else {
            pool.add(effectiveColor, amount);
        }
        if (predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, new PermanentHasSubtypePredicate(CardSubtype.CAVE))) {
            pool.addCaveManaTag(effectiveColor, amount);
        }
        if (creatureSource) {
            pool.addCreatureMana(effectiveColor, amount);
        }
    }

    @Override
    public int calculateManaProduction(GameData gameData, UUID playerId, Permanent permanent,
                                       CardEffect effect, int xValue) {
        ConditionalManaEffect conditional = (ConditionalManaEffect) effect;
        return amountEvaluationService.evaluate(gameData, conditional.amount(),
                AmountContext.forManaAbility(permanent, playerId, xValue));
    }

    @Override
    public List<ManaColor> availableManaColors(GameData gameData, UUID playerId, Permanent permanent,
                                               CardEffect effect) {
        ConditionalManaEffect conditional = (ConditionalManaEffect) effect;
        return List.of(selectedColor(gameData, conditional, ConditionContext.forPermanent(permanent, playerId)));
    }

    private ManaColor selectedColor(GameData gameData, ConditionalManaEffect effect,
                                    ConditionContext context) {
        return conditionEvaluationService.isMet(gameData, effect.condition(), context)
                ? effect.ifMetColor()
                : effect.ifNotMetColor();
    }
}
