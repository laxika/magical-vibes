package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfChosenColorEqualToDevotionEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AwardManaOfChosenColorEqualToDevotionEffectHandler implements ManaAbilityEffectHandler {

    private static final List<String> COLORS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public AwardManaOfChosenColorEqualToDevotionEffectHandler(
            InteractionHandlerRegistry interactionHandlerRegistry) {
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfChosenColorEqualToDevotionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        int manaMultiplier, boolean creatureSource) {
        ChoiceContext.DevotionManaColorChoice context = new ChoiceContext.DevotionManaColorChoice(
                playerId, permanent.getId(), creatureSource, manaMultiplier);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, COLORS, "Choose a color of mana to add."));
    }

    @Override
    public boolean isRevertable() {
        return true;
    }
}
