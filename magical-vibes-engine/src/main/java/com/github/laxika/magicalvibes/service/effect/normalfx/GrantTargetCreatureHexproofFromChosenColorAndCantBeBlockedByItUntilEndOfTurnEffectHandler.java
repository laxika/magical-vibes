package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private static final List<String> COLORS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null || gameQueryService.findPermanentById(gameData, targetId) == null) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null,
                new ChoiceContext.TargetCreatureHexproofFromChosenColorChoice(targetId),
                COLORS, "Choose a color."));
    }
}
