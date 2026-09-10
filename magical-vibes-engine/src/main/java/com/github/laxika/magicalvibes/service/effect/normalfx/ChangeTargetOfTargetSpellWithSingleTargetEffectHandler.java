package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.Objects;
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
    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeTargetOfTargetSpellWithSingleTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolve(gameData, entry.getControllerId(), entry.getTargetId(), entry.getCard(),
                entry.getSourcePermanentId(), effect);
    }

    public void resolve(GameData gameData, UUID controllerId, UUID targetCardId, Card sourceCard, CardEffect effect) {
        resolve(gameData, controllerId, targetCardId, sourceCard, null, effect);
    }

    private void resolve(GameData gameData, UUID controllerId, UUID targetCardId, Card sourceCard,
                         UUID sourcePermanentId, CardEffect effect) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetCardId);
        if (targetSpell == null) {
            return;
        }

        ChangeTargetOfTargetSpellWithSingleTargetEffect redirectEffect =
                (ChangeTargetOfTargetSpellWithSingleTargetEffect) effect;
        List<UUID> occurrences = psychicBattleSupport.targetIds(targetSpell);
        boolean onlySource = !occurrences.isEmpty()
                && occurrences.stream().allMatch(id -> Objects.equals(id, sourcePermanentId));
        if (!targetSpell.isSingleTarget() && !(redirectEffect.requiresSourceTarget() && onlySource)) {
            
            gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " has no effect (", targetSpell.getCard(), " no longer has a single target)."));
            return;
        }

        if (redirectEffect.requiresSourceTarget()
                && !onlySource) {
            gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " has no effect (", targetSpell.getCard(),
                    " doesn't target this creature)."));
            return;
        }

        boolean creatureTargetsOnly = redirectEffect.creatureTargetsOnly();
        if (creatureTargetsOnly && (occurrences.isEmpty() || !isCreatureId(gameData, occurrences.getFirst()))) {
            gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " has no effect (", targetSpell.getCard(), " doesn't target a creature)."));
            return;
        }

        boolean multipleOccurrences = !targetSpell.isSingleTarget();
        List<UUID> validNewTargets = multipleOccurrences
                ? psychicBattleSupport.collectLegalAlternatives(gameData, targetSpell, 0)
                : targetRedirectionSupport.collectValidNewTargets(gameData, targetSpell);
        if (creatureTargetsOnly) {
            validNewTargets = validNewTargets.stream().filter(id -> isCreatureId(gameData, id)).toList();
        } else if (redirectEffect.playerTargetsOnly()) {
            validNewTargets = validNewTargets.stream().filter(gameData.orderedPlayerIds::contains).toList();
        }
        if (validNewTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText("No legal new target for ", targetSpell.getCard(), "."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(
                targetSpell.getCard().getId(), multipleOccurrences ? 0 : null));
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                validNewTargets,
                "Choose a new target for " + targetSpell.getCard().getName() + "."
        );
    }

    private boolean isCreatureId(GameData gameData, UUID id) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, id);
        return permanent != null && gameQueryService.isCreature(gameData, permanent);
    }
}
