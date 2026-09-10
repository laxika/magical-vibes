package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "85")
public class ThievingSkydiver extends Card {

    public ThievingSkydiver() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{X}"));
        targetWhenKicked(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentManaValueAtMostXPredicate()
                )),
                "Target must be an artifact with mana value X or less"
        ), 0, 0, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new TargetPermanentMatches(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)),
                                new AttachTargetToSourcePermanentEffect()));
    }
}
