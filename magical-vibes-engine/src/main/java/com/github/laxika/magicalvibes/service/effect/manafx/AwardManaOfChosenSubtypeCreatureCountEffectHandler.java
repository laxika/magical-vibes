package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenSubtypeCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AwardManaOfChosenSubtypeCreatureCountEffectHandler implements ManaAbilityEffectHandler {

    private static final List<String> COLORS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");

    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public AwardManaOfChosenSubtypeCreatureCountEffectHandler(
            AmountEvaluationService amountEvaluationService,
            InteractionHandlerRegistry interactionHandlerRegistry) {
        this.amountEvaluationService = amountEvaluationService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfChosenSubtypeCreatureCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        int amount = amountEvaluationService.evaluate(gameData,
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSourceChosenSubtypePredicate())), CountScope.CONTROLLER),
                AmountContext.forManaAbility(permanent, playerId)) * manaMultiplier;
        ChoiceContext.ManaColorChoice choiceContext =
                new ChoiceContext.ManaColorChoice(playerId, creatureSource, amount);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, COLORS, "Choose a color of mana to add."));
    }

    @Override
    public boolean isRevertable() {
        return true;
    }
}
