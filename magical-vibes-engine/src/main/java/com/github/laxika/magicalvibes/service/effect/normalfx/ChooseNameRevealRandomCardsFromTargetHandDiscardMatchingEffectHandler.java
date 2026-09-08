package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Nebuchadnezzar's name choice and random hand reveal. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNameRevealRandomCardsFromTargetHandDiscardMatchingEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }

        int count = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, null));
        var choiceContext = new ChoiceContext.ChooseNameRevealRandomHandCardsDiscardChoice(
                entry.getControllerId(), entry.getTargetId(), entry.getCard(), count);

        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, "Choose a card name."));

        log.info("Game {} - Awaiting {} to choose a card name ({})",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
