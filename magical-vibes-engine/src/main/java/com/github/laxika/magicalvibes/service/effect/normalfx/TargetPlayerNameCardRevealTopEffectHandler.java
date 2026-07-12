package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerNameCardRevealTopEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerNameCardRevealTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerNameCardRevealTopEffect revealEffect = (TargetPlayerNameCardRevealTopEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetPlayerId)) return;

        // The targeted player is the one who names the card.
        var choiceContext = new ChoiceContext.TargetPlayerNameCardRevealTopChoice(
                entry.getControllerId(), targetPlayerId, entry.getSourcePermanentId(),
                revealEffect.damageOnMiss());

        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                targetPlayerId, null, null, choiceContext, cardNames, "Choose a card name."));

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name ({})", gameData.id, playerName, entry.getCard().getName());
    }
}
