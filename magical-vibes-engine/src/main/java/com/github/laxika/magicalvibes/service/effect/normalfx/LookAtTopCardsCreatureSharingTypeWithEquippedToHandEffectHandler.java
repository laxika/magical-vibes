package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves Conjurer's Mantle's equipped-creature type library trigger. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect) effect;
        UUID creatureId = entry.getTriggeringPermanentId();
        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, creatureId);
        if (equippedCreature == null) {
            Card lastKnownCard = entry.lastKnownPermanentCard(creatureId);
            if (lastKnownCard != null) {
                equippedCreature = new Permanent(lastKnownCard);
            }
        }
        if (equippedCreature == null) {
            return;
        }
        Permanent creatureForTypes = equippedCreature;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, typedEffect.count(), true);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        List<Card> matchingCards = topCards.stream()
                .filter(card -> gameQueryService.shareCreatureType(gameData, creatureForTypes, card))
                .toList();

        if (matchingCards.isEmpty()) {
            Collections.shuffle(topCards);
            gameData.playerDecks.get(controllerId).addAll(topCards);
            gameLogService.append(gameData, GameLog.text(result.playerName()
                    + " finds no creature card sharing a creature type with the equipped creature. "
                    + "The looked-at cards are put on the bottom of the library in a random order."));
            return;
        }

        String prompt = "You may reveal a card sharing a creature type with the equipped creature "
                + "from among them and put it into your hand. The rest go to the bottom of your library "
                + "in a random order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId,
                topCards,
                matchingCards.stream().map(Card::getId).toList(),
                false,
                true,
                false,
                true,
                false,
                0,
                null,
                1,
                prompt));
    }
}
