package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheArgentEtchings;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "12")
public class EleshNorn extends Card {

    public EleshNorn() {
        setBackFaceCard(new TheArgentEtchings());

        addEffect(EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                new LoseLifeUnlessPaysEffect(2, 1));

        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(anotherCreature, anotherCreature, anotherCreature),
                                List.of("another creature", "another creature", "another creature")
                        ),
                        new ExileSelfAndReturnTransformedEffect()
                ),
                "{2}{W}, Sacrifice three other creatures: Exile Elesh Norn, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheArgentEtchings";
    }
}
