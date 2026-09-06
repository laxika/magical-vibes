package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "150")
public class GlyphOfDestruction extends Card {

    public GlyphOfDestruction() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be a blocking Wall you control"
        ))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(10, 0))
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allToTargetCreatures())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentAtEndStepEffect());
    }
}
