package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayExileOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardRemoveTargetFromCombatIfMatchEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOpponentPaysLifeOrToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsTypeToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ScryMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect e = (LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int xValue = entry.getXValue();
        boolean toBottomRandom = e.remainingToBottomRandom();

        int count = Math.min(xValue, deck.size());
        if (count <= 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName
                    + (deck.isEmpty() ? "'s library is empty."
                    : toBottomRandom ? " reveals 0 cards (0 damage dealt)." : " looks at 0 cards (X is 0).");
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> revealedCards = LibraryRevealSupport.takeTopCards(deck, count);

        String logMsg = toBottomRandom
                ? playerName + " reveals the top " + LibraryRevealSupport.pluralCards(count) + " of their library."
                : playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        // Filter eligible cards using predicates
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (e.alwaysEligiblePredicate() != null
                    && predicateEvaluationService.matchesCardPredicate(card, e.alwaysEligiblePredicate(), null, gameData, controllerId)) {
                eligibleCards.add(card);
            } else if (e.mvCappedEligiblePredicate() != null
                    && card.getManaValue() <= xValue
                    && predicateEvaluationService.matchesCardPredicate(card, e.mvCappedEligiblePredicate(), null)) {
                eligibleCards.add(card);
            }
        }

        if (eligibleCards.isEmpty()) {
            if (toBottomRandom) {
                // No eligible cards — put all on bottom in random order
                Collections.shuffle(revealedCards);
                deck.addAll(revealedCards);
                String noEligibleLog = playerName + " finds no eligible cards. All cards are put on the bottom of their library in a random order.";
                gameBroadcastService.logAndBroadcast(gameData, noEligibleLog);
            } else {
                // No eligible cards — put all into graveyard
                for (Card card : revealedCards) {
                    gameData.playerGraveyards.get(controllerId).add(card);
                }
                String noEligibleLog = playerName + " finds no eligible cards. All cards are put into their graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, noEligibleLog);
            }
            return;
        }

        // Set up player choice for selecting cards to put onto battlefield
        String prompt = toBottomRandom
                ? "Choose any number of eligible cards to put onto the battlefield. The rest go to the bottom of your library in a random order."
                : "Choose any number of eligible cards to put onto the battlefield. The rest go to your graveyard.";
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, cardIds, !toBottomRandom, false, false, toBottomRandom, 0, null,
                eligibleCards.size(), prompt));

        log.info("Game {} - {} resolving {} with X={}, {} revealed, {} eligible",
                gameData.id, playerName, entry.getCard().getName(), xValue, count, eligibleCards.size());
    
    }
}
