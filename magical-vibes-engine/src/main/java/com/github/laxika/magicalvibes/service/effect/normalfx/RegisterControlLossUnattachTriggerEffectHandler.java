package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterControlLossUnattachTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentIfAttachedToControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreatureControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Creates, for each Equipment the resolving ability targets, the delayed triggered ability "when you lose control of
 * that Equipment, unattach it" about it (CR 603.7a, 603.7c), controlled by the resolving ability's controller
 * (CR 603.7d, 603.7e).
 */
@Component
@RequiredArgsConstructor
public class RegisterControlLossUnattachTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterControlLossUnattachTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterControlLossUnattachTriggerEffect) effect;
        for (UUID targetId : entry.targetsForEffect(effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                continue;
            }
            boolean thisTurn = registration.thisTurn();
            TriggerOptions options = thisTurn
                    ? TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(
                            new PermanentAttachedToCreatureControlledBySourceControllerPredicate()))
                    : TriggerOptions.DEFAULT;
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.youLoseControlOfThis(), options,
                            new UnattachEquipmentIfAttachedToControlledCreatureEffect(targetId)),
                    targetId, entry.getSourcePermanentId(), thisTurn));
        }
    }
}
