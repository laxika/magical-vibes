package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureUnlessControllerPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TapTargetCreatureUnlessControllerPaysLifeEffect}: the target creature's
 * controller either pays the life or the creature is tapped. The choice belongs to that controller,
 * so a payable controller is prompted via the may-ability system; a controller that can't pay the
 * life has the creature tapped immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapTargetCreatureUnlessControllerPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TapPermanentsEffectHandler tapPermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapTargetCreatureUnlessControllerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapTargetCreatureUnlessControllerPaysLifeEffect) effect;

        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            // Target left the battlefield before resolution — the ability does nothing.
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        boolean canPay = targetControllerId != null
                && gameQueryService.canPlayerLifeChange(gameData, targetControllerId)
                && gameData.getLife(targetControllerId) >= e.lifeCost();

        if (!canPay) {
            // Can't pay the life — tap the creature.
            tapTargetCreature(gameData, entry.getCard(), entry.getControllerId(), targetPermanentId);
            return;
        }

        // Payable — ask the target creature's controller. Carry the target permanent id so the
        // decline branch can tap it.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, " + target.getCard().getName()
                + " becomes tapped. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetPermanentId, null, entry.getSourcePermanentId()));
    }

    /** Tap the creature {@code targetPermanentId}, attributing the tap to {@code sourceCard}. */
    public void tapTargetCreature(GameData gameData, Card sourceCard, UUID abilityControllerId, UUID targetPermanentId) {
        TapPermanentsEffect tap = new TapPermanentsEffect(TapUntapScope.TARGET);
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, sourceCard, abilityControllerId,
                sourceCard.getName() + " - tap target creature",
                List.of(tap), targetPermanentId, (UUID) null);
        tapPermanentsEffectHandler.resolve(gameData, syntheticEntry, tap);
    }
}
