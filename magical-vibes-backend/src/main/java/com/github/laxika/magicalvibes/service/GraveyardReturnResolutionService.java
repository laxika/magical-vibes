package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardReturnResolutionService {

    private final GameHelper gameHelper;
    private final PermanentRemovalService permanentRemovalService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @HandlesEffect(ReturnCardFromGraveyardEffect.class)
    void resolveReturnCardFromGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();

        // Case 1: Pre-targeted (from spell cast or activated ability targeting)
        if (entry.getTargetZone() == Zone.GRAVEYARD && entry.getTargetPermanentId() != null) {
            resolvePreTargeted(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        // Case 2: Return all matching cards (no choice)
        if (effect.returnAll()) {
            resolveReturnAll(gameData, entry, effect, controllerId, sourceCardId);
            return;
        }

        // Case 3: Search and choose at resolution
        if (effect.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            resolveFromAllGraveyards(gameData, entry, effect, controllerId, sourceCardId);
        } else {
            resolveFromControllersGraveyard(gameData, entry, effect, controllerId, sourceCardId);
        }
    }

    private void resolvePreTargeted(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                    UUID controllerId, UUID sourceCardId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        String filterLabel = describeFilter(effect.filter());

        if (targetCard == null || (effect.filter() != null && !gameQueryService.matchesCardPredicate(targetCard, effect.filter(), sourceCardId))) {
            String fizzleLog = entry.getDescription() + " fizzles (target " + filterLabel + " is no longer in a graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Aura attachment path
        if (effect.attachmentTarget() != null) {
            List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
            List<UUID> attachTargetIds = new ArrayList<>();
            if (controllerBf != null) {
                for (Permanent p : controllerBf) {
                    if (gameQueryService.matchesPermanentPredicate(gameData, p, effect.attachmentTarget())) {
                        attachTargetIds.add(p.getId());
                    }
                }
            }

            if (attachTargetIds.isEmpty()) {
                String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
                gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
            gameData.interaction.setPendingAuraCard(targetCard);

            playerInputService.beginPermanentChoice(gameData, controllerId, attachTargetIds,
                    "Choose a creature you control to attach " + targetCard.getName() + " to.");
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());
        moveCardToDestination(gameData, controllerId, targetCard, effect.destination());
    }

    private void resolveReturnAll(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                  UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = describeFilter(effect.filter());

        if (effect.thisTurnOnly()) {
            Set<UUID> trackedIds = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                    .getOrDefault(controllerId, Set.of());

            if (graveyard == null || graveyard.isEmpty() || trackedIds.isEmpty()) {
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard from the battlefield this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }

            List<Card> toReturn = new ArrayList<>();
            for (Card card : graveyard) {
                boolean isCreatureCard = card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE);
                if (!card.isToken() && isCreatureCard && trackedIds.contains(card.getId())) {
                    toReturn.add(card);
                }
            }

            if (toReturn.isEmpty()) {
                String logEntry = entry.getDescription() + " - no creature cards were put into your graveyard from the battlefield this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }

            List<Card> hand = gameData.playerHands.get(controllerId);
            List<String> returnedNames = new ArrayList<>();
            for (Card card : toReturn) {
                graveyard.remove(card);
                hand.add(card);
                returnedNames.add(card.getName());
                trackedIds.remove(card.getId());
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " returns " + String.join(", ", returnedNames)
                    + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns {} creature card(s) from graveyard to hand",
                    gameData.id, playerName, returnedNames.size());
            return;
        }

        // Return all matching (e.g. self-return)
        if (graveyard == null) {
            return;
        }

        List<Card> toReturn = new ArrayList<>();
        for (Card card : graveyard) {
            if (gameQueryService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                toReturn.add(card);
            }
        }

        if (toReturn.isEmpty()) {
            return;
        }

        List<String> returnedNames = new ArrayList<>();
        for (Card card : toReturn) {
            graveyard.remove(card);
            if (effect.destination() == GraveyardChoiceDestination.HAND) {
                gameData.playerHands.get(controllerId).add(card);
            } else {
                putCardOntoBattlefield(gameData, controllerId, card);
            }
            returnedNames.add(card.getName());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String destName = effect.destination() == GraveyardChoiceDestination.HAND ? "hand" : "the battlefield";
        String logEntry = playerName + " returns " + String.join(", ", returnedNames)
                + " from graveyard to " + destName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returns {} from graveyard to {}", gameData.id, playerName,
                String.join(", ", returnedNames), destName);
    }

    private void resolveFromControllersGraveyard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                                 UUID controllerId, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String filterLabel = describeFilter(effect.filter());

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (gameQueryService.matchesCardPredicate(graveyard.get(i), effect.filter(), sourceCardId)) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield";
        String prompt = "Return a " + filterLabel + " from your graveyard to " + destText + ".";

        gameData.interaction.prepareGraveyardChoice(effect.destination(), null);
        playerInputService.beginGraveyardChoice(gameData, controllerId, matchingIndices, prompt);
    }

    private void resolveFromAllGraveyards(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect effect,
                                          UUID controllerId, UUID sourceCardId) {
        List<Card> cardPool = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (gameQueryService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                    cardPool.add(card);
                }
            }
        }

        String filterLabel = describeFilter(effect.filter());

        if (cardPool.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + filterLabel + "s in any graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            entry.getEffectsToResolve().removeIf(e -> e instanceof ShuffleIntoLibraryEffect);
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < cardPool.size(); i++) {
            indices.add(i);
        }

        String destText = effect.destination() == GraveyardChoiceDestination.HAND ? "your hand" : "the battlefield under your control";
        String prompt = "Choose a " + filterLabel + " from a graveyard to put onto " + destText + ".";

        gameData.interaction.prepareGraveyardChoice(effect.destination(), cardPool);
        playerInputService.beginGraveyardChoice(gameData, controllerId, indices, prompt);
    }

    private void moveCardToDestination(GameData gameData, UUID controllerId, Card card,
                                       GraveyardChoiceDestination destination) {
        String playerName = gameData.playerIdToName.get(controllerId);
        if (destination == GraveyardChoiceDestination.HAND) {
            gameData.playerHands.get(controllerId).add(card);
            String logEntry = playerName + " returns " + card.getName() + " from graveyard to hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            putCardOntoBattlefield(gameData, controllerId, card);
        }
    }

    private void putCardOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        Set<CardType> enterTappedTypes = gameHelper.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " puts " + card.getName() + " onto the battlefield from a graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        if (gameQueryService.isCreature(gameData, permanent)) {
            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    public static String describeFilter(CardPredicate predicate) {
        if (predicate == null) return "card";
        if (predicate instanceof CardTypePredicate p) {
            return p.cardType().name().toLowerCase() + " card";
        }
        if (predicate instanceof CardSubtypePredicate p) {
            return p.subtype().getDisplayName() + " card";
        }
        if (predicate instanceof CardKeywordPredicate p) {
            return "card with " + p.keyword().name().toLowerCase().replace('_', ' ');
        }
        if (predicate instanceof CardIsAuraPredicate) {
            return "Aura card";
        }
        if (predicate instanceof CardAllOfPredicate p) {
            List<String> parts = new ArrayList<>();
            for (CardPredicate sub : p.predicates()) {
                parts.add(describeFilter(sub));
            }
            // "creature card" + "card with infect" → "creature card with infect"
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (i > 0 && part.startsWith("card with ")) {
                    sb.append(" with ").append(part.substring("card with ".length()));
                } else if (i > 0) {
                    sb.append(" ").append(part);
                } else {
                    sb.append(part);
                }
            }
            return sb.toString();
        }
        if (predicate instanceof CardAnyOfPredicate p) {
            List<String> parts = new ArrayList<>();
            for (CardPredicate sub : p.predicates()) {
                parts.add(describeFilter(sub));
            }
            // "artifact card or creature card" → "artifact or creature card"
            if (parts.size() >= 2 && parts.stream().allMatch(part -> part.endsWith(" card"))) {
                List<String> stripped = parts.stream()
                        .map(part -> part.substring(0, part.length() - " card".length()))
                        .toList();
                return String.join(" or ", stripped) + " card";
            }
            return String.join(" or ", parts);
        }
        return "card";
    }

    @HandlesEffect(PutCardFromOpponentGraveyardOntoBattlefieldEffect.class)
    void resolvePutFromOpponentGraveyardAndMill(GameData gameData, StackEntry entry,
            PutCardFromOpponentGraveyardOntoBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target no longer in graveyard).");
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (graveyardOwnerId == null || graveyardOwnerId.equals(controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target not in opponent's graveyard).");
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCard.getId());

        Set<CardType> enterTappedTypes = gameHelper.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(targetCard);
        if (effect.tapped()) {
            permanent.tap();
        }
        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        gameData.stolenCreatures.put(permanent.getId(), graveyardOwnerId);
        gameData.permanentControlStolenCreatures.add(permanent.getId());

        String tappedText = effect.tapped() ? " tapped" : "";
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " puts " + targetCard.getName() + " onto the battlefield" + tappedText + " under their control.");

        if (gameQueryService.isCreature(gameData, permanent)) {
            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, targetCard, null, false);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }

        if (xValue > 0) {
            List<Card> opponentDeck = gameData.playerDecks.get(graveyardOwnerId);
            List<Card> opponentGraveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            int cardsToMill = Math.min(xValue, opponentDeck.size());
            List<String> milledNames = new ArrayList<>();
            for (int i = 0; i < cardsToMill; i++) {
                Card milled = opponentDeck.removeFirst();
                opponentGraveyard.add(milled);
                milledNames.add(milled.getName());
            }
            if (cardsToMill > 0) {
                String opponentName = gameData.playerIdToName.get(graveyardOwnerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        opponentName + " mills " + cardsToMill + " cards (" + String.join(", ", milledNames) + ").");
            }
        }
    }

    @HandlesEffect(ExileCardsFromGraveyardEffect.class)
    void resolveExileCardsFromGraveyard(GameData gameData, StackEntry entry, ExileCardsFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Exile targeted cards that are still in graveyards
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            List<String> exiledNames = new ArrayList<>();
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    exiledNames.add(card.getName());
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Card> graveyard = gameData.playerGraveyards.get(pid);
                        if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                            gameData.playerExiledCards.get(pid).add(card);
                            break;
                        }
                    }
                }
            }
            if (!exiledNames.isEmpty()) {
                String logEntry = playerName + " exiles " + String.join(", ", exiledNames) + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled {} cards from graveyards", gameData.id, playerName, exiledNames.size());
            }
        }

        // Gain life after exile
        if (effect.lifeGain() > 0) {
            Integer currentLife = gameData.playerLifeTotals.get(controllerId);
            gameData.playerLifeTotals.put(controllerId, currentLife + effect.lifeGain());

            String lifeLogEntry = playerName + " gains " + effect.lifeGain() + " life.";
            gameBroadcastService.logAndBroadcast(gameData, lifeLogEntry);
            log.info("Game {} - {} gains {} life", gameData.id, playerName, effect.lifeGain());
        }
    }

    @HandlesEffect(ExileCreaturesFromGraveyardAndCreateTokensEffect.class)
    void resolveExileCreaturesAndCreateTokens(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(gameHelper.snapshotEnterTappedTypes(gameData));

        int tokensToCreate = 0;
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Card> graveyard = gameData.playerGraveyards.get(pid);
                    if (graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                        gameData.playerExiledCards.get(pid).add(card);
                        break;
                    }
                }
                String exileLog = playerName + " exiles " + card.getName() + " from graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, exileLog);
                tokensToCreate++;
            }
        }

        for (int i = 0; i < tokensToCreate; i++) {
            Card tokenCard = new Card();
            tokenCard.setName("Zombie");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.BLACK);
            tokenCard.setPower(2);
            tokenCard.setToughness(2);
            tokenCard.setSubtypes(List.of(CardSubtype.ZOMBIE));

            Permanent tokenPermanent = new Permanent(tokenCard);
            gameHelper.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String tokenLog = "A 2/2 Zombie creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, tokenLog);

            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }

            log.info("Game {} - Zombie token created for player {}", gameData.id, controllerId);
        }
    }

    @HandlesEffect(PutImprintedCreatureOntoBattlefieldEffect.class)
    void resolvePutImprintedCreatureOntoBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        Card imprintedCard = entry.getCard().getImprintedCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (imprintedCard == null) {
            String logMsg = entry.getCard().getName() + "'s imprint ability resolves but no card was imprinted.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        String revealLog = playerName + " turns the exiled card face up: " + imprintedCard.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        boolean isCreature = imprintedCard.getType() == CardType.CREATURE
                || imprintedCard.getAdditionalTypes().contains(CardType.CREATURE);

        if (!isCreature) {
            String notCreatureLog = imprintedCard.getName() + " is not a creature card. It remains in exile.";
            gameBroadcastService.logAndBroadcast(gameData, notCreatureLog);
            return;
        }

        // Remove from exile zone
        List<Card> exiledCards = gameData.playerExiledCards.get(controllerId);
        if (exiledCards != null) {
            exiledCards.removeIf(c -> c.getId().equals(imprintedCard.getId()));
        }

        // Put onto the battlefield
        Permanent perm = new Permanent(imprintedCard);
        gameHelper.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String enterLog = imprintedCard.getName() + " enters the battlefield under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, enterLog);

        gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, imprintedCard, null, false);

        log.info("Game {} - {} puts imprinted creature {} onto battlefield",
                gameData.id, playerName, imprintedCard.getName());
    }
}
