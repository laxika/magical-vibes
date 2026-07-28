package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect}: the target
 * creature's controller either pays life equal to that creature's toughness or the creature is
 * destroyed and can't be regenerated. The choice belongs to that controller, so a payable
 * controller is prompted via the may-ability system; a controller that can't pay has the creature
 * destroyed immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            // Target left the battlefield before resolution — the spell does nothing.
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        int lifeCost = lifeCostFor(gameData, target);
        boolean canPay = targetControllerId != null
                && (lifeCost == 0
                        || (gameQueryService.canPlayerLifeChange(gameData, targetControllerId)
                                && gameData.getLife(targetControllerId) >= lifeCost));

        if (!canPay) {
            destroyTargetCreature(gameData, entry.getCard(), targetPermanentId);
            return;
        }

        String prompt = "Pay " + lifeCost + " life? If you don't, " + target.getCard().getName()
                + " is destroyed and can't be regenerated. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of((CardEffect) effect), prompt,
                targetPermanentId, null, entry.getSourcePermanentId()));
    }

    /** Life the creature's controller must pay to save it — its toughness, never negative. */
    public int lifeCostFor(GameData gameData, Permanent target) {
        return Math.max(0, gameQueryService.getEffectiveToughness(gameData, target));
    }

    /** Destroy {@code targetPermanentId} ignoring regeneration, attributing it to {@code sourceCard}. */
    public void destroyTargetCreature(GameData gameData, Card sourceCard, UUID targetPermanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, target, sourceCard.getName(), true);
    }
}
