package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DKA", collectorNumber = "120")
public class IncreasingSavagery extends Card {

    public IncreasingSavagery() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 1, 1)
                .addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(5));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD), new PutPlusOnePlusOneCounterOnTargetCreatureEffect(5)));
        addCastingOption(new FlashbackCast("{5}{G}{G}"));
    }
}
