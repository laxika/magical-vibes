package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EndStepDrawRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
public class RegisterDrawCardsAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDrawCardsAtNextEndStepEffect) effect;
        UUID drawerId = e.recipient() == EndStepDrawRecipient.TRIGGERING_PLAYER
                ? entry.getTargetId()
                : entry.getControllerId();
        if (drawerId == null) {
            return;
        }
        boolean drawerIsController = drawerId.equals(entry.getControllerId());
        CardEffect draw = drawerIsController ? new DrawCardEffect(e.count())
                : new DrawCardEffect(e.count(), Subjects.targetPlayerDeclaredElsewhere());
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), draw), entry.getSourcePermanentId(), null,
                drawerIsController ? null : drawerId, DelayedReferents.NONE));
        log.info("Game {} - {} registers delayed draw of {} at next end step",
                gameData.id, gameData.playerIdToName.get(drawerId), e.count());
    }
}
