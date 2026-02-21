package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardOfSubtypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardReturnResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(ReturnAuraFromGraveyardToBattlefieldEffect.class,
                (gd, entry, effect) -> resolveReturnAuraFromGraveyardToBattlefield(gd, entry));
        registry.register(ReturnCreatureFromGraveyardToBattlefieldEffect.class,
                (gd, entry, effect) -> resolveReturnCardFromGraveyardToZone(gd, entry, CardType.CREATURE,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        "You may return a creature card from your graveyard to the battlefield."));
        registry.register(ReturnArtifactFromGraveyardToHandEffect.class,
                (gd, entry, effect) -> resolveReturnCardFromGraveyardToZone(gd, entry, CardType.ARTIFACT,
                        GraveyardChoiceDestination.HAND,
                        "You may return an artifact card from your graveyard to your hand."));
        registry.register(ReturnCreatureFromGraveyardToHandEffect.class,
                (gd, entry, effect) -> resolveReturnCardFromGraveyardToZone(gd, entry, CardType.CREATURE,
                        GraveyardChoiceDestination.HAND,
                        "You may return a creature card from your graveyard to your hand."));
        registry.register(ReturnCardFromGraveyardToHandEffect.class,
                (gd, entry, effect) -> resolveReturnCardFromGraveyardToZone(gd, entry, null,
                        GraveyardChoiceDestination.HAND,
                        "You may return a card from your graveyard to your hand."));
        registry.register(ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect.class,
                (gd, entry, effect) -> resolveReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHand(gd, entry));
        registry.register(ReturnSelfFromGraveyardToHandEffect.class,
                (gd, entry, effect) -> resolveReturnSelfFromGraveyardToHand(gd, entry));
        registry.register(ReturnCardOfSubtypeFromGraveyardToHandEffect.class,
                (gd, entry, effect) -> resolveReturnCardOfSubtypeFromGraveyardToHand(gd, entry,
                        (ReturnCardOfSubtypeFromGraveyardToHandEffect) effect));
        registry.register(ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect.class,
                (gd, entry, effect) -> resolveReturnArtifactOrCreatureFromAnyGraveyardToBattlefield(gd, entry));
        registry.register(ExileCardsFromGraveyardEffect.class,
                (gd, entry, effect) -> resolveExileCardsFromGraveyard(gd, entry, (ExileCardsFromGraveyardEffect) effect));
        registry.register(ExileCreaturesFromGraveyardAndCreateTokensEffect.class,
                (gd, entry, effect) -> resolveExileCreaturesAndCreateTokens(gd, entry));
    }

    void resolveReturnAuraFromGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (auraCard == null || !auraCard.isAura()) {
            String fizzleLog = entry.getDescription() + " fizzles (target Aura no longer in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        gameHelper.removeCardFromGraveyardById(gameData, auraCard.getId());
        gameData.interaction.setPendingAuraCard(auraCard);

        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds, "Choose a creature you control to attach " + auraCard.getName() + " to.");
    }

    void resolveReturnCardFromGraveyardToZone(GameData gameData, StackEntry entry,
            CardType cardType, GraveyardChoiceDestination destination, String prompt) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String typeName = cardType != null ? cardType.name().toLowerCase() : "card";
        String targetLabel = cardType != null ? typeName + " card" : "card";
        if (entry.getTargetZone() == Zone.GRAVEYARD && entry.getTargetPermanentId() != null) {
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId());
            if (targetCard == null || (cardType != null && targetCard.getType() != cardType)) {
                String fizzleLog = entry.getDescription() + " fizzles (target " + targetLabel + " is no longer in a graveyard).";
                gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                return;
            }

            gameHelper.removeCardFromGraveyardById(gameData, targetCard.getId());
            if (destination == GraveyardChoiceDestination.HAND) {
                gameData.playerHands.get(controllerId).add(targetCard);
                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = playerName + " returns " + targetCard.getName() + " from graveyard to hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }
        }

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + targetLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (cardType == null || graveyard.get(i).getType() == cardType) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + targetLabel + "s in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.prepareGraveyardChoice(destination, null);
        playerInputService.beginGraveyardChoice(gameData, controllerId, matchingIndices, prompt);
    }

    void resolveReturnCardOfSubtypeFromGraveyardToHand(GameData gameData, StackEntry entry,
            ReturnCardOfSubtypeFromGraveyardToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String subtypeName = effect.subtype().getDisplayName();

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + subtypeName + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getSubtypes().contains(effect.subtype())) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + subtypeName + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.HAND, null);
        playerInputService.beginGraveyardChoice(gameData, controllerId, matchingIndices,
                "Return a " + subtypeName + " card from your graveyard to your hand.");
    }

    void resolveReturnSelfFromGraveyardToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        Card sourceCard = entry.getCard();
        boolean removed = graveyard.removeIf(card -> card.getId().equals(sourceCard.getId()));
        if (!removed) {
            return;
        }

        gameData.playerHands.get(controllerId).add(sourceCard);
        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " returns " + sourceCard.getName() + " from graveyard to hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, playerName, sourceCard.getName());
    }

    void resolveReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Set<UUID> trackedIds = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, Set.of());

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
    }

    void resolveReturnArtifactOrCreatureFromAnyGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> cardPool = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.getType() == CardType.CREATURE || card.getType() == CardType.ARTIFACT) {
                    cardPool.add(card);
                }
            }
        }

        if (cardPool.isEmpty()) {
            String logEntry = entry.getDescription() + " — no artifact or creature cards in any graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            // Per Magic rules: spell fizzles when it has no legal targets at resolution.
            // Remove ShuffleIntoLibraryEffect so the card goes to graveyard instead of being shuffled.
            entry.getEffectsToResolve().removeIf(e -> e instanceof ShuffleIntoLibraryEffect);
            return;
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < cardPool.size(); i++) {
            indices.add(i);
        }

        gameData.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.BATTLEFIELD, cardPool);
        playerInputService.beginGraveyardChoice(gameData, controllerId, indices,
                "Choose an artifact or creature card from a graveyard to put onto the battlefield under your control.");
    }

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

    void resolveExileCreaturesAndCreateTokens(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(controllerId);

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
            gameData.playerBattlefields.get(controllerId).add(tokenPermanent);

            String tokenLog = "A 2/2 Zombie creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, tokenLog);

            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            if (!gameData.interaction.isAwaitingInput()) {
                legendRuleService.checkLegendRule(gameData, controllerId);
            }

            log.info("Game {} - Zombie token created for player {}", gameData.id, controllerId);
        }
    }
}


