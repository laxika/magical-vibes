package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalSurveilCardsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveilEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SurveilEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SurveilEffect e = (SurveilEffect) effect;

        if (e.applyAdditionalChoices()) {
            List<Integer> additionalCounts = additionalSurveilCounts(gameData, entry.getControllerId());
            List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
            if (e.count() > 0 && !additionalCounts.isEmpty() && deck != null && deck.size() > e.count()) {
                int effectIndex = entry.getEffectsToResolve().indexOf(effect);
                entry.insertEffectsToResolve(effectIndex + 1,
                        List.of(buildAdditionalSurveilChoices(additionalCounts, 0, e.count())));
                return;
            }
        }

        resolveSurveil(gameData, entry, e);
    }

    private void resolveSurveil(GameData gameData, StackEntry entry, SurveilEffect e) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        // Surveil 0 (or fewer): no surveil event occurs.
        if (e.count() <= 0) {
            return;
        }

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + " surveil).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            triggerCollectionService.checkSurveilTriggers(gameData, controllerId);
            return;
        }

        // Surveil 2+ uses the top-of-library / graveyard split interaction (shared with scry).
        // Surveil 1 stays a single "put the top card into your graveyard?" may-ability.
        if (e.count() > 1) {
            int count = Math.min(e.count(), deck.size());
            List<Card> topCards = new ArrayList<>(deck.subList(0, count));
            deck.subList(0, count).clear();

            String logEntry = playerName + " surveils " + count + " (" + sourceName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} surveils {} ({})", gameData.id, playerName, count, sourceName);
            triggerCollectionService.checkSurveilTriggers(gameData, controllerId);

            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.Scry(controllerId, topCards, true));
            return;
        }

        Card topCard = deck.getFirst();

        String logEntry = playerName + " surveils 1 (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} surveils 1, top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);
        triggerCollectionService.checkSurveilTriggers(gameData, controllerId);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e),
                sourceName + " — Put " + topCard.getName() + " into your graveyard?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }

    private List<Integer> additionalSurveilCounts(GameData gameData, UUID controllerId) {
        List<Integer> counts = new ArrayList<>();
        List<com.github.laxika.magicalvibes.model.Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return counts;
        }
        for (var permanent : battlefield) {
            for (CardEffect staticEffect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (staticEffect instanceof AdditionalSurveilCardsEffect additional && additional.amount() > 0) {
                    counts.add(additional.amount());
                }
            }
        }
        return counts;
    }

    private CardEffect buildAdditionalSurveilChoices(List<Integer> additionalCounts, int index, int count) {
        if (index >= additionalCounts.size()) {
            return new SurveilEffect(count, false);
        }

        int additionalCount = additionalCounts.get(index);
        String cardText = additionalCount == 1 ? "card" : "cards";
        return new MayEffect(
                buildAdditionalSurveilChoices(additionalCounts, index + 1, count + additionalCount),
                "Look at " + additionalCount + " additional " + cardText + " with Enhanced Surveillance?",
                buildAdditionalSurveilChoices(additionalCounts, index + 1, count)
        );
    }
}
