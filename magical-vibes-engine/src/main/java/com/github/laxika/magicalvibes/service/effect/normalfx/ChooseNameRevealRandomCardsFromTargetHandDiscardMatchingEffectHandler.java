package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Nebuchadnezzar's name-choice, random-reveal, and matching-discard ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }

        var choiceContext = new ChoiceContext.ChooseNameRevealRandomCardsDiscardMatchingChoice(
                entry.getControllerId(), entry.getTargetId(), entry.getCard(), Math.max(0, entry.getXValue()));
        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, "Choose a card name."));

        log.info("Game {} - Awaiting {} to choose a card name ({})",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
