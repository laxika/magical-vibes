package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardFromHandDealDamageEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cursed Scroll: pauses resolution so the controller can name a card; the random reveal from their
 * hand and the conditional damage are finished by {@code ChoiceHandlerService} once the name comes
 * back.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseNameRevealRandomCardFromHandDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameRevealRandomCardFromHandDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNameRevealRandomCardFromHandDealDamageEffect) effect;

        if (entry.getTargetId() == null) {
            return;
        }

        var choiceContext = new ChoiceContext.ChooseNameRevealRandomHandCardDamageChoice(
                entry.getControllerId(), entry.getTargetId(), entry.getSourcePermanentId(),
                entry.getCard(), e.damage());

        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, "Choose a card name."));

        log.info("Game {} - Awaiting {} to choose a card name ({})",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
