package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PreventNextDamageByTargetCreatureEffect}: a one-shot {@link SourceNextDamageToAnyTargetShield} keyed
 * to each source the effect names (a targeted creature only while it is still a creature), multiplying its next damage
 * event by 0 (prevention) or 2 (doubling). A source that isn't a permanent (a chosen spell) is keyed by its id, so the
 * shield also covers the permanent that spell becomes (CR 609.7a).
 */
@Component
@RequiredArgsConstructor
public class PreventNextDamageByTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final DamageSourceChoiceSupport damageSourceChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageByTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventNextDamageByTargetCreatureEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }
        List<UUID> sourceIds = e.source().declaresTarget()
                ? subjectResolutionService.permanents(gameData, entry, effect, e.source()).stream()
                        .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                        .map(Permanent::getId)
                        .toList()
                : subjectResolutionService.damageSourceIds(gameData, entry, effect, e.source());
        for (UUID sourceId : sourceIds) {
            Card sourceCard = damageSourceChoiceSupport.sourceCard(gameData, sourceId);
            gameData.addShield(new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, null,
                    e.doubling() ? 2 : 0, e.gainLife() ? controllerId : null, e.combatOnly()));
            if (sourceCard == null) {
                continue;
            }
            gameData.addWaitingDamageSource(sourceId, sourceCard);
            Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
            String sourceName = permanent != null ? FaceDownVisibility.publicName(gameData, permanent)
                    : FaceDownVisibility.publicName(gameData, sourceCard);
            String damage = e.combatOnly() ? "combat damage" : "damage";
            gameLogService.append(gameData, GameLog.text("The next time " + sourceName + " would deal " + damage
                    + " this turn, " + (e.doubling() ? "it deals double that damage instead."
                    : "that damage is prevented." + (e.gainLife()
                    ? " " + gameData.playerIdToName.get(controllerId) + " gains life equal to the damage prevented."
                    : ""))));
        }
    }
}
