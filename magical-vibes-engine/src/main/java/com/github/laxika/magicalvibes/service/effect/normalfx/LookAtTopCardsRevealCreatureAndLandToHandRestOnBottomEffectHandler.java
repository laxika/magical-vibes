package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Look at the top N cards of your library, may reveal a creature card and/or a land card and put the
 * revealed cards into your hand, then bottom the rest (Gift of the Gargantuan). The at-most-one-per-
 * type bound is enforced by running two sequential single-card {@link PendingInteraction.LibrarySearch}
 * picks over the same looked-at cards: first the creature (carrying {@link LibrarySearchFollowUp#forGiftLandPick()}),
 * then the land (which bottoms the remaining cards on completion). When no creature is present the
 * land pick begins directly; when neither is present the looked-at cards are bottomed immediately.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffect e =
                (LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffect) effect;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();

        List<Card> creatures = topCards.stream().filter(c -> c.hasType(CardType.CREATURE)).toList();
        List<Card> lands = topCards.stream().filter(c -> c.hasType(CardType.LAND)).toList();

        // Nothing eligible — bottom everything in any order.
        if (creatures.isEmpty() && lands.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, topCards);
            return;
        }

        if (!creatures.isEmpty()) {
            // First pick: the creature. The land follow-up runs (and bottoms the rest) afterwards.
            beginPick(gameData, controllerId, creatures, topCards,
                    "You may reveal a creature card from among them and put it into your hand.",
                    LibrarySearchFollowUp.forGiftLandPick());
            return;
        }

        // No creature among the looked-at cards — go straight to the land pick, which bottoms the rest.
        beginPick(gameData, controllerId, lands, topCards,
                "You may reveal a land card from among them and put it into your hand.",
                LibrarySearchFollowUp.NONE);
    }

    private void beginPick(GameData gameData, UUID controllerId, List<Card> eligible,
            List<Card> lookedAtCards, String prompt, LibrarySearchFollowUp followUp) {
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .sourceCards(lookedAtCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .followUp(followUp)
                .prompt(prompt)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(params, prompt, true));
    }
}
