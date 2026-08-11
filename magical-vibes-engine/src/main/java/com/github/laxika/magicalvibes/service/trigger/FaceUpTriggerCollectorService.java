package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers that watch a creature being turned face up. */
@Slf4j
@Service
public class FaceUpTriggerCollectorService {

    private final GameLogService gameLogService;

    public FaceUpTriggerCollectorService(GameLogService gameLogService) {
        this.gameLogService = gameLogService;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP)
    private boolean handleCreatureTurnsFaceUp(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentTurnsFaceUp faceUp = (TriggerContext.PermanentTurnsFaceUp) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                faceUp.turnedPermanent().getId(),
                match.permanent().getId());
        entry.setTriggeringPermanentId(faceUp.turnedPermanent().getId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when {} is turned face up",
                match.gameData().id, sourceCard.getName(), faceUp.turnedPermanent().getCard().getName());
        return true;
    }
}
