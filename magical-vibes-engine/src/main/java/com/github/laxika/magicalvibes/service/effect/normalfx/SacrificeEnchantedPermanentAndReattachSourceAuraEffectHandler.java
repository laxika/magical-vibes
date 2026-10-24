package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
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
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
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
public class SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final PermanentRemovalService permanentRemovalService;
    private final DepartureService departureService;
    private final StepDepartureSupport stepDepartureSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedPermanentAndReattachSourceAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent originalAura = entry.getSourcePermanentSnapshot();
        if (originalAura == null) return;
        Permanent aura = gameQueryService.findPermanentById(gameData, originalAura.getId());
        boolean auraOnBattlefield = aura != null;
        if (aura == null) aura = originalAura;
        UUID enchantedPermanentId = entry.getSourcePermanentId();
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null || gameQueryService.cantBeSacrificed(gameData, enchanted)
                || !gameQueryService.triggeredAbilityCanMoveCreatureToken(gameData, enchanted)) {
            return;
        }

        // "You" is the trigger's controller; a permanent can only be sacrificed by the player who
        // controls it, so a control change after the trigger leaves the sacrifice undone while the
        // Aura still moves onto a permanent the trigger's controller controls.
        UUID controllerId = entry.getControllerId();
        boolean sacrificeEnchanted = controllerId.equals(
                gameQueryService.findPermanentController(gameData, enchantedPermanentId));
        if (!auraOnBattlefield) {
            if (sacrificeEnchanted) {
                stepDepartureSupport.sacrifice(gameData, entry, effect, enchantedPermanentId);
            }
            return;
        }

        SacrificeEnchantedPermanentAndReattachSourceAuraEffect reattachEffect =
                (SacrificeEnchantedPermanentAndReattachSourceAuraEffect) effect;
        // Valid re-attach destinations that player controls, other than a permanent about to be sacrificed.
        List<UUID> validTargetIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (sacrificeEnchanted && p.getId().equals(enchantedPermanentId)) continue;
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
                    new PermanentChoiceContext.ReattachSourceAuraAfterSacrifice(aura.getId(),
                            sacrificeEnchanted ? enchantedPermanentId : null));
            playerInputService.beginPermanentChoice(gameData, controllerId, validTargetIds,
                    aura.getCard().getName() + " — Choose a permanent to attach it to.");
            return;
        }

        if (sacrificeEnchanted) {
            // The Aura is attached right after the sacrifice, before orphaned Auras are removed, so this
            // departure can't end the step (DepartureService#departAsStep removes them as it finishes). A
            // CR 616.1 question instead stops this handler, which changed nothing yet, and resolves it again.
            DepartureService.Outcome.Done done = departureService.departNow(gameData,
                    DepartureBatch.of(List.of(DepartureIntent.sacrifice(enchantedPermanentId))),
                    DepartureService.Hooks.NONE);
            if (done.results().stream().noneMatch(DepartureResult::sacrificed)) {
                return;
            }
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, controllerId, enchanted.getCard());
            entry.setSacrificedCard(enchanted.getCard());
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " sacrifices ", enchanted.getCard(), "."));
        }

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
