package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedPermanentAndReattachSourceAuraEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
 * the enchanted permanent's controller sacrifices it, then moves the source Aura onto a creature or
 * land they control. The Aura keeps its controller. If that player has no other creature or land the
 * enchanted permanent is still sacrificed and the Aura is left unattached (removed as an SBA).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentPredicate CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()
    ));

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedPermanentAndReattachSourceAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }

        UUID enchantedPermanentId = aura.getAttachedTo();
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) {
            return;
        }

        // Valid re-attach destinations: creatures and lands that player controls, other than the
        // permanent about to be sacrificed.
        List<UUID> validTargetIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getId().equals(enchantedPermanentId)) continue;
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, CREATURE_OR_LAND)) {
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
                    aura.getCard().getName() + " — Choose a creature or land to attach it to.");
            return;
        }

        // Sacrifice the enchanted permanent.
        permanentRemovalService.removePermanentToGraveyard(gameData, enchanted);
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, playerName + " sacrifices " + enchanted.getCard().getName() + ".");

        if (validTargetIds.isEmpty()) {
            // No legal destination — the Aura stays unattached and is removed as a state-based action.
            gameBroadcastService.logAndBroadcast(gameData,
                    "There is no creature or land to attach " + aura.getCard().getName() + " to.");
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, validTargetIds.getFirst());
        aura.setAttachedTo(newTarget.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());
        gameBroadcastService.logAndBroadcast(gameData,
                aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".");
        log.info("Game {} - {} reattached to {} after sacrifice", gameData.id,
                aura.getCard().getName(), newTarget.getCard().getName());
    }
}
