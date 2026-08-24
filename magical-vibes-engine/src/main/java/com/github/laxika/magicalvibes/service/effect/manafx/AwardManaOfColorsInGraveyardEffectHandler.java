package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AwardManaOfColorsInGraveyardEffectHandler implements ManaAbilityEffectHandler {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public AwardManaOfColorsInGraveyardEffectHandler(InteractionHandlerRegistry interactionHandlerRegistry) {
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfColorsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        List<ManaColor> availableColors = ManaColor.COLORS.stream()
                .filter(color -> hasColorInGraveyard(gameData, playerId, color))
                .toList();
        if (availableColors.isEmpty()) {
            return;
        }

        if (availableColors.size() == 1) {
            addMana(gameData, playerId, gameData.playerManaPools.get(playerId), availableColors.getFirst(), manaMultiplier,
                    creatureSource);
            return;
        }

        ChoiceContext.ManaColorChoice choiceContext =
                new ChoiceContext.ManaColorChoice(playerId, creatureSource, manaMultiplier);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext,
                availableColors.stream().map(Enum::name).sorted().toList(),
                "Choose a color of a card in your graveyard."));
    }

    @Override
    public boolean isRevertable() {
        return true;
    }

    private static boolean hasColorInGraveyard(GameData gameData, UUID playerId, ManaColor manaColor) {
        CardColor cardColor = CardColor.valueOf(manaColor.name());
        return gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .anyMatch(card -> card.getColors().contains(cardColor));
    }

    private static void addMana(GameData gameData, UUID sourceControllerId, ManaPool manaPool,
                                ManaColor color, int amount, boolean creatureSource) {
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, sourceControllerId, color);
        manaPool.add(effectiveColor, amount);
        if (creatureSource) {
            manaPool.addCreatureMana(effectiveColor, amount);
        }
    }
}
