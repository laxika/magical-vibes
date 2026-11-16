package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSacrificeSourceWhenTargetLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSacrificeSourceWhenTargetLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || entry.getSourcePermanentId() == null) {
            log.info("Game {} - Delayed sacrifice-on-leave not registered (missing target or source)",
                    gameData.id);
            return;
        }

        // "When that creature leaves the battlefield this turn, sacrifice this creature": about the target, with this
        // turn as its stated duration (CR 603.7b); its source is this ability's (CR 603.7e).
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS), new SacrificeSelfEffect()),
                target.getId(), entry.getSourcePermanentId(), true));
        log.info("Game {} - {} registers delayed trigger: if {} leaves this turn, sacrifice source",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
