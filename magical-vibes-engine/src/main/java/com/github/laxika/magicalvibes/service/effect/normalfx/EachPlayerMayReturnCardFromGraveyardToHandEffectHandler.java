package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Queues Minamo's optional graveyard-return choice for each eligible player in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayReturnCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayReturnCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (EachPlayerMayReturnCardFromGraveyardToHandEffect) effect;
        for (UUID playerId : EachPlayerMayScryEffectHandler.apnapPlayers(gameData)) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.stream()
                    .noneMatch(card -> predicateEvaluationService.matchesCardPredicate(card, returnEffect.filter(), null))) {
                continue;
            }

            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    entry.getCard(),
                    playerId,
                    List.of(returnEffect),
                    entry.getCard().getName() + " — You may return a card from your graveyard to your hand."));
        }
    }
}
