package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect e = (LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        // Collect all permanent names from all battlefields
        Set<String> permanentNames = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent perm : bf) {
                    permanentNames.add(perm.getCard().getName());
                }
            }
        }

        // Filter top cards to those matching a permanent name
        List<Card> matchingCards = topCards.stream()
                .filter(card -> permanentNames.contains(card.getName()))
                .toList();

        if (matchingCards.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, matchingCards)
                .canFailToFind(true)
                .sourceCards(topCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("You may put one of these cards onto the battlefield.")
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .build(),
                "You may put one of these cards onto the battlefield if it has the same name as a permanent.",
                true));
    
    }
}
