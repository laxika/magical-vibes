package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetualEffect = (PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect) effect;
        UUID controllerId = entry.getControllerId();
        Set<UUID> candidateIds = Set.copyOf(perpetualEffect.candidateCardIds());
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        Card selectedCard = hand.stream().filter(card -> candidateIds.contains(card.getId())).findFirst().orElse(null);

        synchronized (gameData.exiledCards) {
            gameData.exiledCards.removeIf(exiled -> controllerId.equals(exiled.ownerId())
                    && candidateIds.contains(exiled.card().getId()));
        }

        if (selectedCard == null) {
            return;
        }

        EnumSet<CardType> additionalTypes = EnumSet.noneOf(CardType.class);
        additionalTypes.addAll(selectedCard.getAdditionalTypes());
        additionalTypes.add(CardType.ARTIFACT);
        selectedCard.setType(CardType.CREATURE);
        selectedCard.setAdditionalTypes(additionalTypes);
        selectedCard.freeze();
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " perpetually makes " + selectedCard.getName() + " an artifact creature."));
    }
}
