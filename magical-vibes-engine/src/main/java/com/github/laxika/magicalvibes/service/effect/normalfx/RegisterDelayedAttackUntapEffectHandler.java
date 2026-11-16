package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.springframework.stereotype.Component;

/** Registers "when you next attack this turn, untap each creature you control" (CR 603.7b). */
@Component
public class RegisterDelayedAttackUntapEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedAttackUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.youAttack(),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate())),
                entry.getSourcePermanentId()));
    }
}
