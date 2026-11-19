package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUntapPermanentsEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedUntapPermanentsEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUntapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedUntapPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), controllerId, entry.getCard(), entry.getSourcePermanentId(), null,
                new UntapPermanentsEffect(e.filter() == null ? Subjects.controlledBy(Subjects.eachPlayer())
                        : Subjects.controlledBy(Subjects.eachPlayer(), e.filter()), null, e.count())));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed untap up to {} permanents at next end step",
                gameData.id, playerName, e.count());
    }
}
