package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Completes one optional creature-damage redirect created by Blood of the Martyr. */
@Component
@RequiredArgsConstructor
public class RedirectAllCreatureDamageToControllerHandler implements MayEffectHandlerBean {

    private final ObjectProvider<DamageSupport> damageSupportProvider;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllCreatureDamageToControllerEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        StackEntry damageEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY, ability.sourceCard(), ability.sourceControllerId(),
                ability.sourceCard().getName() + "'s ability", List.of(),
                ability.targetCardId(), ability.sourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        DamageSupport damageSupport = damageSupportProvider.getObject();

        if (accepted) {
            damageSupport.dealDamageToPlayer(
                    gameData, damageEntry, ability.controllerId(), ability.eventValue());
        } else {
            Permanent target = gameQueryService.findPermanentById(gameData, ability.targetCardId());
            if (target != null) {
                gameData.resolvingDeclinedAllCreatureDamageRedirect = true;
                try {
                    damageSupport.dealCreatureDamage(gameData, damageEntry, target, ability.eventValue());
                } finally {
                    gameData.resolvingDeclinedAllCreatureDamageRedirect = false;
                }
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
