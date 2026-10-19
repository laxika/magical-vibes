package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Stores a global trigger until the beginning of its controller's next turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilNextTurnEffect) effect;
        // "Whenever a creature attacks you or <this permanent>" (Tasha, Unholy Archmage) watches the
        // registering permanent, so attacks on other planeswalkers the player controls don't count.
        UUID watchedPermanentId = registration.slot() == EffectSlot.ON_CREATURE_ATTACKS_YOU
                ? entry.getSourcePermanentId() : null;
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), registration.slot(), registration.triggeredEffect(),
                null, false, true, gameData.turnNumber, null, watchedPermanentId));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until their next turn."));
        log.info("Game {} - {} registers a global {} trigger until their next turn",
                gameData.id, entry.getCard().getName(), registration.slot().name());
    }
}
