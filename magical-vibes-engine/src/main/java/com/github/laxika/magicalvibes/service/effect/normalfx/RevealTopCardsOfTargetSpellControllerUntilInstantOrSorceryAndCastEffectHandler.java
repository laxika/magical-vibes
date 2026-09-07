package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Spellshift's library reveal and free-cast rider. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = findTargetSpell(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        UUID targetControllerId = targetSpell.getControllerId();
        List<Card> library = gameData.playerDecks.get(targetControllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!library.isEmpty()) {
            Card card = library.removeFirst();
            revealed.add(card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(targetControllerId) + " reveals ")
                    .card(card).text(" from the top of their library.").build());
            if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
                hit = card;
                break;
            }
        }

        if (hit == null) {
            library.addAll(revealed);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetControllerId);
            return;
        }

        String prompt = "You may cast " + hit.getName() + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(targetControllerId, List.of(hit))
                        .targetPlayerId(targetControllerId)
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(revealed)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(true)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY)
                        .build(),
                prompt,
                true));
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)) {
                return stackEntry;
            }
        }
        return null;
    }
}
