package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameForDelayedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardNameForDelayedCreatureCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardNameForDelayedCreatureCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chooseEffect = (ChooseCardNameForDelayedCreatureCombatDamageEffect) effect;
        var choiceContext = new ChoiceContext.ChooseCardNameForDelayedCreatureCombatDamageChoice(
                entry.getControllerId(), chooseEffect.effects(), entry.getCard(),
                chooseEffect.combatDamageToPlayerOnly(), chooseEffect.untilEndOfTurn());
        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, "Choose a card name."));

        log.info("Game {} - Awaiting {} to choose a card name for {}",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), entry.getCard().getName());
    }
}
