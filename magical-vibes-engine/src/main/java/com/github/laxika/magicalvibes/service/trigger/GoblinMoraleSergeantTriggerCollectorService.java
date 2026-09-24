package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedNontokenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects Goblin Morale Sergeant's optional trigger after it enlists a nontoken creature. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoblinMoraleSergeantTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = ConjureDuplicateOfEnlistedNontokenCreatureEffect.class,
            slot = EffectSlot.STATIC)
    private boolean handleConjureTrigger(TriggerMatchContext match,
                                         ConjureDuplicateOfEnlistedNontokenCreatureEffect effect,
                                         TriggerContext context) {
        if (!(context instanceof TriggerContext.AttackingCreatureTriggeredAbility attack)
                || !attack.enlistment()
                || match.permanent() == null
                || !match.permanent().getId().equals(attack.attackingCreature().getId())
                || attack.enlistedCreature() == null
                || attack.enlistedCreature().getCard().isToken()) {
            return false;
        }

        Permanent source = match.permanent();
        Permanent enlisted = attack.enlistedCreature();
        Card snapshot = enlisted.getCard().createRuntimeCopyWithNewId();
        snapshot.freeze();
        MayEffect may = new MayEffect(
                new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(snapshot),
                "Conjure a duplicate of the enlisted creature into the top five cards of your library?");
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s enlist trigger",
                new ArrayList<>(List.of(may)),
                null,
                source.getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} conjure trigger queued after enlisting {}",
                gameData.id, source.getCard().getName(), enlisted.getCard().getName());
        return true;
    }
}
