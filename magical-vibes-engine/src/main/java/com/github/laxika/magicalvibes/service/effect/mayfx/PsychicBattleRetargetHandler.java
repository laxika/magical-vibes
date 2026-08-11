package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleRetargetEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PsychicBattleSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PsychicBattleRetargetHandler implements MayEffectHandlerBean {

    private final PsychicBattleSupport psychicBattleSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PsychicBattleRetargetEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PsychicBattleRetargetEffect retarget = ability.effects().stream()
                .filter(PsychicBattleRetargetEffect.class::isInstance)
                .map(PsychicBattleRetargetEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (!accepted) {
            continueChoices(gameData, ability, retarget.targetIndex() + 1);
            return;
        }

        StackEntry targetEntry = psychicBattleSupport.findTargetEntry(gameData, retarget.spellCardId());
        if (targetEntry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        var validTargets = psychicBattleSupport.collectLegalAlternatives(
                gameData, targetEntry, retarget.targetIndex());
        if (validTargets.isEmpty()) {
            continueChoices(gameData, ability, retarget.targetIndex() + 1);
            return;
        }

        psychicBattleSupport.beginPermanentChoice(
                gameData, ability.sourceCard(), ability.controllerId(),
                retarget.spellCardId(), retarget.targetIndex());
    }

    private void continueChoices(GameData gameData, PendingMayAbility ability, int nextIndex) {
        boolean queued = psychicBattleSupport.queueNextChoice(
                gameData, ability.sourceCard(), ability.controllerId(),
                ((PsychicBattleRetargetEffect) ability.effects().getFirst()).spellCardId(), nextIndex);
        if (queued) {
            playerInputService.processNextMayAbility(gameData);
        } else {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
