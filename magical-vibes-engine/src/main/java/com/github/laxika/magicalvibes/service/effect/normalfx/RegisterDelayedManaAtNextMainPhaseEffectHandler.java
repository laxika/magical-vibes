package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaAtNextMainPhaseEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RegisterDelayedManaAtNextMainPhaseEffect}: adds a mandatory, this-turn-only
 * delayed triggered ability for the controller (see {@link #delayedMana}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedManaAtNextMainPhaseEffectHandler implements NormalEffectHandlerBean {

    /**
     * The delayed triggered ability "at the beginning of your next main phase, add {@code amount} mana" a resolving
     * spell or ability of {@code card} controlled by {@code controllerId} made: of {@code color}, or in any combination
     * of colors when {@code anyColorCombination}, and when {@code optional} only if its controller means to. It is
     * about the next main phase of either kind, or only the first (precombat) one when {@code firstMainOnly} ("your
     * next first main phase"), and ends with the turn when {@code thisTurnOnly} ("your next main phase this turn").
     */
    public static TemporaryGlobalTriggeredAbility delayedMana(UUID controllerId, Card card, UUID sourcePermanentId,
                                                              ManaColor color, int amount, boolean optional,
                                                              boolean thisTurnOnly, boolean anyColorCombination,
                                                              boolean firstMainOnly) {
        CardEffect manaEffect = anyColorCombination
                ? new AwardManaOfColorsEffect(ManaColor.COLORS, amount)
                : new AwardManaEffect(color, amount);
        if (optional) {
            manaEffect = new MayEffect(manaEffect, "Add " + amount + " " + color.getCode() + "?");
        }
        List<TriggerCondition> mainPhases = firstMainOnly
                ? List.of(new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.YOU)))
                : List.of(new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.YOU)),
                new TriggerCondition(Triggers.postcombatMainPhase(ActivePlayer.YOU)));
        TriggeredAbility ability = new TriggeredAbility(mainPhases, TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(manaEffect)));
        return thisTurnOnly
                ? TemporaryGlobalTriggeredAbility.nextThisTurn(controllerId, card, ability, sourcePermanentId)
                : TemporaryGlobalTriggeredAbility.atNextStep(controllerId, card, ability, sourcePermanentId, null,
                null, DelayedReferents.NONE);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedManaAtNextMainPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedManaAtNextMainPhaseEffect) effect;
        if (e.amount() <= 0) {
            return;
        }

        gameData.temporaryGlobalTriggeredAbilities.add(delayedMana(entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), e.color(), e.amount(), false, true, false, false));

        log.info("Game {} - {} schedules {} {} at their next main phase this turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), e.amount(), e.color());
    }
}
