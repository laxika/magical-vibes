package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
public class RegisterExileCardsFromOwnGraveyardAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), controllerId, entry.getCard(), entry.getSourcePermanentId(), null,
                new ExileGraveyardCardsEffect(e.count(), GraveyardExileScope.OWN)));
        log.info("Game {} - {} registers delayed graveyard exile of {} card(s) at next end step",
                gameData.id, gameData.playerIdToName.get(controllerId), e.count());
    }
}
