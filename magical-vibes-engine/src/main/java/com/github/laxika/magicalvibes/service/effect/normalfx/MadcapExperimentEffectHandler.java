package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MadcapExperimentEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MadcapExperimentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MadcapExperimentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> revealedCards = new ArrayList<>();
        Card artifact = null;

        while (deck != null && !deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.ARTIFACT)) {
                artifact = card;
                break;
            }
        }

        int revealedCount = revealedCards.size();
        entry.setEventValue(revealedCount);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (!revealedCards.isEmpty()) {
            String revealedNames = revealedCards.stream()
                    .map(Card::getName)
                    .collect(Collectors.joining(", "));
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals " + revealedNames + " from the top of their library with "
                            + entry.getCard().getName() + "."));
        }

        if (artifact != null) {
            Permanent permanent = new Permanent(artifact);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(artifact, playerName));

            if (artifact.hasType(CardType.PLANESWALKER) && artifact.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, artifact.getLoyalty());
                permanent.setSummoningSick(false);
            }
            if (artifact.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, artifact, null, false);
            }
            revealedCards.remove(artifact);
        } else if (!revealedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library without finding an artifact card."));
        }

        if (deck != null && !revealedCards.isEmpty()) {
            Collections.shuffle(revealedCards);
            deck.addAll(revealedCards);
        }

        if (revealedCount > 0) {
            dealDamageToPlayersEffectHandler.resolve(
                    gameData, entry, new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER));
        }
    }
}
