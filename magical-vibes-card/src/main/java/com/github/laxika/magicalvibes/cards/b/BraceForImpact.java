package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "5")
public class BraceForImpact extends Card {

    public BraceForImpact() {
        var multicoloredCreature = new PermanentIsMulticoloredPredicate();
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        multicoloredCreature
                )),
                "Target must be a multicolored creature"
        )).addEffect(EffectSlot.SPELL,
                PreventDamageEffect.allToTargetCreaturesAndAddPlusOnePlusOneCounters(multicoloredCreature));
    }
}
