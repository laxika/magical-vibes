package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureUnlessControllerPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the target-creature controller's choice for
 * {@link ReturnTargetCreatureUnlessControllerPaysEffect}.
 */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureUnlessControllerPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ReturnToHandEffectHandler returnToHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureUnlessControllerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreatureUnlessControllerPaysEffect) effect;
        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanentId);
        if (targetControllerId == null) {
            return;
        }

        if (!new com.github.laxika.magicalvibes.model.ManaCost(e.manaCost())
                .canPay(gameData.playerManaPools.get(targetControllerId))) {
            returnTargetCreature(gameData, entry.getCard(), entry.getControllerId(), targetPermanentId);
            return;
        }

        String prompt = "Pay " + e.manaCost() + "? If you don't, " + target.getCard().getName()
                + " is returned to its owner's hand. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetPermanentId, e.manaCost(), entry.getSourcePermanentId()));
    }

    /** Returns the target creature to its owner's hand, attributing the action to the source. */
    public void returnTargetCreature(GameData gameData, Card sourceCard,
            UUID abilityControllerId, UUID targetPermanentId) {
        ReturnToHandEffect bounce = ReturnToHandEffect.target();
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY, sourceCard, abilityControllerId,
                sourceCard.getName() + " - return target creature to its owner's hand",
                List.of(bounce), targetPermanentId, (UUID) null);
        returnToHandEffectHandler.resolve(gameData, syntheticEntry, bounce);
    }
}
