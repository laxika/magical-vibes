package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Asks the payer of a {@link MayPayLifeEffect}. A payer who couldn't pay that much life (CR 119.4) can't choose to
 * pay it, so they aren't asked: the "if you don't" effect follows (CR 118.3, 608.2d).
 */
@Component
@RequiredArgsConstructor
public class MayPayLifeEffectResolutionHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeEffect mayPayLife = (MayPayLifeEffect) effect;
        UUID payer = mayPayLife.payer() == MayPayPayer.TRIGGERING_PLAYER
                ? entry.getTargetId() : entry.getControllerId();
        if (payer == null) {
            return;
        }
        if (!ResolutionCostPaymentService.canPayLife(gameQueryService, gameData, payer, mayPayLife.lifeCost())) {
            if (mayPayLife.elseEffect() != null) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(mayPayLife.elseEffect()));
            }
            return;
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                payer,
                List.of(mayPayLife),
                entry.getCard().getName() + " - " + mayPayLife.prompt(),
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId()));
    }
}
