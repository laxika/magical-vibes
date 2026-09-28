package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TeachCastCopyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TeachSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    public TeachSupport(GameQueryService gameQueryService,
                        GameLogService gameLogService,
                        @Lazy InputCompletionService inputCompletionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.inputCompletionService = inputCompletionService;
    }

    public void teach(GameData gameData, UUID creatureId) {
        teach(gameData, gameData.pendingEffectResolutionEntry, creatureId,
                gameData.pendingEffectResolutionIndex, true);
    }

    public void teachWithoutChoice(GameData gameData, StackEntry entry, int resumeIndex) {
        teach(gameData, entry, null, resumeIndex, false);
    }

    private void teach(GameData gameData, StackEntry entry, UUID creatureId,
                       int resumeIndex, boolean resume) {
        if (entry == null || entry.isCopy()) {
            if (resume) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        Permanent creature = creatureId == null ? null
                : gameQueryService.findPermanentById(gameData, creatureId);
        if (creature != null && gameQueryService.isCreature(gameData, creature)) {
            ActivatedAbility ability = new ActivatedAbility(
                    true,
                    null,
                    List.of(new TeachCastCopyEffect(entry.getCard().getId())),
                    "{T}: Copy the exiled card. You may cast the copy for its teach cost.");
            GrantActivatedAbilityEffect grant = new GrantActivatedAbilityEffect(
                    ability,
                    GrantScope.TARGET,
                    null,
                    EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE,
                    entry.getCard().getId());
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    null,
                    entry.getControllerId(),
                    grant,
                    creature.getId(),
                    null,
                    null,
                    EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE,
                    0));
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" is taught on ")
                    .card(creature.getCard())
                    .text(".")
                    .build());
        }

        entry.setExileAndReturnToHandAtNextEndStep(false);
        entry.insertEffectsToResolve(resumeIndex, List.of(new ExileSpellEffect()));
        if (resume) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
