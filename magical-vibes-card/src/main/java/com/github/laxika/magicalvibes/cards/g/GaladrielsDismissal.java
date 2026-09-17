package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOC", collectorNumber = "16")
@CardRegistration(set = "HOC", collectorNumber = "56")
public class GaladrielsDismissal extends Card {

    public GaladrielsDismissal() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{W}"));
        targetWhenKicked(
                TargetFilters.creature(),
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                1, 1, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                        new Kicked(),
                        new PhaseOutEffect(PhaseOutSubject.TARGET),
                        PhaseOutPermanentsEffect.targetPlayer(new PermanentIsCreaturePredicate())));
    }
}
