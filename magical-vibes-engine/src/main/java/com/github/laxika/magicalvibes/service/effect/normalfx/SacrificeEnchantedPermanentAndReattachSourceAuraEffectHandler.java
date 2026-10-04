package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedPermanentAndReattachSourceAuraEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeEnchantedPermanentAndReattachSourceAuraEffect} (Nettlevine Blight):
 * the enchanted permanent's controller sacrifices it, then moves the source Aura onto a matching
 * permanent they control. The Aura keeps its controller. If that player has no legal destination,
 * the enchanted permanent is still sacrificed and the Aura is left unattached (removed as an SBA).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedPermanentAndReattachSourceAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        boolean auraOnBattlefield = aura != null;
        if (aura == null) aura = entry.getSourcePermanentSnapshot();
        if (aura == null) return;
        Permanent originalAura = entry.getSourcePermanentSnapshot() != null
                ? entry.getSourcePermanentSnapshot() : aura;
        UUID enchantedPermanentId = originalAura.getAttachedTo() != null
                ? originalAura.getAttachedTo() : originalAura.getLastAttachedTo();
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null || gameQueryService.cantBeSacrificed(gameData, enchanted)
                || !gameQueryService.triggeredAbilityCanMoveCreatureToken(gameData, enchanted)) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) {
            return;
        }
        if (!auraOnBattlefield) {
            permanentRemovalService.sacrificePermanentToGraveyard(gameData, enchanted);
            return;
        }

        SacrificeEnchantedPermanentAndReattachSourceAuraEffect reattachEffect =
                (SacrificeEnchantedPermanentAndReattachSourceAuraEffect) effect;
        // Valid re-attach destinations that player controls, other than the permanent about to be sacrificed.
        List<UUID> validTargetIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getId().equals(enchantedPermanentId)) continue;
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p,
                        reattachEffect.destinationFilter())
                        && auraAttachmentService.canEnchant(gameData, aura.getCard(), entry.getControllerId(), p)) {
                    validTargetIds.add(p.getId());
                }
            }
        }

        if (validTargetIds.size() > 1) {
            // Defer the sacrifice until the destination is chosen so the Aura is never orphaned
            // (and removed by a state-based action) while awaiting input.
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ReattachSourceAuraAfterSacrifice(aura.getId(), enchantedPermanentId));
            playerInputService.beginPermanentChoice(gameData, controllerId, validTargetIds,
                    aura.getCard().getName() + " — Choose a permanent to attach it to.");
            return;
        }

        // Sacrifice the enchanted permanent.
        if (!permanentRemovalService.sacrificePermanentToGraveyard(gameData, enchanted)) {
            return;
        }
        entry.setSacrificedCard(enchanted.getCard());
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", enchanted.getCard(), "."));

        if (validTargetIds.isEmpty()) {
            // No legal destination — the Aura stays unattached and is removed as a state-based action.
            gameLogService.append(gameData, GameLog.textCardText("There is no legal permanent to attach ", aura.getCard(), " to."));
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, validTargetIds.getFirst());
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(newTarget.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.cardTextCard(aura.getCard(), " is now attached to ", newTarget.getCard(), "."));
        log.info("Game {} - {} reattached to {} after sacrifice", gameData.id,
                aura.getCard().getName(), newTarget.getCard().getName());
    }
}
