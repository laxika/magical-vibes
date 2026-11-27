package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Perpetually gives each targeted creature's card a static ability: an entry replacement ("enters with an additional
 * shield counter") in the card's perpetual ability store, any other one as a floating grant re-made as the card enters.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantStaticEffectToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantStaticEffectToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantStaticEffectToTargetCreatureEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            if (grant.staticEffect() instanceof EnterWithCountersEffect) {
                // "This creature enters with ..." is read as the card enters, from the physical card's perpetual store.
                Card physical = target.getOriginalCard() != null ? target.getOriginalCard() : target.getCard();
                gameData.grantPerpetualAbility(physical.getId(), StaticAbility.of(grant.staticEffect()));
                continue;
            }

            List<CardEffect> granted = List.of(new GrantEffectEffect(grant.staticEffect(), Subjects.target()));
            PerpetualCardBattlefieldEffectSupport.remember(gameData, target.getCard(), granted);
            PerpetualCardBattlefieldEffectSupport.apply(gameData,
                    gameQueryService.findPermanentController(gameData, targetId), target, granted);
        }
    }
}
