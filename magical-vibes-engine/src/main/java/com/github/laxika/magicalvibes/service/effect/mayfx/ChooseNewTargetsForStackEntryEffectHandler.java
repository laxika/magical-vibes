package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForStackEntryEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PsychicBattleSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseNewTargetsForStackEntryEffectHandler implements MayEffectHandlerBean {

    private final PsychicBattleSupport psychicBattleSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNewTargetsForStackEntryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ChooseNewTargetsForStackEntryEffect retarget = ability.effects().stream()
                .filter(ChooseNewTargetsForStackEntryEffect.class::isInstance)
                .map(ChooseNewTargetsForStackEntryEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (!accepted) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry targetEntry = psychicBattleSupport.findTargetEntry(gameData, retarget.stackEntryId());
        if (targetEntry == null || targetEntry.isNonTargeting()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<UUID> validTargets = psychicBattleSupport.collectLegalAlternatives(
                gameData, targetEntry, retarget.targetIndex());
        if (validTargets.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SpellRetarget(retarget.stackEntryId(), retarget.targetIndex()));
        playerInputService.beginPermanentChoice(
                gameData,
                ability.controllerId(),
                validTargets,
                "Choose a new target for " + targetEntry.getDescription() + ".");
    }
}
