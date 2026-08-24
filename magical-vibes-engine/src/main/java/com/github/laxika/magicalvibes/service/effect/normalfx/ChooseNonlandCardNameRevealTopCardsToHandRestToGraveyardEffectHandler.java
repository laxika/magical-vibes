package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect) effect;
        var choiceContext = new ChoiceContext.ChooseNonlandCardNameRevealTopCardsChoice(
                entry.getControllerId(), entry.getCard(), e.count());

        List<String> cardNames = libraryRevealSupport.collectNonlandCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames,
                "Choose a nonland card name."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a nonland card name ({})",
                gameData.id, playerName, entry.getCard().getName());
    }
}
