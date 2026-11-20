package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AnimatePermanentsEffect} by dispatching on its duration and scope to the matching
 * {@link AnimationSupport} branch. Each branch preserves the behaviour of the record it replaced
 * (until-end-of-turn self/target, until-your-next-turn own-lands, controlled-mass, permanent-target,
 * and while-source-on-battlefield target animations).
 */
@Component
@RequiredArgsConstructor
public class AnimatePermanentsEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimatePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AnimatePermanentsEffect) effect;
        if (Duration.PERMANENT.equals(e.duration())) {
            if (Subjects.permanentsYouControl().equals(e.subject()) && e.filter() != null) {
                animationSupport.animateControlledPermanentsPermanently(gameData, entry, e);
            } else {
                animationSupport.animatePermanentTarget(gameData, entry, e);
            }
        } else if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(e.duration())) {
            animationSupport.animateWhileSource(gameData, entry, e);
        } else if (Duration.WHILE_SOURCE_REMAINS_TAPPED.equals(e.duration())) {
            animationSupport.animateWhileSourceRemainsTapped(gameData, entry, e);
        } else {
            PermanentSubject subject = e.subject();
            if (Subjects.landsYouControl().equals(subject)) {
                animationSupport.animateOwnLands(gameData, entry, e);
            } else if (Subjects.allLands().equals(subject) || Subjects.otherLands().equals(subject)) {
                animationSupport.animateAllLands(gameData, entry, e);
            } else if (subject.declaresPlayerTarget()) {
                animationSupport.animateTargetPlayersLands(gameData, entry, e);
            } else if (Subjects.permanentsYouControl().equals(subject)) {
                animationSupport.animateControlledPermanents(gameData, entry, e);
            } else {
                animationSupport.animateSingle(gameData, entry, e);
            }
        }
    }
}
