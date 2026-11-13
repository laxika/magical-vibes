package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a continuation by making its effect a reflexive triggered ability (CR 603.12), which the stack door puts on
 * the stack: its targets are chosen as it goes there (CR 603.3d).
 */
@Component
@RequiredArgsConstructor
public class QueueReflexiveAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return QueueReflexiveAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry, (QueueReflexiveAbilityEffect) effect, record -> { });
    }

    /**
     * Puts the reflexive ability on the stack with what the preceding action of the resolution determined about it
     * set on its record by {@code recordSetup} ("that player", "the tapped creature"), before the ability takes its
     * route: its target is chosen against the record, and the entry that reaches the stack carries it.
     */
    public void resolve(GameData gameData, StackEntry entry, QueueReflexiveAbilityEffect queueEffect,
                        Consumer<StackEntry> recordSetup) {
        // What the ability records is the same whichever route it takes: the resolving entry's X, event, source and
        // "that player". "That much" and "that many" are the event value, read as such, never the ability's X.
        StackEntry record;
        if (queueEffect.aboutPickedPermanent()) {
            // The permanent the ability's effect acts for ("when a creature is put onto the battlefield this way, it
            // deals damage ...") is the ability's source permanent, as it is now: the next permanent an earlier step
            // of this resolution picked, taken off the entry's picks so the next such step is about the one after.
            List<UUID> picks = entry.getResolutionResults().pickedIds();
            if (picks.isEmpty()) {
                return;
            }
            UUID pickedPermanentId = picks.getFirst();
            entry.recordPicks(picks.subList(1, picks.size()));
            record = ReflexiveAbilitySupport.record(gameData, entry, List.of(queueEffect.effect()),
                    entry.getCard().getName() + "'s reflexive ability", pickedPermanentId);
            Permanent reflexiveSource = gameQueryService.findPermanentById(gameData, pickedPermanentId);
            record.setSourcePermanentSnapshot(reflexiveSource == null ? null : new Permanent(reflexiveSource));
        } else {
            record = ReflexiveAbilitySupport.record(gameData, entry, List.of(queueEffect.effect()));
        }
        if (queueEffect.useEntryTargetAsTriggeringPermanent() && entry.getTargetId() != null) {
            record.setTriggeringPermanentId(entry.getTargetId());
        }
        recordSetup.accept(record);
        triggeredAbilityStackService.putMade(gameData, record);
    }
}
