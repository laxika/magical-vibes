package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTriggeringAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Gives the attackers bound when an attack trigger was created a keyword until end of turn. */
@Component
@RequiredArgsConstructor
public class GrantKeywordToTriggeringAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToTriggeringAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantKeywordToTriggeringAttackersEffect grant = (GrantKeywordToTriggeringAttackersEffect) effect;
        for (AttackingPermanentSnapshot attacker : grant.attackingCreatures()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, attacker.permanentId());
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)
                    || gameQueryService.cantHaveOrGainKeyword(gameData, permanent, grant.keyword())) {
                continue;
            }
            permanent.getGrantedKeywords().add(grant.keyword());
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(Set.of(grant.keyword()), GrantScope.TARGET, null,
                            GrantDuration.END_OF_TURN, null),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
