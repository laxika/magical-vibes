package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final CardSpecificSupport cardSpecificSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllCreaturesYouControlThenRevealCreaturesToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Step 1: Find all creatures the controller controls
        List<Permanent> creaturesToExile = new ArrayList<>(
                gameData.playerBattlefields.get(controllerId).stream()
                        .filter(p -> p.getCard().hasType(CardType.CREATURE))
                        .toList()
        );

        int creatureCount = creaturesToExile.size();

        if (creatureCount == 0) {
            String noCreaturesLog = controllerName + " controls no creatures — no cards are exiled or revealed.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(noCreaturesLog));
            return;
        }

        // Step 2: Exile all creatures
        for (Permanent creature : creaturesToExile) {
            String creatureName = creature.getCard().getName();
            permanentRemovalService.removePermanentToExile(gameData, creature);
            String exileLog = controllerName + " exiles " + creatureName + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
        }

        // Step 3: Reveal cards from the top of the library until finding that many creature cards
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> revealedCards = new ArrayList<>();
        List<Card> foundCreatures = new ArrayList<>();

        while (!deck.isEmpty() && foundCreatures.size() < creatureCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (cardSpecificSupport.cardMatchesAnyType(card, Set.of(CardType.CREATURE))) {
                foundCreatures.add(card);
            }
        }

        if (revealedCards.isEmpty()) {
            String emptyLog = controllerName + "'s library is empty — no cards are revealed.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(emptyLog));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        String revealLog = controllerName + " reveals " + revealedNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(revealLog));

        if (foundCreatures.isEmpty()) {
            // No creature cards found — shuffle all revealed cards back into the library
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String noMatchLog = controllerName + " reveals their entire library — no creature cards found. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(noMatchLog));
            return;
        }

        // Step 4: Put all found creature cards onto the battlefield simultaneously (ruling 2010-08-15)
        for (Card creatureCard : foundCreatures) {
            Permanent perm = new Permanent(creatureCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(creatureCard, controllerName));

            // Handle planeswalkers (e.g. artifact creatures that are also planeswalkers)
            if (creatureCard.hasType(CardType.PLANESWALKER) && creatureCard.getLoyalty() != null) {
                perm.setCounterCount(CounterType.LOYALTY, creatureCard.getLoyalty());
                perm.setSummoningSick(false);
            }
        }

        // Step 5: Shuffle all non-creature revealed cards back into the library
        // This happens before ETB triggers are put on the stack (ruling 2010-08-15:
        // "Any abilities that trigger during the resolution of Mass Polymorph will wait
        // to be put onto the stack until Mass Polymorph finishes resolving.")
        List<Card> nonCreatureCards = new ArrayList<>(revealedCards);
        nonCreatureCards.removeAll(foundCreatures);
        if (!nonCreatureCards.isEmpty()) {
            deck.addAll(nonCreatureCards);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        String shuffleLog = controllerName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(shuffleLog));

        // Step 6: Process ETB triggers after all creatures are on the battlefield and the
        // spell has finished resolving. All creatures enter at the same time, so triggers
        // see the full board state.
        for (Card creatureCard : foundCreatures) {
            battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, creatureCard, null, false);
        }

        // Check legend rule for each creature that entered
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} exiled {} creatures, {} creature cards entered the battlefield",
                gameData.id, controllerName, creatureCount, foundCreatures.size());
    }
}
