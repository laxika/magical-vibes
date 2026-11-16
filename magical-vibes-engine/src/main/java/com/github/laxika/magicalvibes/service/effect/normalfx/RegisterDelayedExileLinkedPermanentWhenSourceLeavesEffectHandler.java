package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Creates Coffin Queen's delayed exile ability (CR 603.7a). The creature the ability just put onto the battlefield is
 * read from, and then cleared off, the source's linked-permanent slot, so the ability names it whatever the source's
 * own abilities become.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        UUID linkedId = source.getChosenPermanentId();
        if (linkedId == null) {
            return;
        }
        source.setChosenPermanentId(null);
        // "When this creature becomes untapped or you lose control of this creature, exile that creature": about the
        // source, with no stated duration (CR 603.7b). It leaving the battlefield loses you control of it too (its
        // ruling of 2014-02-01: one that left before the ability resolved can never fulfil the condition).
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(List.of(new TriggerCondition(Triggers.becomesUntapped()),
                        new TriggerCondition(Triggers.youLoseControlOfThis()),
                        new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS))), TriggerOptions.DEFAULT,
                        List.of(new EffectRegistration(new RemoveLinkedPermanentEffect(
                                RemoveLinkedPermanentEffect.Mode.EXILE, linkedId)))),
                source.getId(), source.getId(), false));
        log.info("Game {} - {} registers delayed trigger: exile {} when it leaves, untaps or is lost",
                gameData.id, entry.getCard().getName(), linkedId);
    }
}
