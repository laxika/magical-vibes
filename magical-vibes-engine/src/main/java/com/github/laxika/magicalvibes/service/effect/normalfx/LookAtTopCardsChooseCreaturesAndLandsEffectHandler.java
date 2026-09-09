package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseCreaturesAndLandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves the creature-and-land split used by Zimone's Experiment. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsChooseCreaturesAndLandsEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsChooseCreaturesAndLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsChooseCreaturesAndLandsEffect typedEffect =
                (LookAtTopCardsChooseCreaturesAndLandsEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, typedEffect.count(), true);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        List<Card> eligibleCards = topCards.stream()
                .filter(card -> card.hasType(CardType.CREATURE) || card.hasType(CardType.LAND))
                .toList();

        if (eligibleCards.isEmpty()) {
            Collections.shuffle(topCards);
            gameData.playerDecks.get(controllerId).addAll(topCards);
            gameLogService.append(gameData, GameLog.text(result.playerName()
                    + " finds no creature or land cards. All cards are put on the bottom of their library in a random order."));
            return;
        }

        int maxCount = Math.min(2, eligibleCards.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId,
                topCards,
                eligibleCards.stream().map(Card::getId).toList(),
                false,
                false,
                false,
                true,
                false,
                0,
                null,
                maxCount,
                "You may reveal up to " + maxCount + " creature and/or land cards from among them.",
                true,
                0,
                false).withSelectedCardsToBattlefieldType(CardType.LAND));
    }
}
