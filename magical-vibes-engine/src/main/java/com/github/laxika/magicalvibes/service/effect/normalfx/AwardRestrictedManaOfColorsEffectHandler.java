package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a fixed-color restricted mana effect placed on the stack. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardRestrictedManaOfColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardRestrictedManaOfColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardRestrictedManaOfColorsEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int picks = Math.max(0, amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source)));
        if (picks <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (e.colors().size() == 1) {
            ManaColor manaColor = ManaProductionSupport.effectiveColor(
                    gameData, controllerId, e.colors().getFirst());
            e.restriction().applyTo(gameData.playerManaPools.get(controllerId), manaColor, picks);
            appendLog(gameData, controllerId, picks, manaColor, e);
            return;
        }

        List<String> colors = e.colors().stream().map(Enum::name).toList();
        ChoiceContext.RestrictedManaColorChoice choiceContext =
                new ChoiceContext.RestrictedManaColorChoice(controllerId, false, picks,
                        e.colors(), e.restriction());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
    }

    private void appendLog(GameData gameData, UUID controllerId, int amount, ManaColor color,
                            AwardRestrictedManaOfColorsEffect effect) {
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + amount + " "
                + color.getCode() + " (" + effect.restriction().description() + ")."));
        log.info("Game {} - {} adds {} {} restricted mana ({})", gameData.id, playerName,
                amount, color, effect.restriction().description());
    }
}
