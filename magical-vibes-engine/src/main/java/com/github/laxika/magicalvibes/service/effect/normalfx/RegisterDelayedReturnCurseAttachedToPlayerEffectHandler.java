package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Registers Lynde's next-end-step return of a Curse attached to Lynde's controller. */
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnCurseAttachedToPlayerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnCurseAttachedToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The Curse is the card the trigger event put into a graveyard, as long as it is still that object (CR 400.7);
        // "attached to you" is the ability's controller.
        UUID cardId = entry.getTriggeringCardId();
        UUID attachedPlayerId = entry.getControllerId();
        if (cardId == null || attachedPlayerId == null || !gameData.playerIds.contains(attachedPlayerId)
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardId))
                || gameData.playerGraveyards.values().stream()
                .noneMatch(graveyard -> graveyard.stream().anyMatch(card -> card.getId().equals(cardId)))) {
            return;
        }

        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                attachedPlayerId, entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                        new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                                .underControlOf(Subjects.you())
                                .attachedTo(Subjects.you())),
                entry.getSourcePermanentId(), null, null,
                DelayedReferents.graveyardCard(cardId, gameData.graveyardEntryVersion(cardId))));
    }
}
