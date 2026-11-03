package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantingPermanentAwareEffect;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

    private static final Set<GrantScope> ATTACHMENT_SCOPES = EnumSet.of(
            GrantScope.ENCHANTED_CREATURE, GrantScope.ENCHANTED_PERMANENT, GrantScope.EQUIPPED_CREATURE);

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
     * The effects of the abilities with {@code trigger} that other permanents' static abilities grant
     * {@code permanent}, as it is now: "creatures you control have 'when this creature enters, …'".
     */
    public List<CardEffect> grantedTriggerEffects(GameData gameData, Permanent permanent, Trigger trigger) {
        return grantedTriggerEffects(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null), trigger);
    }

    /**
     * Controller-aware form for collecting abilities from a permanent snapshot after it has left the battlefield.
     */
    public List<CardEffect> grantedTriggerEffects(GameData gameData, Permanent permanent, UUID controllerId,
                                                  Trigger trigger) {
        return grantedTriggerEffects(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, controllerId), trigger);
    }

    /**
     * Form for callers that already hold the permanent's static bonus for the current game state, sparing a second
     * layered query. Each effect is wrapped in its ability's options ({@link TriggeredAbility#legacyEffects()}), the
     * form the collectors that still read effects take, and an ability is found by its trigger's
     * {@link com.github.laxika.magicalvibes.model.trigger.Trigger#watchKey()}.
     */
    public List<CardEffect> grantedTriggerEffects(GameData gameData, Permanent permanent,
                                                  GameQueryService.StaticBonus staticBonus, Trigger trigger) {
        return grantedTriggeredAbilities(gameData, permanent, staticBonus,
                ability -> ability.hasWatchedTrigger(trigger)).stream()
                .flatMap(ability -> ability.legacyEffects().stream().map(EffectRegistration::effect))
                .toList();
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
     * leaves them: for a collector that reads granted abilities themselves rather than their effects.
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
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                if (!source.isAttached() || !permanent.getId().equals(source.getAttachedTo())) continue;
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    if (!(effect instanceof GrantTriggeredAbilityEffect grant)
                            || !(grant.grantedAbility() instanceof TriggeredAbility ability)
                            || !wanted.test(ability)
                            || !ATTACHMENT_SCOPES.contains(grant.scope())
                            || !(grant.grantedEffect() instanceof GrantingPermanentAwareEffect)) {
                        continue;
                    }
                    TriggeredAbility bound = ability.withEffects(List.of(new EffectRegistration(
                            bindGrantingPermanent(grant.grantedEffect(), source.getId()))));
                    int unboundIndex = result.indexOf(ability);
                    if (unboundIndex >= 0) {
                        result.set(unboundIndex, bound);
                    } else {
                        result.add(bound);
                    }
                }
            }
        }
        return result;
    }

    private CardEffect bindGrantingPermanent(CardEffect effect, UUID permanentId) {
        return effect instanceof GrantingPermanentAwareEffect aware
                ? aware.withGrantingPermanentId(permanentId)
                : effect;
    }
}
