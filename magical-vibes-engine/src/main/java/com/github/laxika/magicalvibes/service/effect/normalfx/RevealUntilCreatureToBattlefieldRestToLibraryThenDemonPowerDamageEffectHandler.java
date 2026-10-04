package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCreature = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.CREATURE)) {
                foundCreature = card;
                break;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        Permanent enteredPermanent = null;
        if (foundCreature != null
                && !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, foundCreature, Zone.LIBRARY)) {
            revealedCards.remove(foundCreature);
            enteredPermanent = new Permanent(foundCreature);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, enteredPermanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCreature, playerName));
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, foundCreature, null, false);
        } else if (foundCreature == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no creature card was found."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    foundCreature.getName() + " can't enter the battlefield from a library; it stays in the library."));
        }

        deck.addAll(revealedCards);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        boolean enteredDemon = enteredPermanent != null
                && gameQueryService.effectiveCreatureSubtypes(gameData, enteredPermanent)
                .contains(CardSubtype.DEMON);
        if (enteredDemon) {
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    foundCreature,
                    controllerId,
                    foundCreature.getName() + "'s ability",
                    List.of(),
                    null,
                    enteredPermanent.getId());
            dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry,
                    new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
        }

        log.info("Game {} - {} revealed a creature, entered={}, demon={}",
                gameData.id, playerName, foundCreature != null, enteredDemon);
    }
}
