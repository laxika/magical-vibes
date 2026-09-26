package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicatesOfSoughtCardsAndManifestEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConjureDuplicatesOfSoughtCardsAndManifestEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicatesOfSoughtCardsAndManifestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureDuplicatesOfSoughtCardsAndManifestEffect soughtEffect =
                (ConjureDuplicatesOfSoughtCardsAndManifestEffect) effect;
        if (soughtEffect.soughtCards().isEmpty()) {
            return;
        }

        List<Card> duplicates = soughtEffect.soughtCards().stream()
                .map(Card::createConjuredCopy)
                .toList();
        UUID controllerId = entry.getControllerId();
        duplicates.forEach(card -> gameData.addCardToHand(controllerId, card));
        Set<UUID> duplicateIds = duplicates.stream().map(Card::getId).collect(Collectors.toSet());
        gameData.playerHands.get(controllerId).removeIf(card -> duplicateIds.contains(card.getId()));
        manifestService.manifestCards(gameData, controllerId, entry.getCard(), duplicates);
    }
}
