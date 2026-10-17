package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect) effect;
        AmountContext baseContext = AmountContext.forStackEntry(entry, null);
        CardIsPermanentPredicate permanentPredicate = new CardIsPermanentPredicate();
        Map<UUID, List<Card>> permanentCardsByPlayer = new LinkedHashMap<>();
        Map<UUID, Boolean> shuffledLibraries = new LinkedHashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            int revealCount = Math.max(0, amountEvaluationService.evaluate(
                    gameData, e.amount(), baseContext.withControllerId(playerId)));
            int actualCount = Math.min(revealCount, library.size());
            if (actualCount == 0) {
                continue;
            }

            List<Card> revealedCards = LibraryRevealSupport.takeTopCards(library, actualCount);
            logReveal(gameData, entry, playerId, revealedCards);

            List<Card> permanentCards = new ArrayList<>();
            for (Card card : revealedCards) {
                if (predicateEvaluationService.matchesCardPredicate(
                        card, permanentPredicate, null, gameData, playerId)) {
                    permanentCards.add(card);
                } else {
                    graveyardService.addCardToGraveyard(gameData, playerId, card, Zone.LIBRARY);
                }
            }

            if (!permanentCards.isEmpty()) {
                permanentCardsByPlayer.put(playerId, permanentCards);
            }
        }

        List<BattlefieldEntryCard> entering = new ArrayList<>();
        for (Map.Entry<UUID, List<Card>> playerCards : permanentCardsByPlayer.entrySet()) {
            UUID playerId = playerCards.getKey();
            List<Card> library = gameData.playerDecks.get(playerId);
            List<Card> enteringCards = new ArrayList<>();
            for (Card card : playerCards.getValue()) {
                if (card.isAura() && !card.isEnchantZone() && !hasLegalAuraHost(gameData, playerId, card)) {
                    library.add(0, card);
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            " has no legal object to enchant; it stays on top of the library."));
                    continue;
                }
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                    library.add(card);
                    shuffledLibraries.put(playerId, true);
                    continue;
                }
                enteringCards.add(card);
                entering.add(new BattlefieldEntryCard(
                        battlefieldEntryService.resolveEnteringController(gameData, playerId, new Permanent(card)),
                        playerId, card, Zone.LIBRARY, null));
            }
            library.addAll(0, enteringCards);
        }

        shuffledLibraries.forEach((playerId, ignored) ->
                Collections.shuffle(gameData.playerDecks.get(playerId)));

        battlefieldEntryBatchSupport.begin(gameData, entering);

        if (!gameData.interaction.isAwaitingInput()) {
            gameData.orderedPlayerIds.forEach(playerId -> legendRuleService.checkLegendRule(gameData, playerId));
        }
    }

    private boolean hasLegalAuraHost(GameData gameData, UUID controllerId, Card aura) {
        if (!aura.isEnchantPlayer() && gameData.anyPermanentMatches(permanent ->
                !gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                        && auraAttachmentService.canEnchant(gameData, aura, controllerId, permanent))) {
            return true;
        }
        return gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> auraAttachmentService.canEnchantPlayer(gameData, aura, controllerId, playerId));
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
