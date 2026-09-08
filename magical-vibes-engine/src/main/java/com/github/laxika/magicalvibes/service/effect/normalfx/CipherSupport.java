package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CipherCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CipherSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    public CipherSupport(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            @Lazy InputCompletionService inputCompletionService
    ) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.inputCompletionService = inputCompletionService;
    }

    public void encode(GameData gameData, UUID creatureId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (entry == null || entry.isCopy() || creature == null
                || !gameQueryService.isCreature(gameData, creature)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        MayEffect trigger = new MayEffect(
                new CipherCastCopyEffect(entry.getCard().getId()),
                "Cast a copy of " + entry.getCard().getName() + " without paying its mana cost?");
        creature.addPersistentTriggeredEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, trigger);

        int resumeIndex = gameData.pendingEffectResolutionIndex;
        entry.setExileAndReturnToHandAtNextEndStep(false);
        entry.insertEffectsToResolve(resumeIndex, List.of(new ExileSpellEffect()));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" is encoded on ")
                .card(creature.getCard())
                .text(".")
                .build());

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
