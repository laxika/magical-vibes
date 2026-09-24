package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalAnyColorManaEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConditionalAnyColorManaEffectHandler implements NormalEffectHandlerBean, ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalAnyColorManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConditionalAnyColorManaEffect conditional = (ConditionalAnyColorManaEffect) effect;
        Permanent source = entry.getSourcePermanentSnapshot();
        int amount = amount(gameData, conditional, ConditionContext.forStackEntry(entry),
                AmountContext.forStackEntry(entry, source));
        AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                entry.getControllerId(), new AwardAnyColorManaEffect(amount), amount, false,
                source == null ? null : source.getChosenSubtype(), source == null ? null : source.getCard(),
                source == null ? null : source.getId(), null, false, false, null, false);
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        ConditionalAnyColorManaEffect conditional = (ConditionalAnyColorManaEffect) effect;
        int amount = amount(gameData, conditional, ConditionContext.forPermanent(permanent, playerId),
                AmountContext.forManaAbility(permanent, playerId, 0)) * manaMultiplier;
        if (amount <= 0) {
            return;
        }
        AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData, playerId,
                new AwardAnyColorManaEffect(amount), amount, creatureSource, permanent.getChosenSubtype(),
                permanent.getCard(), permanent.getId(), null, false, false, null, false);
    }

    @Override
    public int calculateManaProduction(GameData gameData, UUID playerId, Permanent permanent,
                                       CardEffect effect, int xValue) {
        ConditionalAnyColorManaEffect conditional = (ConditionalAnyColorManaEffect) effect;
        return amount(gameData, conditional, ConditionContext.forPermanent(permanent, playerId),
                AmountContext.forManaAbility(permanent, playerId, xValue));
    }

    @Override
    public List<ManaColor> availableManaColors(GameData gameData, UUID playerId, Permanent permanent,
                                               CardEffect effect) {
        return List.copyOf(ManaColor.COLORS);
    }

    @Override
    public boolean isRevertable() {
        return true;
    }

    private int amount(GameData gameData, ConditionalAnyColorManaEffect effect,
                       ConditionContext conditionContext, AmountContext amountContext) {
        var definition = conditionEvaluationService.isMet(gameData, effect.condition(), conditionContext)
                ? effect.ifMetAmount() : effect.ifNotMetAmount();
        return amountEvaluationService.evaluate(gameData, definition, amountContext);
    }
}
