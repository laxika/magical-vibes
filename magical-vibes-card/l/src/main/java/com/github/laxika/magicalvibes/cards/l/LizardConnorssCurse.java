package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "106")
@CardRegistration(set = "SPM", collectorNumber = "265")
@CardRegistration(set = "OM1", collectorNumber = "108")
public class LizardConnorssCurse extends Card {

    public LizardConnorssCurse() {
        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new PermanentPredicateTargetFilter(otherCreature, "Target must be another creature"), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GrantColorEffect(CardColor.GREEN, GrantScope.TARGET, true))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GrantSubtypeEffect(CardSubtype.LIZARD, GrantScope.TARGET, true))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AnimatePermanentsEffect(
                                new Fixed(4), new Fixed(4), List.of(), Set.of(), null,
                                Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.PERMANENT,
                                null, true));
    }
}
