package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeTargetOfTargetSpellWithSingleTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellWithSingleTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.isSingleTarget()) {
            
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " no longer has a single target)."));
            return;
        }

        boolean creatureTargetsOnly = ((ChangeTargetOfTargetSpellWithSingleTargetEffect) effect).creatureTargetsOnly();
        if (creatureTargetsOnly && !isCreatureId(gameData, targetSpell.getTargetId())) {
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " has no effect (", targetSpell.getCard(), " doesn't target a creature)."));
            return;
        }

        List<UUID> validNewTargets = targetRedirectionSupport.collectValidNewTargets(gameData, targetSpell);
        if (creatureTargetsOnly) {
            validNewTargets = validNewTargets.stream().filter(id -> isCreatureId(gameData, id)).toList();
        }
        if (validNewTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText("No legal new target for ", targetSpell.getCard(), "."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(targetSpell.getCard().getId()));
        playerInputService.beginPermanentChoice(
                gameData,
                entry.getControllerId(),
                validNewTargets,
                "Choose a new target for " + targetSpell.getCard().getName() + "."
        );
    }

    private boolean isCreatureId(GameData gameData, UUID id) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, id);
        return permanent != null && gameQueryService.isCreature(gameData, permanent);
    }
}
