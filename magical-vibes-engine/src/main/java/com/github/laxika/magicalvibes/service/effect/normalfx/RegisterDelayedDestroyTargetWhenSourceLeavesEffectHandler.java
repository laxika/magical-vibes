package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyTargetWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDestroyTargetWhenSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDestroyTargetWhenSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || entry.getSourcePermanentId() == null) {
            log.info("Game {} - Delayed destroy-on-source-leave not registered (missing target or source)",
                    gameData.id);
            return;
        }

        var register = (RegisterDelayedDestroyTargetWhenSourceLeavesEffect) effect;
        // "When this artifact leaves the battlefield this turn, destroy that creature" (War Barge), "when Merieke Ri
        // Berit leaves the battlefield or becomes untapped, destroy that creature": about the source, for this turn or
        // with no stated duration (CR 603.7b). The creature can't be regenerated.
        List<TriggerCondition> conditions = new ArrayList<>();
        conditions.add(new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)));
        if (register.alsoWhenSourceUntaps()) {
            conditions.add(new TriggerCondition(Triggers.becomesUntapped()));
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(conditions, TriggerOptions.DEFAULT, List.of(new EffectRegistration(
                        new DestroyLinkedPermanentEffect(true, target.getId())))),
                entry.getSourcePermanentId(), entry.getSourcePermanentId(), register.thisTurnOnly()));
        log.info("Game {} - {} registers delayed trigger: if source leaves this turn, destroy {}",
                gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
