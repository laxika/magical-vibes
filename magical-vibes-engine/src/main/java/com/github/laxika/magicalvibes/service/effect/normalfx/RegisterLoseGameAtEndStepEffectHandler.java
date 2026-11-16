package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
public class RegisterLoseGameAtEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterLoseGameAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Long extraTurnSequence = entry.getCard().getSpellEffects()
                .stream()
                .anyMatch(ControllerExtraTurnEffect.class::isInstance)
                ? gameData.extraTurnSequences.peekFirst()
                : null;
        // "At the beginning of that turn's end step" is the extra turn's own; "at the beginning of your next end step"
        // is the controller's, past the turn the spell was cast in.
        DelayedReferents referents = extraTurnSequence != null
                ? DelayedReferents.NONE.duringExtraTurn(extraTurnSequence)
                : DelayedReferents.NONE.afterTurn(gameData.turnNumber);
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                controllerId, entry.getCard(),
                new TriggeredAbility(Triggers.endStep(extraTurnSequence != null ? ActivePlayer.ANY : ActivePlayer.YOU),
                        new ControllerLosesGameEffect()),
                entry.getSourcePermanentId(), null, null, referents));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers a delayed 'lose the game' at their own next end step",
                gameData.id, playerName);
    }
}
