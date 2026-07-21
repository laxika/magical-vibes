package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "122")
public class NayaSojourners extends Card {

    public NayaSojourners() {
        // When this creature dies, you may put a +1/+1 counter on target creature. The death pipeline
        // narrows targets to creatures by default, which matches "target creature" exactly — no filter.
        addEffect(EffectSlot.ON_DEATH, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        // Cycling {2}{G} ({2}{G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may put a +1/+1 counter on target creature." The reflexive cycle
        // trigger rides on the cycling ability (Esper Sojourners pattern): its target creature is chosen at
        // activation, the counter resolves, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), new DrawCardEffect(1)),
                "Cycling {2}{G} ({2}{G}, Discard this card: Draw a card.)",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
