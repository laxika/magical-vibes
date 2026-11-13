package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentThenExileMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a non-targeting choice followed by a global permanent exile. */
@Component
@RequiredArgsConstructor
public class ExilePermanentThenExileMatchingPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExilePermanentThenExileMatchingPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExilePermanentThenExileMatchingPermanentsEffect exileEffect =
                (ExilePermanentThenExileMatchingPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> matches(gameData, permanent, entry.getCard(), controllerId,
                        entry.getSourcePermanentId(), exileEffect.choiceFilter()))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (matchingIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context =
                new PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents(
                        entry.getCard(), entry.getSourcePermanentId(), controllerId,
                        exileEffect.choiceFilter(), exileEffect.matchingFilter(),
                        exileEffect.choiceLabel());
        if (matchingIds.size() == 1) {
            exileChosenAndMatching(gameData, matchingIds.getFirst(), context, entry);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds, context,
                entry.getCard().getName() + " — choose a " + exileEffect.choiceLabel() + " to exile.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !matches(gameData, chosen, context.sourceCard(), context.controllerId(),
                        context.sourcePermanentId(), context.choiceFilter())) {
            throw new IllegalStateException("Chosen permanent is no longer a matching permanent you control");
        }
        exileChosenAndMatching(gameData, permanentId, context,
                ReflexiveAbilitySupport.resolvingEntryOf(gameData, context.sourceCard(), context.sourcePermanentId()));
    }

    private void exileChosenAndMatching(GameData gameData, UUID chosenId,
                                        PermanentChoiceContext.ExilePermanentThenExileMatchingPermanents context,
                                        StackEntry resolving) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
        if (chosen == null) {
            return;
        }
        exileSupport.exilePermanentAndLog(gameData, chosen, context.sourceCard().getName());

        // "When you do" is a reflexive trigger (CR 603.12): the matching exile goes on the stack above
        // the resolving ability and resolves separately, so the matching permanents stay on the
        // battlefield until it resolves. It records what the resolving entry was about.
        StackEntry reflexiveEntry = ReflexiveAbilitySupport.recordFor(gameData, resolving, context.sourceCard(),
                context.controllerId(), List.of(new ExileAllPermanentsEffect(context.matchingFilter())),
                context.sourceCard().getName() + "'s reflexive ability", context.sourcePermanentId());
        reflexiveEntry.setNonTargeting(true);
        reflexiveEntry.setReflexiveTrigger(true);
        triggeredAbilityStackService.putMade(gameData, reflexiveEntry);
    }

    private boolean matches(GameData gameData, Permanent permanent, Card sourceCard,
                            UUID controllerId, UUID sourcePermanentId, PermanentPredicate filter) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCard.getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId);
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext);
    }
}
