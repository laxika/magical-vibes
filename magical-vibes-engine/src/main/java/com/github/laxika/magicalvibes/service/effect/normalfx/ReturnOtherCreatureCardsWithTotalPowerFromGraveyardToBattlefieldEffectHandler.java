package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Lets the controller choose, as the ability resolves, any number of other creature cards from their
 * graveyard whose total power fits under the dying creature's power. The returns themselves happen when the
 * answer to the multi-card graveyard choice arrives (see {@code GraveyardChoiceHandlerService}).
 */
@Component
@RequiredArgsConstructor
public class ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }
        List<Card> candidates = graveyard.stream()
                .filter(card -> card.hasType(CardType.CREATURE) && !card.isToken())
                .filter(card -> e.dyingCardId() == null || !e.dyingCardId().equals(card.getId()))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }
        int maxTotalPower = Math.max(0, entry.getEventValue());
        gameData.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume = true;
        playerInputService.beginMultiGraveyardChoiceWithMaximumPower(gameData, controllerId, candidates,
                candidates.size(), 0, maxTotalPower,
                "Choose any number of other creature cards with total power " + maxTotalPower
                        + " or less from your graveyard.");
    }
}
