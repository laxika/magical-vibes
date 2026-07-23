package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link AwardManaOfColorsEffect} on the stack (e.g. Burnt Offering: "Add X mana in any
 * combination of {B} and/or {R}"), mirroring the mana-ability path in
 * {@code ActivatedAbilityExecutionService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaOfColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaOfColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaOfColorsEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int picks = Math.max(0, amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source)));
        if (picks <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        boolean fromCreature = source != null && gameQueryService.isCreature(gameData, source);

        if (e.colors().size() == 1) {
            ManaColor manaColor = e.colors().get(0);
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            pool.add(manaColor, picks);
            if (fromCreature) {
                pool.addCreatureMana(manaColor, picks);
            }
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " adds " + picks + " " + manaColor.getCode() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds {} {}", gameData.id, playerName, picks, manaColor);
            return;
        }

        ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice
                .fixedColorCombination(controllerId, fromCreature, picks, e.colors());
        List<String> colors = e.colors().stream().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, choiceContext, colors, "Choose a color of mana to add."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a mana color from a fixed set (amount={})",
                gameData.id, playerName, picks);
    }
}
