package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.ResolutionResults;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEventTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentReturnedWhenSourceLeftPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardIdPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Registers the delayed triggered ability with what the exile step did, each card bound to its stay (CR 603.7c), so
 * its effects read that step as {@link RegisterDelayedStepTriggerEffectHandler}'s do.
 */
@Component
@RequiredArgsConstructor
public class RegisterDelayedEventTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedEventTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var register = (RegisterDelayedEventTriggerEffect) effect;
        StepOutcome exiled = RegisterDelayedStepTriggerEffectHandler.exiled(gameData,
                entry.getResolutionResults().outcome(StepKind.EXILE));
        if (exiled.objectIds().isEmpty()) {
            return;
        }
        List<UUID> cardIds = exiled.movedCards().stream().map(MovedCard::cardId).distinct().toList();
        DelayedReferents referents = DelayedReferents.cards(cardIds)
                .withStepResults(ResolutionResults.EMPTY.withOutcome(StepKind.EXILE, exiled));
        UUID controllerId = entry.getControllerId();
        UUID sourceId = entry.getSourcePermanentId();
        CardEffect[] effects = register.effects().toArray(CardEffect[]::new);
        switch (register.event()) {
            case SOURCE_LEAVES -> {
                // Once it has left, the event can't happen any more (CR 603.7a).
                if (sourceId != null && gameQueryService.findPermanentById(gameData, sourceId) != null) {
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                            controllerId, entry.getCard(),
                            new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS), effects),
                            sourceId, sourceId, false).withReferents(referents));
                }
            }
            case EXILED_CARD_RETURNS -> {
                if (sourceId != null) {
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                            controllerId, entry.getCard(),
                            new TriggeredAbility(Triggers.enters(
                                    new PermanentReturnedWhenSourceLeftPredicate(sourceId),
                                    ObjectRelation.YOU_CONTROL), effects),
                            null, sourceId, false).withReferents(referents));
                }
            }
            case YOU_CAST_EXILED_CARD -> cardIds.forEach(cardId ->
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                            controllerId, entry.getCard(),
                            new TriggeredAbility(Triggers.youCastSpell()
                                    .withStackEntryFilter(new StackEntryCardIdPredicate(cardId)), effects),
                            sourceId).withReferents(referents)));
        }
    }
}
