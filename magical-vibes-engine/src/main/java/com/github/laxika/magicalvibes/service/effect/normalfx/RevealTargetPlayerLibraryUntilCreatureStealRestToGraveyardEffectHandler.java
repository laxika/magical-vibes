package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
public class RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Reveal cards from the top of the target player's library until a creature is revealed.
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> revealedCards = new ArrayList<>();
        Card foundCreature = null;

        while (deck != null && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.CREATURE)) {
                foundCreature = card;
                break;
            }
        }

        if (revealedCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " reveals " + revealedNames + "."));

        if (foundCreature != null) {
            // The creature card is stolen: it goes onto the caster's battlefield, not the graveyard.
            revealedCards.remove(foundCreature);

            Permanent perm = new Permanent(foundCreature);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(foundCreature, controllerName));
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, foundCreature, null, false);
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " reveals their entire library — no creature card was found."));
        }

        // All revealed noncreature cards go to the target player's graveyard.
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, targetPlayerId, card);
        }

        if (foundCreature != null && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} reveals {} from {}'s library, stolen creature={}",
                gameData.id, targetName, revealedCards.size() + (foundCreature != null ? 1 : 0),
                targetName, foundCreature != null ? foundCreature.getName() : "none");
    }
}
