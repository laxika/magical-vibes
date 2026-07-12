package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachAllAurasToAnotherPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Glamer Spinners' ETB trigger — "attach all Auras enchanting target permanent to another
 * permanent with the same controller." Collects every Aura on the targeted permanent, then prompts the
 * controller to pick another permanent (same controller as the target) that all of those Auras can
 * legally enchant. The reattachment itself happens in
 * {@code PermanentChoiceBattlefieldHandlerService.handleAttachAllAurasToAnotherPermanent}.
 */
@Component
@RequiredArgsConstructor
public class AttachAllAurasToAnotherPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAllAurasToAnotherPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID casterId = entry.getControllerId();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());

        List<Permanent> auras = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (p.getCard().isAura() && target.getId().equals(p.getAttachedTo())) {
                auras.add(p);
            }
        });

        if (auras.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    target.getCard().getName() + " has no Auras to move.");
            return;
        }

        // Valid recipient: another permanent controlled by the same player as the target that every
        // Aura can legally enchant (CR: if none exists, the Auras don't move).
        List<UUID> validRecipientIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (p.getId().equals(target.getId())) return;
            UUID pController = gameQueryService.findPermanentController(gameData, p.getId());
            if (targetControllerId == null || !targetControllerId.equals(pController)) return;
            for (Permanent aura : auras) {
                if (!canEnchant(gameData, aura, p)) return;
            }
            validRecipientIds.add(p.getId());
        });

        if (validRecipientIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    "No permanent can receive the Auras enchanting " + target.getCard().getName() + "; they stay attached.");
            return;
        }

        List<UUID> auraIds = auras.stream().map(Permanent::getId).toList();
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.AttachAllAurasToAnotherPermanent(auraIds));
        playerInputService.beginPermanentChoice(gameData, casterId, validRecipientIds,
                "Attach all Auras enchanting " + target.getCard().getName()
                        + " to another permanent with the same controller.");
    }

    private boolean canEnchant(GameData gameData, Permanent aura, Permanent candidate) {
        TargetFilter auraFilter = aura.getCard().getTargetFilter();
        if (auraFilter == null) {
            return gameQueryService.isCreature(gameData, candidate);
        }
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(aura.getCard().getId())
                .withSourceControllerId(auraControllerId);
        return predicateEvaluationService.checkTargetFilter(auraFilter, candidate, filterContext).isEmpty();
    }
}
