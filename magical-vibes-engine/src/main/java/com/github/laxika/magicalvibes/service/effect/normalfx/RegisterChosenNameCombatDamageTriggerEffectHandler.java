package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterChosenNameCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.springframework.stereotype.Component;

/** Resolves {@link RegisterChosenNameCombatDamageTriggerEffect}: the ability names the creature by the chosen name. */
@Component
public class RegisterChosenNameCombatDamageTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterChosenNameCombatDamageTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String name = entry.getChosenCardName();
        if (name == null) {
            return;
        }
        var register = (RegisterChosenNameCombatDamageTriggerEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.creatureDealsCombatDamageToPlayer()
                        .withFilter(new PermanentNamedPredicate(name)),
                        register.effects().toArray(CardEffect[]::new)), entry.getSourcePermanentId()));
    }
}
