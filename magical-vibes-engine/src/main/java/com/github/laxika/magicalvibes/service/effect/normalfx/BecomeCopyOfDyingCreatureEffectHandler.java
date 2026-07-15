package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfDyingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfDyingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BecomeCopyOfDyingCreatureEffect) effect;

        // The source permanent (Cemetery Puca) is carried as the stack entry's self-target.
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null) {
            log.info("Game {} - Become-copy-of-dying source no longer on the battlefield", gameData.id);
            return;
        }

        // Copy the dying creature from last-known information — its card sits in a graveyard.
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, e.dyingCardId());
        if (dyingCard == null) {
            String logEntry = source.getCard().getName() + "'s ability fizzles (the creature that died is no longer available to copy).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Become-copy-of-dying fizzles, dying card not found", gameData.id);
            return;
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, dyingCard, null, null, Set.of());

        // "except it has this ability" — re-grant the source's own death-copy trigger onto the copy.
        Card copiedCard = source.getCard();
        for (EffectRegistration reg : source.getOriginalCard().getEffectRegistrations(EffectSlot.ON_ANY_CREATURE_DIES)) {
            copiedCard.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, reg.effect(), reg.triggerMode());
        }

        String logEntry = originalName + " becomes a copy of " + dyingCard.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, dyingCard.getName());
    }
}
