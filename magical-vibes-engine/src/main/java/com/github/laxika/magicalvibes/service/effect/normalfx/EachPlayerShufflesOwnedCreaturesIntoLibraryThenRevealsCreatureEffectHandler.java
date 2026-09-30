package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesOwnedCreaturesIntoLibraryThenRevealsCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EachPlayerShufflesOwnedCreaturesIntoLibraryThenRevealsCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesOwnedCreaturesIntoLibraryThenRevealsCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<CreatureToShuffle>> creaturesByOwner = collectOwnedCreatures(gameData);
        List<Permanent> creatures = creaturesByOwner.values().stream()
                .flatMap(List::stream)
                .map(CreatureToShuffle::permanent)
                .toList();
        Set<UUID> tokenCardIds = creaturesByOwner.values().stream()
                .flatMap(List::stream)
                .filter(creature -> !creature.nontoken())
                .flatMap(creature -> creature.permanent().cardsLeavingBattlefield().stream())
                .map(Card::getId)
                .collect(Collectors.toSet());

        if (!creatures.isEmpty()) {
            permanentRemovalService.removeAllToLibraryBottom(gameData, creatures);
        }

        Map<UUID, Boolean> shuffledNontokenCreatureByPlayer = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            boolean shuffledNontokenCreature = creaturesByOwner
                    .getOrDefault(playerId, List.of()).stream()
                    .anyMatch(creature -> creature.nontoken()
                            && isCardInLibrary(gameData, playerId, creature.permanent()));
            shuffledNontokenCreatureByPlayer.put(playerId, shuffledNontokenCreature);

            List<Card> library = gameData.playerDecks.get(playerId);
            library.removeIf(card -> card.isToken()
                    || tokenCardIds.contains(card.getId()));
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId)
                            + " shuffles all creatures they own into their library."));
        }

        Map<UUID, RevealResult> revealsByPlayer = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!shuffledNontokenCreatureByPlayer.getOrDefault(playerId, false)) {
                continue;
            }

            List<Card> library = gameData.playerDecks.get(playerId);
            List<Card> revealed = new ArrayList<>();
            Card foundCreature = null;
            while (!library.isEmpty()) {
                Card card = library.removeFirst();
                revealed.add(card);
                if (card.hasType(CardType.CREATURE)) {
                    foundCreature = card;
                    break;
                }
            }
            revealsByPlayer.put(playerId, new RevealResult(foundCreature, revealed));
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " reveals "
                            + revealed.stream().map(Card::getName).toList() + " from their library."));
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<EnteredCreature> enteredCreatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            RevealResult reveal = revealsByPlayer.get(playerId);
            if (reveal == null || reveal.foundCreature() == null) {
                continue;
            }

            Card foundCreature = reveal.foundCreature();
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, foundCreature, Zone.LIBRARY)) {
                gameData.playerDecks.get(playerId).addFirst(foundCreature);
                reveal.revealed().remove(foundCreature);
                continue;
            }

            Permanent permanent = new Permanent(foundCreature, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, playerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredCreatures.add(new EnteredCreature(playerId, foundCreature));
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                    foundCreature, gameData.playerIdToName.get(playerId)));
        }

        for (EnteredCreature entered : enteredCreatures) {
            battlefieldEntryService.processCreatureETBEffects(
                    gameData, entered.playerId(), entered.card(), null, false);
        }

        for (Map.Entry<UUID, RevealResult> revealEntry : revealsByPlayer.entrySet()) {
            List<Card> remaining = new ArrayList<>(revealEntry.getValue().revealed());
            remaining.remove(revealEntry.getValue().foundCreature());
            if (remaining.isEmpty()) {
                continue;
            }
            Collections.shuffle(remaining);
            gameData.playerDecks.get(revealEntry.getKey()).addAll(remaining);
        }
    }

    private Map<UUID, List<CreatureToShuffle>> collectOwnedCreatures(GameData gameData) {
        Map<UUID, List<CreatureToShuffle>> creaturesByOwner = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                return;
            }

            UUID ownerId = gameData.stolenCreatures.get(permanent.getId());
            if (ownerId == null) {
                ownerId = permanent.getOriginalCard().getOwnerId();
            }
            if (ownerId == null) {
                ownerId = controllerId;
            }

            creaturesByOwner.computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                    .add(new CreatureToShuffle(permanent, !gameQueryService.isToken(gameData, permanent)));
        });
        return creaturesByOwner;
    }

    private boolean isCardInLibrary(GameData gameData, UUID playerId, Permanent permanent) {
        Set<UUID> libraryCardIds = new HashSet<>();
        for (Card card : gameData.playerDecks.get(playerId)) {
            libraryCardIds.add(card.getId());
        }
        return permanent.cardsLeavingBattlefield().stream()
                .anyMatch(card -> libraryCardIds.contains(card.getId()));
    }

    private record CreatureToShuffle(Permanent permanent, boolean nontoken) {
    }

    private record RevealResult(Card foundCreature, List<Card> revealed) {
    }

    private record EnteredCreature(UUID playerId, Card card) {
    }
}
