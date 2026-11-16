package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() != null) {
            int damageToController = ((SacrificeAtEndOfCombatEffect) effect).damageToController();
            List<CardEffect> effects = new ArrayList<>(List.of(new SacrificeSelfEffect()));
            if (damageToController > 0) {
                effects.add(new DealDamageToPlayersEffect(damageToController, DamageRecipient.CONTROLLER));
            }
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.endOfCombat(), effects.toArray(CardEffect[]::new)),
                    entry.getSourcePermanentId(), null, null, DelayedReferents.NONE));
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" will be sacrificed at end of combat.")
                    .build());
        }
    }
}
