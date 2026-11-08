package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIs;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect) effect;
        CardEffect grantedEffect = bindCaster(grant.staticEffect(), entry.getControllerId());
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(entry.getControllerId())) {
                return;
            }
            for (Permanent permanent : battlefield) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                        entry.getControllerId(), new GrantEffectEffect(grantedEffect, GrantScope.TARGET),
                        permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            }
        });
    }

    /**
     * "The player who cast [this spell]": a granted ability's event condition naming no player yet
     * ({@link TargetPlayerIs} with a {@code null} player, Hellish Rebuke) names the caster, fixed as the spell resolves.
     */
    private static CardEffect bindCaster(CardEffect staticEffect, UUID casterId) {
        if (staticEffect instanceof GrantTriggeredAbilityEffect grant
                && grant.grantedAbility() instanceof TriggeredAbility ability
                && ability.options().eventCondition() instanceof TargetPlayerIs player
                && player.playerId() == null) {
            TriggeredAbility bound = new TriggeredAbility(ability.conditions(),
                    ability.options().withEventCondition(player.withPlayer(casterId)), ability.effects(),
                    ability.chapterTargets(), ability.instance(), ability.grantingPermanentId(),
                    ability.targetFilter());
            return new GrantTriggeredAbilityEffect(bound, grant.scope(), grant.filter());
        }
        return staticEffect;
    }
}
