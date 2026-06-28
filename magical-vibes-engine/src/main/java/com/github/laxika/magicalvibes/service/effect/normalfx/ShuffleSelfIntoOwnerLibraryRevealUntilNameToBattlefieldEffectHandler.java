package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect) effect;
        // Find source permanent on the battlefield
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return; // Already left the battlefield — nothing happens
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId());
        if (controllerId == null) {
            return;
        }

        // Owner may differ from controller (e.g. stolen creatures)
        UUID ownerId = gameData.stolenCreatures.getOrDefault(self.getId(), controllerId);
        String ownerName = gameData.playerIdToName.get(ownerId);
        String cardName = self.getCard().getName();

        // Shuffle self into owner's library
        permanentRemovalService.removePermanentToLibraryBottom(gameData, self);
        permanentRemovalService.removeOrphanedAuras(gameData);
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);

        gameBroadcastService.logAndBroadcast(gameData,
                ownerName + " shuffles " + cardName + " into their library.");

        // Reveal cards from top of owner's library until finding the named card
        List<Card> deck = gameData.playerDecks.get(ownerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;

        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.getName().equals(e.cardName())) {
                foundCard = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    ownerName + "'s library is empty — no cards are revealed.");
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, ownerName + " reveals " + revealedNames + ".");

        if (foundCard != null) {
            // Remove found card from the revealed list (it goes to battlefield, not graveyard)
            revealedCards.remove(foundCard);

            // Put found card onto the battlefield under the owner's control
            Permanent perm = new Permanent(foundCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);

            gameBroadcastService.logAndBroadcast(gameData,
                    foundCard.getName() + " enters the battlefield under " + ownerName + "'s control.");

            if (foundCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, foundCard, null, false);
            }

            if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                perm.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                perm.setSummoningSick(false);
            }
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    ownerName + " reveals their entire library — no card named " + e.cardName() + " was found.");
        }

        // All other revealed cards go to owner's graveyard
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, ownerId, card);
        }

        // Check legend rule
        if (foundCard != null && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, ownerId);
        }

        log.info("Game {} - {} shuffled {} into library, revealed {} cards, found={}",
                gameData.id, ownerName, cardName, revealedCards.size() + (foundCard != null ? 1 : 0),
                foundCard != null ? foundCard.getName() : "none");
    }
}
