package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AmountsFixedWhenFloatedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Floats the granted static effect on each player the grant names, for its duration; a source-linked duration keeps
 * the source on the floating effect so the duration clock can end it, and so does a granted effect that names its
 * source ("this creature"), so its reader can find it. The amounts of an {@link AmountsFixedWhenFloatedEffect} are
 * evaluated once, as the effect begins, and the float keeps those numbers (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class GrantStaticEffectToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToPlayerEffect) effect;
        UUID sourcePermanentId = null;
        if (grant.duration().isSourceLinked()) {
            // A "for as long as" grant whose source already left never begins (CR 611.2b).
            sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                return;
            }
        } else if (namesItsSource(grant.staticEffect())) {
            // "Dealt to this creature instead" floated this turn (Kjeldoran Royal Guard): "this creature" is the
            // ability's source, so the float keeps it; once it has left, the redirection does nothing (CR 614.9).
            sourcePermanentId = entry.getSourcePermanentId();
        }
        CardEffect staticEffect = grant.staticEffect();
        if (staticEffect instanceof AmountsFixedWhenFloatedEffect withAmounts) {
            AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
            staticEffect = withAmounts.withAmountsFixed(amount -> amount instanceof Fixed
                    ? amount : new Fixed(amountEvaluationService.evaluate(gameData, amount, context)));
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, grant.player())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    sourcePermanentId,
                    entry.getControllerId(),
                    staticEffect,
                    null,
                    playerId,
                    null,
                    grant.duration(),
                    0L));
        }
    }

    /** Whether the granted static effect acts on its own source permanent ("dealt to this creature instead"). */
    private static boolean namesItsSource(CardEffect staticEffect) {
        return staticEffect instanceof RedirectPlayerDamageToSelfEffect redirect
                && redirect.subject() instanceof PermanentSubject.Source;
    }
}
