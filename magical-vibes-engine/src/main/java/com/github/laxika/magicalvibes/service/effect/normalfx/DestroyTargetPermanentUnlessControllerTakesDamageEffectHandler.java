package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted permanent's damage-or-destruction choice. */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentUnlessControllerTakesDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentUnlessControllerTakesDamageEffect) effect;
        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        if (targetControllerId == null) {
            return;
        }

        String damageDescription = e.damage() instanceof Fixed fixed
                ? "deal " + fixed.value() + " damage"
                : "deal damage equal to its power";
        String prompt = "Have " + entry.getCard().getName() + " " + damageDescription
                + " to you? If you don't, " + target.getCard().getName()
                + " is destroyed. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetPermanentId, null, entry.getSourcePermanentId(), null, 0, 0,
                null, null, null, entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
    }

    /** Destroys the target permanent, attributing the action to the source card. */
    public void destroyTargetPermanent(GameData gameData, Card sourceCard, UUID targetPermanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, target, sourceCard.getName());
    }
}
