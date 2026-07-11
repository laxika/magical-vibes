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

        // Find the source aura permanent and the enchanted creature
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            log.warn("Game {} - No source permanent for Call to the Kindred effect", gameData.id);
            return;
        }
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (auraPerm == null || !auraPerm.isAttached()) {
            log.info("Game {} - Aura no longer on battlefield or not attached, effect does nothing", gameData.id);
            return;
        }
        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, auraPerm.getAttachedTo());
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, effect does nothing", gameData.id);
            return;
        }

        // Collect the enchanted creature's creature subtypes (including transient subtypes)
        List<CardSubtype> enchantedTypes = new ArrayList<>(enchantedCreature.getCard().getSubtypes());
        enchantedTypes.addAll(enchantedCreature.getTransientSubtypes());
        boolean enchantedIsChangeling = enchantedCreature.hasKeyword(Keyword.CHANGELING);

        if (enchantedTypes.isEmpty() && !enchantedIsChangeling) {
            // Enchanted creature has no creature types — no card can share a type
            LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
            if (result == null) return;
            libraryRevealSupport.reorderRemainingToBottom(gameData, result.controllerId(), result.topCards());
            return;
        }

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        // Filter for creature cards that share a creature type with the enchanted creature
        List<Card> matchingCards = topCards.stream()
                .filter(card -> card.getType() == CardType.CREATURE
                        || card.getAdditionalTypes().contains(CardType.CREATURE))
                .filter(card -> {
                    List<CardSubtype> cardTypes = card.getSubtypes();
                    boolean cardIsChangeling = card.getKeywords().contains(Keyword.CHANGELING);

                    return (enchantedIsChangeling && (cardIsChangeling || !cardTypes.isEmpty()))
                            || (cardIsChangeling && !enchantedTypes.isEmpty())
                            || enchantedTypes.stream().anyMatch(cardTypes::contains);
                })
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
