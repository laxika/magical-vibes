package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardNameAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardNameAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        begin(gameData, entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                (ChooseCardNameAtResolutionEffect) effect, null);
    }

    /**
     * Starts the name choice. A non-null {@code followUpSubtypeChoice} is made right after the name, for an
     * "as this becomes attached" pair of choices that is not a triggered ability.
     */
    public void begin(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                      ChooseCardNameAtResolutionEffect chooseEffect,
                      ChooseSubtypeForSourceEffect followUpSubtypeChoice) {
        var choiceContext = new ChoiceContext.ChooseCardNameAtResolutionChoice(
                sourceCard, controllerId, sourcePermanentId, chooseEffect.requiredType(), followUpSubtypeChoice);
        List<String> cardNames = chooseEffect.requiredType() == null
                ? libraryRevealSupport.collectAllCardNamesInGame(gameData)
                : libraryRevealSupport.collectCardNamesInGameOfType(gameData, chooseEffect.requiredType());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, choiceContext, cardNames,
                chooseEffect.requiredType() == null
                        ? "Choose a card name."
                        : "Choose a " + chooseEffect.requiredType().getDisplayName().toLowerCase() + " card name."));

        log.info("Game {} - Awaiting {} to choose a card name for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), sourceCard.getName());
    }
}
