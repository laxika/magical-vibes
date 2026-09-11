package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MorphicTideBottomCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MorphicTideEffectHandler implements NormalEffectHandlerBean {

    private static final Set<CardType> FIRST_BATCH_TYPES = Set.of(
            CardType.ARTIFACT, CardType.CREATURE, CardType.LAND, CardType.PLANESWALKER);

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LegendRuleService legendRuleService;
    private final ZoneToLibraryService zoneToLibraryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MorphicTideEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> shuffledPermanentCounts = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            shuffledPermanentCounts.put(playerId,
                    zoneToLibraryService.moveOwnedPermanentsIntoLibrary(gameData, playerId));
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }

        Map<UUID, List<Card>> revealedByPlayer = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            int revealCount = Math.min(shuffledPermanentCounts.get(playerId), library.size());
            if (revealCount == 0) {
                continue;
            }
            List<Card> revealed = LibraryRevealSupport.takeTopCards(library, revealCount);
            revealedByPlayer.put(playerId, revealed);
            logReveal(gameData, entry, playerId, revealed);
        }

        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<UUID, List<Permanent>> enteredByController = new LinkedHashMap<>();
        Map<UUID, Set<UUID>> acceptedCardIds = new LinkedHashMap<>();
        Map<UUID, Set<UUID>> rejectedCardIds = new LinkedHashMap<>();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);

        for (Map.Entry<UUID, List<Card>> playerCards : revealedByPlayer.entrySet()) {
            putFirstBatch(gameData, playerCards.getKey(), playerCards.getValue(), enterTappedTypes,
                    simultaneouslyEntered, enteredByController, acceptedCardIds, rejectedCardIds);
        }
        for (Map.Entry<UUID, List<Card>> playerCards : revealedByPlayer.entrySet()) {
            putEnchantmentBatch(gameData, playerCards.getKey(), playerCards.getValue(), enterTappedTypes,
                    simultaneouslyEntered, enteredByController, acceptedCardIds, rejectedCardIds);
        }

        enteredByController.forEach((controllerId, permanents) -> permanents.forEach(permanent ->
                battlefieldEntryService.processCreatureETBEffects(
                        gameData, controllerId, permanent.getCard(), null, false)));

        if (!gameData.interaction.isAwaitingInput()) {
            gameData.orderedPlayerIds.forEach(playerId -> legendRuleService.checkLegendRule(gameData, playerId));
        }

        List<CardEffect> followUps = new ArrayList<>();
        for (Map.Entry<UUID, List<Card>> playerCards : revealedByPlayer.entrySet()) {
            Set<UUID> accepted = acceptedCardIds.getOrDefault(playerCards.getKey(), Set.of());
            List<Card> rest = playerCards.getValue().stream()
                    .filter(card -> !accepted.contains(card.getId()))
                    .toList();
            if (!rest.isEmpty()) {
                followUps.add(new MorphicTideBottomCardsEffect(playerCards.getKey(), rest));
            }
        }
        if (!followUps.isEmpty()) {
            int effectIndex = findEffectIndex(entry, effect);
            if (effectIndex >= 0) {
                entry.insertEffectsToResolve(effectIndex + 1, followUps);
            }
        }
    }

    private void putFirstBatch(GameData gameData, UUID ownerId, List<Card> cards,
                               Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                               Map<UUID, List<Permanent>> enteredByController,
                               Map<UUID, Set<UUID>> acceptedCardIds, Map<UUID, Set<UUID>> rejectedCardIds) {
        for (Card card : cards) {
            if (!matchesAnyType(card, FIRST_BATCH_TYPES)) {
                continue;
            }
            putCardIfAllowed(gameData, ownerId, card, enterTappedTypes, simultaneouslyEntered,
                    enteredByController, acceptedCardIds, rejectedCardIds);
        }
    }

    private void putEnchantmentBatch(GameData gameData, UUID ownerId, List<Card> cards,
                                     Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                                     Map<UUID, List<Permanent>> enteredByController,
                                     Map<UUID, Set<UUID>> acceptedCardIds, Map<UUID, Set<UUID>> rejectedCardIds) {
        for (Card card : cards) {
            if (!card.hasType(CardType.ENCHANTMENT)
                    || acceptedCardIds.getOrDefault(ownerId, Set.of()).contains(card.getId())
                    || rejectedCardIds.getOrDefault(ownerId, Set.of()).contains(card.getId())) {
                continue;
            }
            putCardIfAllowed(gameData, ownerId, card, enterTappedTypes, simultaneouslyEntered,
                    enteredByController, acceptedCardIds, rejectedCardIds);
        }
    }

    private void putCardIfAllowed(GameData gameData, UUID ownerId, Card card,
                                  Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                                  Map<UUID, List<Permanent>> enteredByController,
                                  Map<UUID, Set<UUID>> acceptedCardIds, Map<UUID, Set<UUID>> rejectedCardIds) {
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
            rejectedCardIds.computeIfAbsent(ownerId, ignored -> java.util.HashSet.newHashSet(1)).add(card.getId());
            return;
        }

        Permanent permanent = new Permanent(card);
        initializeStartingCounters(permanent, card);
        UUID controllerId = battlefieldEntryService.resolveEnteringController(gameData, ownerId, permanent);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
        if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
            simultaneouslyEntered.add(permanent);
            acceptedCardIds.computeIfAbsent(ownerId, ignored -> java.util.HashSet.newHashSet(1)).add(card.getId());
            enteredByController.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(permanent);
        }
    }

    private boolean matchesAnyType(Card card, Set<CardType> types) {
        return types.stream().anyMatch(card::hasType);
    }

    private void initializeStartingCounters(Permanent permanent, Card card) {
        if (card.hasType(CardType.PLANESWALKER)) {
            permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty() != null ? card.getLoyalty() : 0);
            permanent.setSummoningSick(false);
        } else if (card.hasType(CardType.BATTLE)) {
            permanent.setCounterCount(CounterType.DEFENSE, card.getDefense() != null ? card.getDefense() : 0);
            permanent.setSummoningSick(false);
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                return i;
            }
        }
        return -1;
    }

    private void logReveal(GameData gameData, StackEntry entry, UUID playerId, List<Card> revealedCards) {
        GameLog.Builder builder = GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(revealedCards.get(i));
        }
        builder.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, builder.build());
    }
}
