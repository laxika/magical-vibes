package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndCollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a death trigger that exiles its source before collecting evidence. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceCardFromGraveyardAndCollectEvidenceEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromGraveyardAndCollectEvidenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileAndCollect = (ExileSourceCardFromGraveyardAndCollectEvidenceEffect) effect;
        Card sourceCard = entry.getCard();
        UUID sourceCardId = sourceCard.getId();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        if (ownerId == null || !canCollectEvidence(gameData, entry, exileAndCollect.minimumManaValue(), sourceCardId)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sourceCardId);
        exileService.exileCard(gameData, ownerId, sourceCard);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is exiled."));
        entry.setEventValue(0);

        ConditionalEffect returnIfCollected = ConditionalEffect.unless(
                new EventValueAtLeast(exileAndCollect.minimumManaValue()),
                new ReturnSourceCardFromExileToBattlefieldEffect(exileAndCollect.returnTapped()));
        List<CardEffect> followUp = List.of(
                new CollectEvidenceEffect(exileAndCollect.minimumManaValue()),
                returnIfCollected);
        insertFollowUp(entry, effect, followUp);
        log.info("Game {} - {} exiled itself to collect evidence {}",
                gameData.id, sourceCard.getName(), exileAndCollect.minimumManaValue());
    }

    private boolean canCollectEvidence(GameData gameData, StackEntry entry, int minimumManaValue,
                                       UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (minimumManaValue == 0) {
            return true;
        }
        return graveyard != null && graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .mapToInt(Card::getManaValue)
                .sum() >= minimumManaValue;
    }

    private void insertFollowUp(StackEntry entry, CardEffect effect, List<CardEffect> followUp) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            CardEffect current = effects.get(i);
            if (current.equals(effect)
                    || current instanceof MayEffect may && may.wrapped().equals(effect)) {
                entry.insertEffectsToResolve(i + 1, followUp);
                return;
            }
        }
        throw new IllegalStateException("Could not locate exile-and-collect effect on stack entry");
    }
}
