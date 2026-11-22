package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Discards the named cards of each hand as one event; what is discarded is added to the step as it happens. */
@Component
@RequiredArgsConstructor
public class DiscardCardsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var discard = (DiscardCardsEffect) effect;
        List<LocatedCard> named = subjectResolutionService.locatedCards(gameData, entry, discard, discard.cards());
        entry.recordStep(StepKind.DISCARD, StepOutcome.NONE);
        Map<UUID, List<Card>> byPlayer = new LinkedHashMap<>();
        for (LocatedCard located : named) {
            if (located.zone() == Zone.HAND) {
                byPlayer.computeIfAbsent(located.ownerId(), ignored -> new ArrayList<>()).add(located.card());
            }
        }
        byPlayer.forEach((playerId, cards) -> {
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            playerInteractionSupport.discardCards(gameData, playerId, cards);
        });
    }
}
