package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromTargetGraveyardThenManifestEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExileCreaturesFromTargetGraveyardThenManifestEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final ManifestService manifestService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesFromTargetGraveyardThenManifestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getTargetId());
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        List<Card> creatureCards = graveyard.stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }

        graveyard.removeAll(creatureCards);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, entry.getTargetId(), creatureCards);
        for (Card card : creatureCards) {
            exileService.exileCardFaceDown(gameData, entry.getTargetId(), card, null);
        }

        List<Card> shuffledPile = new ArrayList<>(creatureCards);
        Collections.shuffle(shuffledPile);
        manifestService.manifestExiledCards(gameData, entry.getControllerId(), entry.getCard(), shuffledPile);
    }
}
