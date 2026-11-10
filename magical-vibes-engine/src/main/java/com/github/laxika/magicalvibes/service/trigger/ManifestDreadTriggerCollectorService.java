package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManifestDreadTriggerCollectorService {

    private final GameLogService gameLogService;

    /** "Whenever you manifest dread": "that card", the card put into the graveyard, is the entry's triggering card. */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_MANIFESTS_DREAD)
    private boolean handleManifestDreadDefault(TriggerMatchContext match, TriggerContext context) {
        TriggerContext.ManifestDread manifestDread = (TriggerContext.ManifestDread) context;
        Card sourceCard = match.permanent().getCard();
        Card graveyardCard = manifestDread.cardPutIntoGraveyard();
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        if (graveyardCard != null) {
            // "That card": if it leaves the graveyard before the trigger resolves, it is a new object
            // and the return must not happen (CR 400.7).
            trigger.setTriggeringCard(graveyardCard.getId(),
                    match.gameData().graveyardEntryVersion(graveyardCard.getId()));
        }
        match.gameData().enqueueTrigger(trigger);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on manifest dread", match.gameData().id, sourceCard.getName());
        return true;
    }
}
