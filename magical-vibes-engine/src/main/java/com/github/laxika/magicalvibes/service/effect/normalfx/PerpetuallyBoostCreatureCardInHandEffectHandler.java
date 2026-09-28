package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/** Begins the mandatory choice to perpetually modify a creature card in hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCreatureCardInHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostCreatureCardInHandEffect boost =
                (PerpetuallyBoostCreatureCardInHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        List<Integer> creatureIndices = IntStream.range(0, hand.size())
                .filter(index -> hand.get(index).hasType(CardType.CREATURE))
                .boxed()
                .toList();
        if (creatureIndices.isEmpty()) {
            return;
        }

        playerInputService.beginPerpetualCreatureCardChoice(
                gameData, entry.getControllerId(), creatureIndices,
                "Choose a creature card in your hand.", boost.powerBoost(), boost.keywords());
    }
}
