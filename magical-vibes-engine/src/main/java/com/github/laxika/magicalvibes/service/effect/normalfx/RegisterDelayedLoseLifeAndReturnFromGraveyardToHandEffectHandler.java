package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                controllerId, entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), new LoseLifeEffect(e.lifeLoss()),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()),
                entry.getSourcePermanentId(), null, null,
                DelayedReferents.graveyardCard(entry.getCard().getId(), entry.sourceCardGraveyardEntryVersion())));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed lose-life-and-return at next end step for {}",
                gameData.id, playerName, entry.getCard().getName());
    }
}
