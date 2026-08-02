package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BecomeCopyOfEnteringCreatureEffect) effect;

        // The source permanent (Unstable Shapeshifter) is carried as the stack entry's self-target.
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null) {
            log.info("Game {} - Become-copy-of-entering source no longer on the battlefield", gameData.id);
            return;
        }

        Permanent entering = gameQueryService.findPermanentById(gameData, e.enteringPermanentId());
        if (entering == null) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s ability fizzles (the creature that entered is no longer on the battlefield)."));
            log.info("Game {} - Become-copy-of-entering fizzles, entering permanent gone", gameData.id);
            return;
        }

        String originalName = source.getCard().getName();
        Card enteringCard = entering.getCard();
        permanentCopierService.applyCloneCopy(source, enteringCard, null, null, Set.of());

        // "except it has this ability" — re-grant the source's own enters trigger onto the copy.
        Card copiedCard = source.getCard();
        for (EffectRegistration reg : source.getOriginalCard()
                .getEffectRegistrations(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)) {
            copiedCard.addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, reg.effect(), reg.triggerMode());
        }

        gameLogService.append(gameData, GameLog.textCardText(originalName + " becomes a copy of ", enteringCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, enteringCard.getName());
    }
}
