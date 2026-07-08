package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyOneColorInstantSorceryOnlyManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwardAnyOneColorInstantSorceryOnlyManaEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardAnyOneColorInstantSorceryOnlyManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardAnyOneColorInstantSorceryOnlyManaEffect) effect;

        ChoiceContext.ManaColorChoice choiceContext =
                ChoiceContext.ManaColorChoice.instantSorceryOnly(entry.getControllerId(), e.amount());
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, colors,
                "Choose a color of mana to add (instant and sorcery spells only)."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose an instant/sorcery-only mana color", gameData.id, playerName);
    }
}
