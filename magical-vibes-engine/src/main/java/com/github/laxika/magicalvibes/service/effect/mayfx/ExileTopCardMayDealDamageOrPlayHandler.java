package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayDealDamageOrPlayEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToTargetCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExileTopCardMayDealDamageOrPlayHandler implements MayEffectHandlerBean {

    private final DealDamageToTargetCreatureEffectHandler damageHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayDealDamageOrPlayEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExileTopCardMayDealDamageOrPlayEffect effect = ability.effects().stream()
                .filter(ExileTopCardMayDealDamageOrPlayEffect.class::isInstance)
                .map(ExileTopCardMayDealDamageOrPlayEffect.class::cast)
                .findFirst()
                .orElseThrow();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(effect.exiledCardId());
        if (accepted && exiledEntry != null) {
            CardEffect damage = new DealDamageToTargetCreatureEffect(exiledEntry.card().getManaValue());
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + " deals damage to the target creature.",
                    List.of(damage),
                    effect.targetCreatureId(),
                    (java.util.UUID) null
            );
            damageHandler.resolve(gameData, damageEntry, damage);
        } else if (!accepted && exiledEntry != null) {
            gameData.exilePlayPermissions.put(exiledEntry.card().getId(), ability.controllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(exiledEntry.card().getId());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
