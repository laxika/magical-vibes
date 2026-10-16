package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the Inquisitor Captain-style two-card seek: both sought cards go to hand, then one of
 * them is put onto the battlefield from there and the other is shuffled into the library.
 */
@Component
@RequiredArgsConstructor
public class SeekTwoCardsToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;
    private final SeekCardsToHandEffectHandler seekCardsToHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekTwoCardsToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekTwoCardsToBattlefieldEffect seek = (SeekTwoCardsToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> soughtCards = seekCardsToHandEffectHandler.seek(
                gameData, entry, new SeekCardsToHandEffect(new Fixed(2), seek.filter(), null));
        if (soughtCards.isEmpty()) {
            return;
        }

        if (soughtCards.size() == 1) {
            putOntoBattlefieldFromHand(gameData, controllerId, soughtCards.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId,
                soughtCards,
                soughtCards.stream().map(Card::getId).toList(),
                false,
                false,
                false,
                false,
                false,
                0,
                null,
                1,
                "Choose one of the sought creature cards to put onto the battlefield. "
                        + "Shuffle the other into your library.",
                false,
                1,
                false));
    }

    /** Whether the effect currently being resolved is this seek (possibly wrapped in a condition). */
    public static boolean isSeekTwoToBattlefield(CardEffect effect) {
        CardEffect current = effect;
        while (current instanceof com.github.laxika.magicalvibes.model.effect.ConditionalEffect conditional) {
            current = conditional.wrapped();
        }
        return current instanceof SeekTwoCardsToBattlefieldEffect;
    }

    /** Puts the chosen sought card onto the battlefield from hand and shuffles the other into the library. */
    public void completeChoice(GameData gameData, UUID controllerId, List<Card> soughtCards, List<UUID> chosenIds) {
        Set<UUID> chosen = Set.copyOf(chosenIds);
        List<Card> library = gameData.playerDecks.get(controllerId);
        for (Card card : soughtCards) {
            if (chosen.contains(card.getId())) {
                putOntoBattlefieldFromHand(gameData, controllerId, card);
            } else if (gameData.playerHands.get(controllerId).removeIf(c -> c.getId().equals(card.getId()))) {
                library.add(card);
            }
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }

    private void putOntoBattlefieldFromHand(GameData gameData, UUID controllerId, Card card) {
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.HAND)
                || !gameData.playerHands.get(controllerId).removeIf(c -> c.getId().equals(card.getId()))) {
            return;
        }
        String playerName = gameData.playerIdToName.get(controllerId);
        Permanent permanent = new Permanent(card, Zone.HAND);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, null, false);
    }
}
