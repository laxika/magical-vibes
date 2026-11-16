package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Component
public class RegisterDelayedReturnCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        // "That card" is the graveyard object the event put there: once it left and came back it is a new object the
        // ability doesn't find (CR 400.7).
        if (cardId == null || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardId))) {
            return;
        }

        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true)),
                entry.getSourcePermanentId(), null, null,
                DelayedReferents.graveyardCard(cardId, gameData.graveyardEntryVersion(cardId))));
    }
}
