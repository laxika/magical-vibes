package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageAndRedirectEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventNextDamageAndRedirectEffect}: adds a {@link DamageRedirectShield} for the evaluated amount
 * over its controller (and their permanents) or over the target creature; each point it prevents is dealt by the
 * ability's source to the redirect target. Nothing happens without a positive amount, when the protected creature is
 * no longer a creature on the battlefield, or when the entry's own target is gone; a shield on a target creature
 * still prevents when its separate redirect target became illegal (CR 608.2b), and then deals nothing.
 */
@Component
@RequiredArgsConstructor
public class PreventNextDamageAndRedirectEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageAndRedirectEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var shield = (PreventNextDamageAndRedirectEffect) effect;
        UUID controllerId = entry.getControllerId();

        Permanent protectedCreature = null;
        if (shield.protectedPermanent() != null) {
            protectedCreature = subjectResolutionService.permanent(gameData, entry, effect, shield.protectedPermanent());
            if (protectedCreature == null || !gameQueryService.isCreature(gameData, protectedCreature)) {
                return;
            }
        }

        UUID redirectTargetId;
        if (shield.redirectTargetGroup() == null) {
            redirectTargetId = entry.getTargetId();
        } else {
            List<UUID> redirectTargets = entry.targetsForGroup(shield.redirectTargetGroup());
            redirectTargetId = redirectTargets.isEmpty() ? null : redirectTargets.getFirst();
        }
        if (redirectTargetId == null && shield.redirectTargetGroup() == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, shield.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        gameData.addShield(new DamageRedirectShield(controllerId, amount, entry.getSourcePermanentId(),
                entry.getCard(), redirectTargetId, shield.andPermanentsYouControl(),
                protectedCreature == null ? null : protectedCreature.getId()));

        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        String protectedName = protectedCreature != null
                ? FaceDownVisibility.publicName(gameData, protectedCreature)
                : gameData.playerIdToName.get(controllerId)
                        + (shield.andPermanentsYouControl() ? " and/or the permanents they control" : "");
        gameLogService.append(gameData, GameLog.text(sourceName + " — the next " + amount
                + " damage that would be dealt to " + protectedName + " this turn is prevented. If damage is"
                + " prevented this way, " + sourceName + " deals that much damage to " + redirectName(gameData,
                redirectTargetId) + "."));
    }

    private String redirectName(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return "its target";
        }
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) {
            return playerName;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        return permanent == null ? "its target" : FaceDownVisibility.publicName(gameData, permanent);
    }
}
