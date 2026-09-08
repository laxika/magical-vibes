package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastOneWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resolves the shared top-library free-cast flow with a resolution-time mana-value cap. */
@Component
@RequiredArgsConstructor
@Slf4j
public class LookAtTopCardsMayCastOneWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayCastOneWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsMayCastOneWithoutPayingManaCostEffect topCardsEffect =
                (LookAtTopCardsMayCastOneWithoutPayingManaCostEffect) effect;

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int lookCount = Math.max(0, amountEvaluationService.evaluate(
                gameData, topCardsEffect.lookCount(), context));
        int maxManaValue = Math.max(0, amountEvaluationService.evaluate(
                gameData, topCardsEffect.maxManaValue(), context));

        if (lookCount == 0) {
            return;
        }

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, lookCount, true);
        if (result == null) {
            return;
        }

        List<Card> castable = result.topCards().stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .toList();
        if (castable.isEmpty()) {
            Collections.shuffle(result.topCards());
            gameData.playerDecks.get(result.controllerId()).addAll(result.topCards());
            return;
        }

        String prompt = "You may cast a spell with mana value less than or equal to "
                + maxManaValue + " from among the top " + result.topCards().size()
                + " cards without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(result.controllerId(), castable)
                        .reveals(false)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(result.topCards()))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
        log.info("Game {} - {} looks at the top {} cards for a spell with mana value at most {}",
                gameData.id, result.playerName(), result.topCards().size(), maxManaValue);
    }
}
