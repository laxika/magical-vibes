package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a graveyard return whose target was selected by an opponent. */
@Component
@RequiredArgsConstructor
public class ReturnCardFromGraveyardToHandOfOpponentsChoiceEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final ReturnCardFromGraveyardEffectHandler returnCardFromGraveyardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect choiceEffect =
                (ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect) effect;
        returnCardFromGraveyardEffectHandler.resolve(gameData, entry,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(com.github.laxika.magicalvibes.model.GraveyardChoiceDestination.HAND)
                        .filter(choiceEffect.filter())
                        .targetGraveyard(true)
                        .build());
    }

    public void beginCardChoice(GameData gameData, UUID opponentId, List<Card> matchingCards,
                                String sourceCardName) {
        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, matchingCards, 1, 1,
                sourceCardName + "'s ability — choose a creature card from its controller's graveyard.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
                                       PermanentChoiceContext.MausoleumTurnkeyOpponentChoice context) {
        beginCardChoice(gameData, opponentId, context.matchingCards(), context.sourceCard().getName());
    }
}
