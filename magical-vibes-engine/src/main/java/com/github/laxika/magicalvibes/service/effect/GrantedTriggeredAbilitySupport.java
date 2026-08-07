package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    /**
     * The abilities that Auras/Equipment attached to {@code permanentId} grant it for {@code slot},
     * read straight off the attached permanents instead of through the layer system.
     *
     * <p>Needed by the death-trigger path: by the time death triggers are collected the dying
     * permanent has already left the battlefield, so a layered static-bonus assembly no longer
     * sees it, while the Aura granting the ability is still attached (orphaned Auras only fall
     * off in a later state-based-action pass). Only plain, unconditional grants are read here.
     */
    public List<CardEffect> grantedTriggeredEffectsFromAttachments(GameData gameData, UUID permanentId, EffectSlot slot) {
        List<CardEffect> result = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                if (!source.isAttached() || !permanentId.equals(source.getAttachedTo())) continue;
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof GrantTriggeredAbilityEffect grant
                            && grant.slot() == slot
                            && ATTACHMENT_SCOPES.contains(grant.scope())) {
                        result.add(grant.grantedEffect());
                    }
                }
            }
        }
        return result;
    }

    private static final Set<GrantScope> ATTACHMENT_SCOPES = EnumSet.of(
            GrantScope.ENCHANTED_CREATURE, GrantScope.ENCHANTED_PERMANENT, GrantScope.EQUIPPED_CREATURE);

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        List<CardEffect> result = new ArrayList<>();
        for (CardEffect granted : gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof GrantTriggeredAbilityEffect grant && grant.slot() == slot) {
                result.add(grant.grantedEffect());
            }
        }
        return result;
    }
}
