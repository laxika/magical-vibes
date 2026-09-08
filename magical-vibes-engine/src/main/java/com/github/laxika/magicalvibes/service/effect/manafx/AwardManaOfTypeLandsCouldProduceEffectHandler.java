package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LandManaTypeSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AwardManaOfTypeLandsCouldProduceEffectHandler implements ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LandManaTypeSupport landManaTypeSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfTypeLandsCouldProduceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect cardEffect, int manaMultiplier, boolean creatureSource) {
        var effect = (AwardManaOfTypeLandsCouldProduceEffect) cardEffect;

        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, effect.amount(),
                AmountContext.forManaAbility(permanent, playerId))) * manaMultiplier;
        if (amount == 0) {
            return;
        }

        Set<ManaColor> availableTypes = collectAvailableTypes(gameData, playerId, effect);
        if (availableTypes.isEmpty()) {
            return;
        }

        if (availableTypes.size() == 1) {
            addMana(gameData.playerManaPools.get(playerId), availableTypes.iterator().next(), amount,
                    creatureSource);
            return;
        }

        ChoiceContext.ManaColorChoice context = new ChoiceContext.ManaColorChoice(
                playerId, creatureSource, amount);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context,
                availableTypes.stream().map(Enum::name).sorted().toList(),
                "Choose a type of mana to add."));
    }

    @Override
    public boolean isRevertable() {
        return true;
    }

    private Set<ManaColor> collectAvailableTypes(GameData gameData, UUID playerId,
                                                  AwardManaOfTypeLandsCouldProduceEffect effect) {
        Set<ManaColor> availableTypes = EnumSet.noneOf(ManaColor.class);
        for (UUID ownerId : gameData.orderedPlayerIds) {
            boolean isController = ownerId.equals(playerId);
            if (effect.scope() == ManaColorLandScope.CONTROLLER ? !isController : isController) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent land : battlefield) {
                if (!land.getCard().hasType(CardType.LAND)
                        || !predicateEvaluationService.matchesPermanentPredicate(
                                gameData, land, effect.landPredicate())) {
                    continue;
                }
                availableTypes.addAll(landManaTypeSupport.manaTypesCouldProduce(gameData, land));
            }
        }
        return availableTypes;
    }

    private static void addMana(ManaPool pool, ManaColor color, int amount, boolean creatureSource) {
        pool.add(color, amount);
        if (creatureSource) {
            pool.addCreatureMana(color, amount);
        }
    }
}
