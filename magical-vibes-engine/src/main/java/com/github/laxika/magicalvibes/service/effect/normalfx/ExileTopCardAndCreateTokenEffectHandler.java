package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.DelayedExiledCardReturns;

/** Resolves Ugin's top-card exile and Spirit-token creation as one linked operation. */
@Component
@RequiredArgsConstructor
public class ExileTopCardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardAndCreateTokenEffect exileAndCreate = (ExileTopCardAndCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Card exiledCard = deck == null || deck.isEmpty() ? null : deck.removeFirst();

        if (exiledCard != null) {
            gameData.addToExile(controllerId, exiledCard, null, true);
            gameData.grantExileLookPermission(exiledCard.getId(), controllerId);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability exiles the top card of its controller's library face down."));
        }

        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, exileAndCreate.token());
        if (exiledCard != null && entry.getCreatedPermanentIds().size() > createdBefore) {
            UUID tokenPermanentId = entry.getCreatedPermanentIds().getLast();
            // "When that token leaves the battlefield, put the exiled card into your hand": a delayed triggered
            // ability about the token, with no stated duration (CR 603.7b).
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                    controllerId, entry.getCard(),
                    new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                            new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE))),
                    tokenPermanentId, entry.getSourcePermanentId(), false)
                    .withReferents(DelayedExiledCardReturns.exiled(gameData, List.of(exiledCard.getId()))));
        }
    }
}
