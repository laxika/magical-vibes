package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

        // "Becomes an artifact creature" keeps its other types (CR 205.1b); a change to the physical card, kept in the
        // perpetual stores, not to its copiable values (CR 707.2).
        selectedCard.freeze();
        PerpetuallyGrantCardCharacteristicsEffectHandler.grant(gameData, selectedCard.getId(),
                new PerpetuallyGrantCardCharacteristicsEffect(
                        Set.of(CardType.ARTIFACT, CardType.CREATURE), Set.of(), List.of()));
        // The drafted card is in a hand (CR 400.2): the line doesn't name it.
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " perpetually makes the drafted card an artifact creature."));
    }
}
