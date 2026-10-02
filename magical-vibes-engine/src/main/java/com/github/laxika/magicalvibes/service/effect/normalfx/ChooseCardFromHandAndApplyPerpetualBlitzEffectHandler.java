package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualBlitzEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/** Begins the choice to perpetually grant blitz to a creature card in hand. */
@Component
@RequiredArgsConstructor
public class ChooseCardFromHandAndApplyPerpetualBlitzEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandAndApplyPerpetualBlitzEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = IntStream.range(0, hand.size())
                .filter(index -> isEligible(gameData, entry, hand.get(index)))
                .boxed()
                .toList();
        if (validIndices.isEmpty()) {
            return;
        }

        playerInputService.beginPerpetualCreatureCardChoice(
                gameData, entry.getControllerId(), validIndices,
                "Choose a creature card in your hand. It perpetually gains blitz.",
                0, java.util.Set.of(), true);
    }

    private boolean isEligible(GameData gameData, StackEntry entry, Card card) {
        return card.hasType(CardType.CREATURE)
                && card.getManaCost() != null
                && card.getCastingOption(AlternateHandCast.class)
                        .map(AlternateHandCast::blitz)
                        .orElse(false) == false
                && gameQueryService.findGrantedBlitzAlternateCast(
                        gameData, entry.getControllerId(), card).isEmpty();
    }
}
