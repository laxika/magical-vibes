package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfSourceCardColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the manabond land ability using the activating permanent's current color(s). */
@Component
public class AwardManaOfSourceCardColorsEffectHandler implements ManaAbilityEffectHandler {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public AwardManaOfSourceCardColorsEffectHandler(GameQueryService gameQueryService,
                                                    InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfSourceCardColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        List<ManaColor> colors = availableColors(gameData, playerId, permanent, effect);
        if (colors.size() == 1) {
            addMana(gameData, playerId, permanent, colors.getFirst(), manaMultiplier, creatureSource);
            return;
        }
        if (colors.isEmpty()) {
            return;
        }

        ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(
                playerId, creatureSource, manaMultiplier).withSourcePermanentId(permanent.getId());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext,
                colors.stream().map(Enum::name).toList(),
                "Choose a color of this card's color."));
    }

    @Override
    public int calculateManaProduction(GameData gameData, UUID playerId, Permanent permanent,
                                       CardEffect effect, int xValue) {
        return availableColors(gameData, playerId, permanent, effect).isEmpty() ? 0 : 1;
    }

    @Override
    public List<ManaColor> availableManaColors(GameData gameData, UUID playerId,
                                               Permanent permanent, CardEffect effect) {
        return availableColors(gameData, playerId, permanent, effect);
    }

    @Override
    public boolean isRevertable() {
        return true;
    }

    private List<ManaColor> availableColors(GameData gameData, UUID playerId, Permanent permanent,
                                            CardEffect effect) {
        List<ManaColor> colors = ManaColor.COLORS.stream()
                .filter(color -> gameQueryService.getEffectiveColors(gameData, permanent)
                        .contains(CardColor.valueOf(color.name())))
                .toList();
        return colors.isEmpty() ? List.of(ManaColor.COLORLESS) : colors;
    }

    private static void addMana(GameData gameData, UUID playerId, Permanent permanent,
                                ManaColor color, int amount, boolean creatureSource) {
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, playerId,
                permanent, color);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        pool.add(effectiveColor, amount);
        if (creatureSource) {
            pool.addCreatureMana(effectiveColor, amount);
        }
    }
}
