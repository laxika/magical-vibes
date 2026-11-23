package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.EffectRegistration;
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
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registers the delayed triggered ability with what the exile step did, each card bound to its stay (CR 603.7c), so
 * its effects read that step as {@link RegisterDelayedStepTriggerEffectHandler}'s do; or, for one about a permanent,
 * about the source with that permanent as its entry's triggering permanent, which it doesn't affect once it has left
 * the battlefield (CR 603.7c, 400.7).
 */
@Component
@RequiredArgsConstructor
public class RegisterDelayedEventTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedEventTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var register = (RegisterDelayedEventTriggerEffect) effect;
        if (register.permanent() != null) {
            registerAboutPermanent(gameData, entry, register);
            return;
        }
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
            case SOURCE_LEAVES_OR_UNTAPS, SOURCE_UNTAPS_OR_CONTROL_LOST -> throw new IllegalStateException(
                    "An ability on an untap is about a permanent: " + register);
            case YOU_CAST_EXILED_CARD -> cardIds.forEach(cardId ->
                    gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                            controllerId, entry.getCard(),
                            new TriggeredAbility(Triggers.youCastSpell()
                                    .withStackEntryFilter(new StackEntryCardIdPredicate(cardId)), effects),
                            sourceId).withReferents(referents)));
        }
    }

    /**
     * "When this leaves the battlefield [this turn] / becomes untapped / you lose control of it, [effects] that
     * creature": about the source, "that creature" the permanent the subject names now. None is made once the source has
     * left the battlefield, as none of its events can happen any more (CR 603.7a), nor when the subject names nothing,
     * as it could affect nothing; one made while the source is untapped waits for its next untap (CR 603.7a).
     */
    private void registerAboutPermanent(GameData gameData, StackEntry entry, RegisterDelayedEventTriggerEffect register) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null || gameQueryService.findPermanentById(gameData, sourceId) == null) {
            return;
        }
        List<UUID> about = subjectResolutionService.permanentIds(gameData, entry, register, register.permanent());
        if (about.isEmpty()) {
            return;
        }
        List<TriggerCondition> conditions = new ArrayList<>();
        switch (register.event()) {
            case SOURCE_LEAVES -> conditions.add(new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)));
            case SOURCE_LEAVES_OR_UNTAPS -> {
                conditions.add(new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)));
                conditions.add(new TriggerCondition(Triggers.becomesUntapped()));
            }
            case SOURCE_UNTAPS_OR_CONTROL_LOST -> {
                conditions.add(new TriggerCondition(Triggers.becomesUntapped()));
                conditions.add(new TriggerCondition(Triggers.youLoseControlOfThis()));
                conditions.add(new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)));
            }
            default -> throw new IllegalStateException("Not an event of the source: " + register.event());
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(conditions, TriggerOptions.DEFAULT,
                        register.effects().stream().map(EffectRegistration::new).toList()),
                sourceId, sourceId, register.thisTurn())
                .withReferents(DelayedReferents.NONE.withTriggeringPermanent(about.getFirst())));
    }
}
