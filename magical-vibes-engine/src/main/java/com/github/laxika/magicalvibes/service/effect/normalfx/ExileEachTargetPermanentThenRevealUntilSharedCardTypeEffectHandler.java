package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = new ArrayList<>(entry.targetsForEffect(effect));
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getSpellTargets().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        List<ExiledPermanent> exiledPermanents = new ArrayList<>();
        for (UUID targetId : new LinkedHashSet<>(targetIds)) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null) {
                continue;
            }

            exiledPermanents.add(new ExiledPermanent(
                    controllerId, snapshotCardTypes(gameData, target)));
            permanentRemovalService.removePermanentToExile(gameData, target);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (ExiledPermanent exiled : exiledPermanents) {
            replaceFromLibrary(gameData, exiled);
        }
    }

    private Set<CardType> snapshotCardTypes(GameData gameData, Permanent permanent) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        if (gameQueryService.isArtifact(gameData, permanent)) {
            types.add(CardType.ARTIFACT);
        }
        if (gameQueryService.isCreature(gameData, permanent)) {
            types.add(CardType.CREATURE);
        }
        if (gameQueryService.isEnchantment(gameData, permanent)) {
            types.add(CardType.ENCHANTMENT);
        }
        if (gameQueryService.isPlaneswalker(gameData, permanent)) {
            types.add(CardType.PLANESWALKER);
        }
        if (gameQueryService.isLand(gameData, permanent)) {
            types.add(CardType.LAND);
        }
        return Set.copyOf(types);
    }

    private void replaceFromLibrary(GameData gameData, ExiledPermanent exiled) {
        String controllerName = gameData.playerIdToName.get(exiled.controllerId());
        List<Card> deck = gameData.playerDecks.get(exiled.controllerId());
        if (deck == null) {
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (!permanentTypesOf(card).isEmpty()
                    && cardSpecificSupport.cardMatchesAnyType(card, exiled.cardTypes())) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(controllerName + " reveals " + revealedNames + "."));

        if (foundCard == null) {
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, exiled.controllerId());
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " reveals their entire library — no matching card found. Library is shuffled."));
            return;
        }

        Permanent enteringPermanent = new Permanent(foundCard);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, exiled.controllerId(), enteringPermanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, controllerName));

        if (foundCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, exiled.controllerId(), foundCard, null, false);
        }
        if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
            enteringPermanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
            enteringPermanent.setSummoningSick(false);
        }

        revealedCards.remove(foundCard);
        deck.addAll(revealedCards);
        LibraryShuffleHelper.shuffleLibrary(gameData, exiled.controllerId());
        gameLogService.append(gameData, GameLog.text(controllerName + " shuffles their library."));
    }

    private record ExiledPermanent(UUID controllerId, Set<CardType> cardTypes) {
    }

    private Set<CardType> permanentTypesOf(Card card) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        if (card.getType() != null) {
            types.add(card.getType());
        }
        types.addAll(card.getAdditionalTypes());
        types.removeIf(type -> !type.isPermanentType() || type == CardType.KINDRED);
        return types;
    }
}
