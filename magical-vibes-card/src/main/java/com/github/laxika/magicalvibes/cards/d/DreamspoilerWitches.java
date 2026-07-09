package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "108")
public class DreamspoilerWitches extends Card {

    public DreamspoilerWitches() {
        // Flying (auto-loaded from Scryfall).
        // Whenever you cast a spell during an opponent's turn, you may have target creature get
        // -1/-1 until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(null, List.of(new BoostTargetCreatureEffect(-1, -1)), true),
                "Have target creature get -1/-1 until end of turn?"
        ));
    }
}
