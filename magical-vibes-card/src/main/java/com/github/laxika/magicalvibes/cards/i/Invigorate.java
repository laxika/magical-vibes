package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EachOpponentGainsLifeCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "254")
public class Invigorate extends Card {

    public Invigorate() {
        // If you control a Forest, you may have an opponent gain 3 life rather than pay this
        // spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new EachOpponentGainsLifeCastingCost(3)),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                false));

        // Target creature gets +4/+4 until end of turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
    }
}
