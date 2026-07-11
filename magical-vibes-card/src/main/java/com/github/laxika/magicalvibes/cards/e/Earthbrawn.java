package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "119")
public class Earthbrawn extends Card {

    public Earthbrawn() {
        // Target creature gets +3/+3 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));

        // Reinforce 1—{1}{G} ({1}{G}, Discard this card: Put a +1/+1 counter on target creature.)
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "Reinforce 1—{1}{G} ({1}{G}, Discard this card: Put a +1/+1 counter on target creature.)",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")));
    }
}
