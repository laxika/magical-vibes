package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect e = (LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect) effect;

        // The Aura and the enchanted creature are read with last-known information when either left
        // the battlefield before the trigger resolved (CR 608.2h).
        Permanent auraPerm = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enchantedSnapshot = entry.getAttachedPermanentSnapshot();
        UUID enchantedId = auraPerm != null && auraPerm.isAttached() ? auraPerm.getAttachedTo()
                : enchantedSnapshot != null ? enchantedSnapshot.getId() : null;
        Permanent enchantedCreature = enchantedId == null ? null
                : gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            enchantedCreature = enchantedSnapshot;
        }
        if (enchantedCreature == null) {
            log.info("Game {} - Call to the Kindred has no enchanted creature, effect does nothing", gameData.id);
            return;
        }

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        // Filter for creature cards that share a creature type with the enchanted creature
        Permanent enchanted = enchantedCreature;
        List<Card> matchingCards = topCards.stream()
                .filter(card -> card.getType() == CardType.CREATURE
                        || card.getAdditionalTypes().contains(CardType.CREATURE))
                .filter(card -> gameQueryService.shareCreatureType(gameData, enchanted, card))
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
                .prompt("You may put a creature card that shares a creature type with the enchanted creature onto the battlefield.")
                .destination(LibrarySearchDestination.BATTLEFIELD)
                .build(),
                "You may put a creature card that shares a creature type with the enchanted creature onto the battlefield.",
                true));
    
    }
}
