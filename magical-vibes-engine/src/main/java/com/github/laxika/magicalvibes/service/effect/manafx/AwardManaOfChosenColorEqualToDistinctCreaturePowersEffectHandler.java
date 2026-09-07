package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEqualToDistinctCreaturePowersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class AwardManaOfChosenColorEqualToDistinctCreaturePowersEffectHandler
        implements ManaAbilityEffectHandler {

    private static final List<String> COLORS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public AwardManaOfChosenColorEqualToDistinctCreaturePowersEffectHandler(
            GameQueryService gameQueryService, InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfChosenColorEqualToDistinctCreaturePowersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        Set<Integer> distinctPowers = new HashSet<>();
        for (Permanent controlledPermanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.isCreature(gameData, controlledPermanent)) {
                distinctPowers.add(gameQueryService.getEffectivePower(gameData, controlledPermanent));
            }
        }

        ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(
                playerId, creatureSource, distinctPowers.size() * manaMultiplier);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, COLORS,
                "Choose a color of mana to add."));
    }

    @Override
    public boolean isRevertable() {
        return true;
    }
}
