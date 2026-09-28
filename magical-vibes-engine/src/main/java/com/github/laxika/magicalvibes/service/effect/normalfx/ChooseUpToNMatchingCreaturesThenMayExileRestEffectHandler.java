package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToNMatchingCreaturesThenMayExileRestEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesExceptChosenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseUpToNMatchingCreaturesThenMayExileRestEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseUpToNMatchingCreaturesThenMayExileRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseUpToNMatchingCreaturesThenMayExileRestEffect choiceEffect =
                (ChooseUpToNMatchingCreaturesThenMayExileRestEffect) effect;
        List<UUID> candidateIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, choiceEffect.choiceFilter())) {
                    candidateIds.add(permanent.getId());
                }
            }
        });

        MultiPermanentChoiceContext context = new MultiPermanentChoiceContext
                .ChooseUpToNMatchingCreaturesThenMayExileRest(
                        entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId());
        if (candidateIds.isEmpty()) {
            queueExileChoice(gameData, entry.getCard(), entry.getControllerId(),
                    entry.getSourcePermanentId(), List.of());
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                candidateIds,
                Math.min(choiceEffect.maxCount(), candidateIds.size()),
                context,
                "Choose up to " + choiceEffect.maxCount() + " " + choiceEffect.choiceName() + ".");
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.ChooseUpToNMatchingCreaturesThenMayExileRest context) {
        queueExileChoice(gameData, context.sourceCard(), context.controllerId(),
                context.sourcePermanentId(), chosenIds);
    }

    private void queueExileChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                  UUID sourcePermanentId, List<UUID> chosenIds) {
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(SequenceEffect.of(
                        new ExileAllCreaturesExceptChosenEffect(chosenIds),
                        new DealDamageToPlayersEffect(13, DamageRecipient.CONTROLLER))),
                "Exile all other creatures? If you do, " + sourceCard.getName()
                        + " deals 13 damage to you.",
                null,
                null,
                sourcePermanentId));
    }
}
