package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardTargetingService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;

    public void handleGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                  List<CardEffect> allEffects, ExileCardsFromGraveyardEffect exile) {
        List<UUID> allCardIds = new ArrayList<>();
        List<CardView> allCardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                allCardIds.add(graveyardCard.getId());
                allCardViews.add(cardViewFactory.create(graveyardCard));
            }
        }

        if (allCardIds.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            int maxTargets = Math.min(exile.maxTargets(), allCardIds.size());
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(allEffects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from graveyards to exile.");
        }
    }

    public void handleBeginningOfCombatGraveyardTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId,
            ExileTargetCardFromGraveyardEffect exileEffect) {
        CardType requiredType = exileEffect.requiredType();
        boolean anyGraveyard = exileEffect.canTargetAnyGraveyard();

        List<UUID> allCardIds = new ArrayList<>();
        List<CardView> allCardViews = new ArrayList<>();
        List<UUID> searchPlayerIds = anyGraveyard ? gameData.orderedPlayerIds : List.of(controllerId);
        for (UUID playerId : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (requiredType == null || graveyardCard.hasType(requiredType)) {
                    allCardIds.add(graveyardCard.getId());
                    allCardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        String description = card.getName() + "'s beginning of combat ability";

        if (allCardIds.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    description,
                    new ArrayList<>(effects),
                    0,
                    null,
                    sourcePermanentId,
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            ));
            String logEntry = description + " triggers.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} beginning-of-combat trigger pushed onto stack with 0 graveyard targets",
                    gameData.id, card.getName());
            return;
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, 1,
                "Choose up to one target card from a graveyard to exile.");

        String logEntry = description + " triggers — choose a graveyard target.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} beginning-of-combat trigger awaiting graveyard target selection",
                gameData.id, card.getName());
    }

    public void handleGraveyardCastETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                 List<CardEffect> effects) {
        CastTargetInstantOrSorceryFromGraveyardEffect castEffect = effects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElseThrow();
        GraveyardSearchScope scope = castEffect.scope();

        List<UUID> eligibleCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            boolean include = switch (scope) {
                case OPPONENT_GRAVEYARD -> !playerId.equals(controllerId);
                case CONTROLLERS_GRAVEYARD -> playerId.equals(controllerId);
                case ALL_GRAVEYARDS -> true;
            };
            if (!include) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.INSTANT) || graveyardCard.hasType(CardType.SORCERY)) {
                    eligibleCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        if (eligibleCardIds.isEmpty()) {
            String etbLog = card.getName() + "'s enter-the-battlefield ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB graveyard cast has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, eligibleCardIds, cardViews, 1,
                    "Choose target instant or sorcery card from a graveyard to cast.");
        }
    }

    public void handleGrantFlashbackETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                  List<CardEffect> effects) {
        GrantFlashbackToTargetGraveyardCardEffect flashbackEffect = effects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect)
                .map(e -> (GrantFlashbackToTargetGraveyardCardEffect) e)
                .findFirst().orElseThrow();

        List<UUID> eligibleCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                boolean matchesType = false;
                for (CardType type : flashbackEffect.cardTypes()) {
                    if (graveyardCard.hasType(type)) {
                        matchesType = true;
                        break;
                    }
                }
                if (matchesType) {
                    eligibleCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        if (eligibleCardIds.isEmpty()) {
            String etbLog = card.getName() + "'s enter-the-battlefield ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB grant flashback has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, eligibleCardIds, cardViews, 1,
                    "Choose target instant or sorcery card in your graveyard to gain flashback.");
        }
    }

    public void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                               StackEntryType entryType, int xValue) {
        List<UUID> creatureCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.CREATURE)) {
                    creatureCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = xValue;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCardIds, cardViews, xValue,
                "Choose " + xValue + " target creature card" + (xValue != 1 ? "s" : "") + " from your graveyard to exile.");
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = matchingCardIds.size();
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose any number of target " + filterLabel + "s from your graveyard.");
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from your graveyard.");
    }

    public void handleUpToNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                UUID targetPlayerId, Card card,
                                                                StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                                List<CardEffect> spellEffects) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from " + targetPlayerName + "'s graveyard.");
    }

    public void handleUpToNAllGraveyardsSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                        List<CardEffect> spellEffects) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from graveyards.");
    }
}
