package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesTokenEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves an APNAP-ordered choice of one token option for each player. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesTokenEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerChoosesTokenEffect) effect;
        beginNextChoice(gameData, e, apnapPlayers(gameData), entry.getCard().getName(), Map.of());
    }

    /**
     * Records one player's token choice. Tokens are created only once every player has chosen, as the
     * choices are made in APNAP order and the resulting actions then happen simultaneously (CR 101.4).
     */
    public void completeChoice(GameData gameData, String label,
                               ChoiceContext.EachPlayerChoosesTokenChoice context,
                               UUID choosingPlayerId) {
        EachPlayerChoosesTokenEffect.TokenOption option = context.effect().options().stream()
                .filter(candidate -> candidate.label().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid token choice: " + label));

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Each-player token choice is not resumable");
        }
        Map<UUID, EachPlayerChoosesTokenEffect.TokenOption> chosenOptions = new LinkedHashMap<>(context.chosenOptions());
        chosenOptions.put(choosingPlayerId, option);
        if (context.remainingPlayerIds().isEmpty()) {
            chosenOptions.forEach((playerId, chosen) ->
                    createTokenEffectHandler.resolveForController(gameData, entry, chosen.token(), playerId));
            return;
        }
        beginNextChoice(gameData, context.effect(), context.remainingPlayerIds(), context.sourceName(), chosenOptions);
    }

    private void beginNextChoice(GameData gameData, EachPlayerChoosesTokenEffect effect,
                                 List<UUID> remainingPlayerIds, String sourceName,
                                 Map<UUID, EachPlayerChoosesTokenEffect.TokenOption> chosenOptions) {
        if (remainingPlayerIds.isEmpty()) {
            return;
        }

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> remaining = List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null,
                new ChoiceContext.EachPlayerChoosesTokenChoice(effect, remaining, chosenOptions, sourceName),
                effect.options().stream().map(EachPlayerChoosesTokenEffect.TokenOption::label).toList(),
                sourceName + " — choose a token."));
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
