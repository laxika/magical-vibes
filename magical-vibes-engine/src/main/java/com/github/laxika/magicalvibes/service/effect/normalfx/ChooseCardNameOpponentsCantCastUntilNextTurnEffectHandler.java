package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOpponentsCantCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardNameOpponentsCantCastUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardNameOpponentsCantCastUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseCardNameOpponentsCantCastUntilNextTurnEffect chooseName =
                (ChooseCardNameOpponentsCantCastUntilNextTurnEffect) effect;
        List<String> cardNames = playerInputService.cardNamesOfferedTo(
                gameData, entry.getControllerId(), chooseName.excludedTypes(), null);
        var choiceContext = new ChoiceContext.OpponentsCantCastNamedSpellsUntilNextTurnChoice(
                entry.getControllerId(), chooseName.excludedTypes());
        String prompt = chooseName.excludedTypes().isEmpty()
                ? "Choose a card name."
                : "Choose a non" + chooseName.excludedTypes().stream()
                        .map(type -> type.name().toLowerCase())
                        .reduce((first, second) -> first + "/" + second)
                        .orElse("") + " card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a card name ({})",
                gameData.id, playerName, entry.getCard().getName());
    }
}
