package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithChosenNameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenWithChosenNameEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithChosenNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenWithChosenNameEffect chosenNameEffect = (CreateTokenWithChosenNameEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext amountContext = AmountContext.forStackEntry(entry, source)
                .withControllerId(entry.getControllerId());
        int amount = amountEvaluationService.evaluate(gameData,
                chosenNameEffect.tokenTemplate().amount(), amountContext);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData,
                chosenNameEffect.tokenTemplate().power(), amountContext);
        int toughness = amountEvaluationService.evaluate(gameData,
                chosenNameEffect.tokenTemplate().toughness(), amountContext);

        ChoiceContext.CreateTokenWithChosenNameChoice choiceContext =
                new ChoiceContext.CreateTokenWithChosenNameChoice(
                        entry.getCard(), entry.getControllerId(), chosenNameEffect.tokenTemplate(),
                        amount, power, toughness);
        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames,
                "Choose a name for the token."));

        log.info("Game {} - Awaiting {} to choose a name for a token created by {}",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
