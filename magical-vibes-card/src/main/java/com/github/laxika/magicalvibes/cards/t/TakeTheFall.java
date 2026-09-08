package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "73")
public class TakeTheFall extends Card {

    public TakeTheFall() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ConditionalReplacementEffect(
                        new ControlsPermanent(new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.ASSASSIN,
                                CardSubtype.MERCENARY,
                                CardSubtype.PIRATE,
                                CardSubtype.ROGUE,
                                CardSubtype.WARLOCK))),
                        new BoostTargetCreatureEffect(-1, 0),
                        new BoostTargetCreatureEffect(-4, 0)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
