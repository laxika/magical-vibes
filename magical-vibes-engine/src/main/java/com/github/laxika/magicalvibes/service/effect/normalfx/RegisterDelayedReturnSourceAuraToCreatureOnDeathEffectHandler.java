package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Registers Ghoulish Impetus's next-end-step Aura return. */
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnSourceAuraToCreatureOnDeathEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId());
        // An Aura that left the graveyard and came back is a new object (CR 400.7).
        if (auraCard == null || entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(auraCard.getId()) != entry.sourceCardGraveyardEntryVersion()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s delayed return fizzles (card not in a graveyard)."));
            return;
        }

        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                        new PutCardsOntoBattlefieldEffect(Subjects.sourceCard()).underControlOf(Subjects.you())),
                entry.getSourcePermanentId(), null, null,
                DelayedReferents.graveyardCard(auraCard.getId(), gameData.graveyardEntryVersion(auraCard.getId()))));
        gameLogService.append(gameData,
                GameLog.cardThen(auraCard, " will return to the battlefield at the beginning of the next end step."));
    }
}
