package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenCreatureTappedForManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Trigger collectors for controller-scoped creature-mana events. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManaTapTriggerCollectorService {

    private final GameLogService gameLogService;

    @CollectsTrigger(value = AddManaWhenCreatureTappedForManaEffect.class,
            slot = EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA)
    private boolean handleAddManaWhenCreatureTappedForMana(TriggerMatchContext match,
            AddManaWhenCreatureTappedForManaEffect trigger, TriggerContext ctx) {
        TriggerContext.CreatureTapForMana creatureTap = (TriggerContext.CreatureTapForMana) ctx;
        match.gameData().playerManaPools.get(creatureTap.tappingPlayerId()).add(trigger.color());
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + match.gameData().playerIdToName.get(creatureTap.tappingPlayerId())
                        + " adds 1 additional " + trigger.color().name().toLowerCase() + " mana."));
        log.info("Game {} - {} triggers on creature tap for mana", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }
}
