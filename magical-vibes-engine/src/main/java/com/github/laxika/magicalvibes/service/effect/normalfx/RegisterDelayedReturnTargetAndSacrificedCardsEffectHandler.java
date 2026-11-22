package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.ResolutionResults;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnTargetAndSacrificedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnTargetAndSacrificedCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnTargetAndSacrificedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> cardIds = new ArrayList<>(2);
        if (entry.getTargetId() != null) {
            cardIds.add(entry.getTargetId());
        }
        if (entry.getSacrificedCardId() != null && !cardIds.contains(entry.getSacrificedCardId())) {
            cardIds.add(entry.getSacrificedCardId());
        }
        // "That card and the sacrificed card": each in the zone and stay it is in now, the sacrificed one also in
        // exile where a replacement put it (its ruling; CR 400.7).
        List<MovedCard> remembered = new ArrayList<>(2);
        for (UUID cardId : cardIds) {
            if (gameQueryService.findCardInGraveyardById(gameData, cardId) != null) {
                remembered.add(gameData.boundToStay(new MovedCard(cardId, cardId, Zone.GRAVEYARD, null)));
            } else if (gameData.findExiledCard(cardId) != null) {
                remembered.add(gameData.boundToStay(new MovedCard(cardId, cardId, Zone.EXILE, null)));
            }
        }
        if (!remembered.isEmpty()) {
            List<UUID> ids = remembered.stream().map(MovedCard::cardId).toList();
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(), new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                            new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                    .underControlOf(Subjects.you())),
                    null, null, null, DelayedReferents.cards(ids).withStepResults(ResolutionResults.EMPTY
                            .withOutcome(StepKind.CHOOSE, StepOutcome.ofObjects(ids, List.of(), remembered)))));
        }
    }
}
