package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Begins the redirection of {@link RedirectYourDamageToTargetCreatureThisTurnEffect}: a
 * {@link TurnDamageRedirectToCreatureShield} for the effect's controller, lasting the effect's duration, toward the one
 * permanent its destination names as it resolves (an "enchanted creature" read as it last existed, CR 608.2h). With no
 * destination on the battlefield, or a named damage source that names nothing, nothing begins.
 */
@Component
@RequiredArgsConstructor
public class RedirectYourDamageToTargetCreatureThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectYourDamageToTargetCreatureThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var redirect = (RedirectYourDamageToTargetCreatureThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }
        Permanent destination = subjectResolutionService.permanent(gameData, entry, effect, redirect.destination());
        if (destination == null) {
            return;
        }
        UUID damageSourceId = null;
        if (redirect.damageSource() != null) {
            Permanent source = subjectResolutionService.permanent(gameData, entry, effect, redirect.damageSource());
            if (source == null) {
                return;
            }
            damageSourceId = source.getId();
        }
        gameData.addShield("", null, controllerId, null,
                new TurnDamageRedirectToCreatureShield(controllerId, destination.getId(), redirect.combatOnly(),
                        redirect.redirected(), damageSourceId, redirect.destinationMayBePlaneswalker()),
                redirect.duration());
    }
}
