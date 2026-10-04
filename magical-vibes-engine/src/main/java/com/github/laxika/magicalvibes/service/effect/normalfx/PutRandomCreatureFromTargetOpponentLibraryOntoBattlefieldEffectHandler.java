package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Better Offer's random targeted-library creature entry. */
@Component
@RequiredArgsConstructor
public class PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var betterOffer = (PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || controllerId.equals(targetPlayerId)) {
            return;
        }

        int powerAndToughness = Math.max(0, amountEvaluationService.evaluate(
                gameData, betterOffer.powerAndToughness(), AmountContext.forStackEntry(entry, null)));
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> candidates = new ArrayList<>(library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> card.getManaValue() <= powerAndToughness)
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList());
        if (candidates.isEmpty()) {
            return;
        }

        Card selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!library.remove(selected)) {
            return;
        }

        Card modified = selected.createRuntimeCopy();
        modified.setOwnerId(targetPlayerId);
        modified.setPower(powerAndToughness);
        modified.setToughness(powerAndToughness);
        EnumSet<Keyword> keywords = modified.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(modified.getKeywords());
        keywords.add(Keyword.WARD);
        modified.setKeywords(keywords);
        modified.freeze();
        gameData.perpetualCardBasePowerToughness.put(modified.getId(),
                new GameData.PerpetualBasePowerToughness(powerAndToughness, powerAndToughness,
                        gameData.nextTimestamp()));

        CounterUnlessPaysEffect ward = new CounterUnlessPaysEffect(1);
        rememberPerpetualWard(gameData, modified.getId(), ward);

        Permanent permanent = new Permanent(modified, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, modified, null, false);

        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                modified, gameData.playerIdToName.get(controllerId)));
    }

    private void rememberPerpetualWard(GameData gameData, UUID cardId, CounterUnlessPaysEffect ward) {
        gameData.perpetualCardKeywords.merge(cardId, Set.of(Keyword.WARD),
                PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffectHandler::mergeKeywords);
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new java.util.EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            updated.computeIfAbsent(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                    ignoredSlot -> new ArrayList<>()).add(ward);
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return Map.copyOf(updated);
        });
    }

    private static Set<Keyword> mergeKeywords(Set<Keyword> existing, Set<Keyword> added) {
        EnumSet<Keyword> merged = EnumSet.noneOf(Keyword.class);
        merged.addAll(existing);
        merged.addAll(added);
        return Set.copyOf(merged);
    }
}
