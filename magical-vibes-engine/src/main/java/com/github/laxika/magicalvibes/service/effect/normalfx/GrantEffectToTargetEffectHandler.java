package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEffectToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantEffectToTargetEffect) effect;

        if (e.scope() == GrantScope.ENCHANTED_PERMANENT) {
            Permanent enchanted = subjectResolutionService.permanent(gameData, entry, effect,
                    GrantScope.ENCHANTED_PERMANENT.subject());
            if (enchanted != null) {
                grantTo(gameData, entry, e, enchanted);
            }
            return;
        }

        if (e.scope() == GrantScope.SELF) {
            // A spell's "put a card onto the battlefield, then it gains ...": the put handler records the permanent as
            // the entry's target, and the spell has no source permanent (Terminal Velocity).
            Permanent source = subjectResolutionService.permanent(gameData, entry, effect,
                    entry.getSourcePermanentId() == null ? Subjects.target() : GrantScope.SELF.subject());
            if (source != null) {
                grantTo(gameData, entry, e, source);
            }
            return;
        }

        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect,
                GrantScope.TARGET.subject())) {
            grantTo(gameData, entry, e, target);
        }
    }

    private void grantTo(GameData gameData, StackEntry entry, GrantEffectToTargetEffect e, Permanent target) {
        // "If it doesn't have [ability], it gains that ability" — grant at most once (Musician).
        // Some abilities intentionally add another instance each time they resolve (Bushido).
        if (e.skipIfAlreadyPresent()
                && (alreadyHasGrantedEffect(gameData, target, e.grantedAbility(), e.grantedEffect())
                || alreadyHasRampage(gameData, target, e.grantedAbility(), e.grantedEffect()))) {
            return;
        }

        // "Until your next upkeep" ends at the upkeep of the effect's controller; with no stated duration the
        // grant lasts until the permanent leaves (CR 611.2a).
        Duration duration = Duration.UNTIL_END_OF_TURN.equals(e.duration())
                || Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP.equals(e.duration())
                ? e.duration() : Duration.PERMANENT;
        gameData.grantAbilityToPermanent(entry, e.grantedAbility(), target.getId(), duration);

        String durationText = Duration.UNTIL_END_OF_TURN.equals(e.duration()) ? " until end of turn"
                : Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP.equals(e.duration()) ? " until your next upkeep" : "";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants a " + AbilityEffects.label(e.grantedAbility()) + " ability to ")
                .card(target.getCard())
                .text(durationText + ".")
                .build());
        log.info("Game {} - {} grants {} effect to {}{}",
                gameData.id, entry.getCard().getName(), AbilityEffects.label(e.grantedAbility()),
                target.getCard().getName(), durationText);
    }

    private boolean alreadyHasGrantedEffect(GameData gameData, Permanent target, Ability ability,
                                            CardEffect granted) {
        List<CardEffect> grantedEffects = gameQueryService.computeStaticBonus(gameData, target).grantedEffects();
        if (ability instanceof StaticAbility) {
            return target.getCard().getStaticEffects().contains(granted) || grantedEffects.contains(granted);
        }
        if (ability instanceof TriggeredAbility triggered) {
            Trigger trigger = triggered.conditions().getFirst().trigger();
            return target.getCard().getTriggerEffects(trigger).contains(granted)
                    || grantedTriggerEffects(grantedEffects, trigger).contains(granted);
        }
        return false;
    }

    private boolean alreadyHasRampage(GameData gameData, Permanent target, Ability ability,
                                      CardEffect granted) {
        Trigger becomesBlocked = Triggers.becomesBlocked();
        if (!(granted instanceof RampageEffect) || !(ability instanceof TriggeredAbility triggered)
                || !triggered.hasTrigger(becomesBlocked)) {
            return false;
        }
        return containsRampage(target.getCard().getTriggerEffects(becomesBlocked))
                || containsRampage(grantedTriggerEffects(
                gameQueryService.computeStaticBonus(gameData, target).grantedEffects(), becomesBlocked));
    }

    /** The effects of the granted triggered abilities among {@code grantedEffects} that have {@code trigger}. */
    private static List<CardEffect> grantedTriggerEffects(List<CardEffect> grantedEffects, Trigger trigger) {
        List<CardEffect> effects = new ArrayList<>();
        for (CardEffect effect : grantedEffects) {
            if (effect instanceof GrantTriggeredAbilityEffect grant
                    && grant.grantedAbility() instanceof TriggeredAbility grantedTriggered
                    && grantedTriggered.hasTrigger(trigger)) {
                effects.addAll(AbilityEffects.effectsOf(grantedTriggered));
            }
        }
        return effects;
    }

    private static boolean containsRampage(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof RampageEffect) {
                return true;
            }
        }
        return false;
    }
}
