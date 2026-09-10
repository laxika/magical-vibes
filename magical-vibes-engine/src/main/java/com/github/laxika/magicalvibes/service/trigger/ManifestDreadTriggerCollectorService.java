package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
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

    @CollectsTrigger(value = ReturnTriggeringCardToOwnerHandEffect.class,
            slot = EffectSlot.ON_CONTROLLER_MANIFESTS_DREAD)
    private boolean handleReturnToHand(TriggerMatchContext match,
                                       ReturnTriggeringCardToOwnerHandEffect effect,
                                       TriggerContext context) {
        TriggerContext.ManifestDread manifestDread = (TriggerContext.ManifestDread) context;
        Card sourceCard = match.permanent().getCard();
        Card graveyardCard = manifestDread.cardPutIntoGraveyard();
        ReturnTriggeringCardToOwnerHandEffect baked = new ReturnTriggeringCardToOwnerHandEffect(
                graveyardCard == null ? null : graveyardCard.getId(), manifestDread.manifestingPlayerId());
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(baked)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on manifest dread", match.gameData().id, sourceCard.getName());
        return true;
    }
}
