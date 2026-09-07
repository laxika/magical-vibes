package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutSelectedCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueMayPutSelectedCardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPutSelectedCardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPutSelectedCardOntoBattlefieldEffect typed = (MayPutSelectedCardOntoBattlefieldEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        Card selectedCard = hand.getLast();
        if (!selectedCard.hasType(CardType.CREATURE)
                || selectedCard.getManaValue() > typed.manaValueAtMost()) {
            return;
        }

        gameData.queueMayAbilityForPlayer(
                entry.getCard(),
                entry.getControllerId(),
                new MayEffect(typed, "Put the revealed card onto the battlefield?"),
                selectedCard.getId(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                entry.getSourcePermanentSnapshot());
        playerInputService.processNextMayAbility(gameData);
    }
}
