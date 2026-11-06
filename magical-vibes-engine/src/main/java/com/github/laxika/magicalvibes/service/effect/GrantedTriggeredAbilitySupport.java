package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Collects triggered abilities granted continuously via {@link GrantTriggeredAbilityEffect}
 * (e.g. Tandem Lookout granting its soulbond pair "Whenever this creature deals damage to an
 * opponent, draw a card"). Trigger-collection sites append these next to the permanent's own
 * {@code EffectSlot} effects.
 */
@Component
@RequiredArgsConstructor
public class GrantedTriggeredAbilitySupport {

    private final GameQueryService gameQueryService;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredEffects(gameData, permanent, (UUID) null, slot);
    }

    /**
     * Controller-aware form for collecting abilities from a permanent snapshot after it has left
     * the battlefield.
     */
    public List<CardEffect> grantedTriggeredEffects(
            GameData gameData, Permanent permanent, UUID controllerId, EffectSlot slot) {
        return grantedTriggeredEffects(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, controllerId), slot);
    }

    /**
     * Form for callers that already hold the permanent's static bonus for the current game state,
     * sparing a second layered query.
     */
    public List<CardEffect> grantedTriggeredEffects(
            GameData gameData, Permanent permanent, GameQueryService.StaticBonus staticBonus, EffectSlot slot) {
        return grantedTriggeredAbilities(gameData, permanent, staticBonus,
                ability -> LegacySlots.slotOf(ability) == slot).stream()
                .map(ability -> ability.effects().getFirst().effect())
                .toList();
    }

    /**
     * The "when this enters" abilities ({@link TriggeredAbility#hasOwnEntry()}) that other permanents' static abilities
     * grant {@code permanent}, as it is now: "creatures you control have 'when this creature enters, …'".
     */
    public List<TriggeredAbility> grantedOwnEntryAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilities(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null), TriggeredAbility::hasOwnEntry);
    }

    /**
     * The abilities other permanents' static abilities grant {@code permanent}, as it is now, with a trigger condition
     * whose {@link com.github.laxika.magicalvibes.model.trigger.Trigger#watchKey()} is {@code watched}: for a collector
     * that reads the abilities themselves (the combat collectors, {@code CombatAbilityReader}).
     */
    public List<TriggeredAbility> grantedWatchedAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return grantedTriggeredAbilities(gameData, permanent, null, ability -> ability.hasWatchedTrigger(watched));
    }

    /**
     * Controller-aware form of {@link #grantedWatchedAbilities(GameData, Permanent, Trigger)}, for a permanent
     * snapshot after it has left the battlefield.
     */
    public List<TriggeredAbility> grantedWatchedAbilities(GameData gameData, Permanent permanent, UUID controllerId,
                                                          Trigger watched) {
        return grantedTriggeredAbilities(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, controllerId),
                ability -> ability.hasWatchedTrigger(watched));
    }

    /**
     * The abilities other permanents' static abilities grant {@code permanent} that trigger on a zone change, its
     * own ("when this dies") or another object's ("whenever a creature dies"), as it is now. A departure records
     * them before the permanent leaves, so its look-back triggers see them (CR 603.10a).
     */
    public List<TriggeredAbility> grantedZoneChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilities(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null),
                ability -> ability.conditions().stream().anyMatch(condition ->
                        condition.trigger() instanceof ZoneChangeTrigger));
    }

    /**
     * The triggered abilities other permanents' static abilities grant {@code permanent} that trigger on it or
     * another permanent phasing, transforming or being turned face up.
     */
    public List<TriggeredAbility> grantedPermanentChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilities(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null),
                ability -> ability.conditions().stream().anyMatch(condition ->
                        condition.trigger() instanceof PermanentChangeTrigger));
    }

    /**
     * The triggered abilities other permanents' static abilities grant {@code permanent} that {@code wanted} accepts,
     * as {@code staticBonus} (the permanent's static bonus for the current game state, or {@code null} to compute it)
     * leaves them: for a collector that reads granted abilities themselves rather than their effects. An ability an
     * Aura or Equipment grants names it ({@link TriggeredAbility#grantingPermanentId()}); a collector that puts it on
     * the stack copies that to the entry.
     */
    public List<TriggeredAbility> grantedTriggeredAbilities(GameData gameData, Permanent permanent,
                                                            GameQueryService.StaticBonus staticBonus,
                                                            Predicate<TriggeredAbility> wanted) {
        if (staticBonus == null) {
            staticBonus = gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null);
        }
        List<TriggeredAbility> result = new ArrayList<>();
        for (CardEffect granted : staticBonus.grantedEffects()) {
            if (granted instanceof GrantTriggeredAbilityEffect grant
                    && grant.grantedAbility() instanceof TriggeredAbility ability && wanted.test(ability)) {
                result.add(ability);
            }
        }
        return result;
    }
}
