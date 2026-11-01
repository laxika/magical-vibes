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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
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
     * The abilities other permanents' static abilities grant {@code permanent} that trigger on its own zone
     * change ("when this dies", "when this leaves the battlefield"), as it is now. A departure records them
     * before the permanent leaves, so its look-back triggers see them (CR 603.10a).
     */
    public List<TriggeredAbility> grantedSelfZoneChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilities(gameData, permanent,
                gameQueryService.computeStaticBonus(gameData, permanent, (UUID) null),
                ability -> ability.conditions().stream().anyMatch(condition ->
                        condition.trigger() instanceof ZoneChangeTrigger trigger
                                && trigger.relation() == ObjectRelation.THIS));
    }

    private List<TriggeredAbility> grantedTriggeredAbilities(GameData gameData, Permanent permanent,
                                                             GameQueryService.StaticBonus staticBonus,
                                                             Predicate<TriggeredAbility> wanted) {
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
