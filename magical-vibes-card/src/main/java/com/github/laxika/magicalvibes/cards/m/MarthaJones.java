package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "48")
@CardRegistration(set = "WHO", collectorNumber = "363")
@CardRegistration(set = "WHO", collectorNumber = "653")
@CardRegistration(set = "WHO", collectorNumber = "954")
public class MarthaJones extends Card {

    public MarthaJones() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));

        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature"), 0, 1)
                .addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                                SequenceEffect.of(
                                        new MakeCreatureUnblockableEffect(true),
                                        new MakeCreatureUnblockableEffect()
                                )
                        ));
    }
}
